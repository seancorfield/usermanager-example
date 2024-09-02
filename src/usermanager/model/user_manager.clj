;; copyright (c) 2019-2024 Sean Corfield, all rights reserved

(ns usermanager.model.user-manager
  "The model for the application. This is where the persistence happens,
  although in a larger application, this would probably contain just the
  business logic and the persistence would be in a separate namespace."
  (:require [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [xtdb.client :as xtc]))

;; our initial data -- _id is added on insertion:

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
        (sql/insert! (db) :department {:name d :_id (inc ix)}
                     {:return-keys false}))
      (doseq [a initial-user-data]
        (sql/insert! (db) :addressbook (assoc a :_id (random-uuid))
                     {:return-keys false}))
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
      (let [this+ds (assoc this :datasource (jdbc/get-datasource db-spec))]
        ;; set up database if necessary
        (populate this+ds)
        this+ds)))
  (stop [this]
    (if datasource
      (assoc this :datasource nil)
      this))

  ;; allow the Database component to be "called" with no arguments
  ;; to produce the underlying datasource object
  clojure.lang.IFn
  (invoke [_] datasource))

(defn setup-database []
  (map->Database {:db-spec {:dbtype "postgresql"
                            :host "localhost"
                            :port 5432}}))

;; data model access functions

(defn get-department-by-id
  "Given a department ID, return the department record."
  [db id]
  (sql/get-by-id (db) :department id :department._id {}))

(defn get-departments
  "Return all available department records (in order)."
  [db]
  (sql/query (db) ["select d.* from department d order by d.name"]))

(defn get-user-by-id
  "Given a user ID, return the user record."
  [db id]
  (sql/get-by-id (db) :addressbook id :addressbook._id {}))

(defn get-users
  "Return all available users, sorted by name."
  [db]
  (sql/query (db)
             ["
select a.*, d.name
 from addressbook a
 join department d on a.department_id = d._id
 order by a.last_name, a.first_name
"]))

(defn save-user
  "Save a user record. If ID is present and not empty, then
  this is an update operation, otherwise it's an insert."
  [db user]
  (let [id   (:_id user)
        user (dissoc user :_id)]
    (if (and id (uuid? id))
      ;; update
      (sql/update! (db) :addressbook user {:addressbook._id id})
      ;; insert
      (let [id (random-uuid)]
        (sql/insert! (db) :addressbook (assoc user :_id id)
                     {:return-keys false})
        {:_id id}))))

(defn delete-user-by-id
  "Given a user ID, delete that user."
  [db id]
  (sql/delete! (db) :addressbook {:addressbook._id id}))
