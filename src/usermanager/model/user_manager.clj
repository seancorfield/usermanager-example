;; copyright (c) 2019-2023 Sean Corfield, all rights reserved

(ns usermanager.model.user-manager
  "The model for the application. This is where the persistence happens,
  although in a larger application, this would probably contain just the
  business logic and the persistence would be in a separate namespace."
  (:require [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [next.jdbc.xt] ; activate XTDB support
            [xtdb.client :as xtc]))

;; our initial data -- xt$id is added on insertion:

(def ^:private departments
  "List of departments."
  ["Accounting" "Sales" "Support" "Development"])

(def ^:private initial-user-data
  "Seed the database with this data."
  [{:first_name "Sean" :last_name "Corfield"
    :email "sean@worldsingles.com" :department_id 4}])

;; database initialization

(defn- populate
  "Called at application startup. If the departments are
   missing, insert the initial data."
  [db]
  (when-not (seq (try
                   (sql/query (db) ["select * from department"])
                   (catch Exception _)))
    (try
      (doseq [[ix d] (map-indexed vector departments)]
        (sql/insert! (db) :department {:name d :xt$id (inc ix)}))
      (doseq [a initial-user-data]
        (sql/insert! (db) :addressbook (assoc a :xt$id (str (random-uuid)))))
      (println "Populated database with initial data!")
      (catch Exception e
        (println "Exception:" (ex-message e))
        (println "Unable to populate the initial data -- proceed with caution!")))))

;; database component

(defrecord Database [db-spec     ; configuration
                     datasource] ; state

  component/Lifecycle
  (start [this]
    (if datasource
      this ; already initialized
      (let [database (assoc this :datasource (jdbc/get-datasource db-spec))]
        ;; set up database if necessary
        (populate database)
        database)))
  (stop [this]
    (if datasource
      (do
        (.close datasource)
        (assoc this :datasource nil))
      this))

  ;; allow the Database component to be "called" with no arguments
  ;; to produce the underlying datasource object
  clojure.lang.IFn
  (invoke [_] datasource))

(defn setup-database [] (map->Database {:db-spec (xtc/start-client "http://localhost:3000")}))

;; data model access functions

(defn get-department-by-id
  "Given a department ID, return the department record."
  [db id]
  (sql/get-by-id (db) :department id :department.xt$id {}))

(defn get-departments
  "Return all available department records (in order)."
  [db]
  (sort-by :name (sql/query (db) ["select d.* from department d"])))

(defn get-user-by-id
  "Given a user ID, return the user record."
  [db id]
  (sql/get-by-id (db) :addressbook id :addressbook.xt$id {}))

(defn get-users
  "Return all available users, sorted by name."
  [db]
  (sort-by (juxt :last_name :first_name)
           (sql/query (db)
                      ["select a.*, d.name
 from addressbook a
 join department d on a.department_id = d.xt$id
"])))

(defn save-user
  "Save a user record. If ID is present and not empty, then
  this is an update operation, otherwise it's an insert."
  [db user]
  (let [id   (:xt$id user)
        user (dissoc user :xt$id)]
    (if (seq id)
      ;; update
      (sql/update! (db) :addressbook user {:addressbook.xt$id id})
      ;; insert
      (sql/insert! (db) :addressbook (assoc user :xt$id (str (random-uuid)))))))

(defn delete-user-by-id
  "Given a user ID, delete that user."
  [db id]
  (sql/delete! (db) :addressbook {:addressbook.xt$id id}))
