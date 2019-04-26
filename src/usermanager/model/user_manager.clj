;; copyright (c) 2019 Sean Corfield, all rights reserved

(ns usermanager.model.user-manager
  "The model for the application. This is where the persistence happens,
  although in a larger application, this would probably contain just the
  business logic and the persistence would be in a separate namespace."
  (:require [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

;; our database connection and initial data

(def ^:private my-db
  "SQLite database connection spec."
  {:dbtype "sqlite" :dbname "usermanager_db"})

(def ^:private departments
  "List of departments."
  ["Accounting" "Sales" "Support" "Development"])

(def ^:private initial-user-data
  "Seed the database with this data."
  [{:first_name "Sean" :last_name "Corfield"
    :email "sean@worldsingles.com" :department_id 4}])

;; database initialization

(defn- populate
  "Called at application startup. Attempts to create the
  database table and populate it. Takes no action if the
  database table already exists."
  [db]
  (try
    (jdbc/execute-one! (db)
                       ["
create table department (
  id            integer primary key autoincrement,
  name          varchar(32)
)"])
    (jdbc/execute-one! (db)
                       ["
create table addressbook (
  id            integer primary key autoincrement,
  first_name    varchar(32),
  last_name     varchar(32),
  email         varchar(64),
  department_id integer not null
)"])
    (println "Created database and addressbook table!")
    ;; if table creation was successful, it didn't exist before
    ;; so populate it...
    (try
      (doseq [d departments]
        (sql/insert! (db) :department {:name d}))
      (doseq [row initial-user-data]
        (sql/insert! (db) :addressbook row))
      (println "Populated database with initial data!")
      (catch Exception e
        (println "Exception:" (ex-message e))
        (println "Unable to populate the initial data -- proceed with caution!")))
    (catch Exception e
      (println "Exception:" (ex-message e))
      (println "Looks like the database is already setup?"))))

;; database component

(defrecord Database [datasource]

  component/Lifecycle
  (start [this]
    (if datasource
      this ; already initialized
      (let [database (assoc this :datasource (jdbc/get-datasource my-db))]
        ;; set up database if necessary
        (populate database)
        database)))
  (stop [this]
    (assoc this :datasource nil))

  ;; allow the Database component to be "called" with no arguments
  ;; to produce the underlying datasource object
  clojure.lang.IFn
  (invoke [this] datasource))

(defn setup-database [] (map->Database {}))

;; data model access functions

(defn get-department-by-id
  "Given a department ID, return the department record."
  [db id]
  (sql/get-by-id (db) :department id))

(defn get-departments
  "Return all available department records (in order)."
  [db]
  (sql/query (db) ["select * from department order by name"]))

(defn get-user-by-id
  "Given a user ID, return the user record."
  [db id]
  (sql/get-by-id (db) :addressbook id))

(defn get-users
  "Return all available users, sorted by name.

  Since this is a join, the keys in the hash maps returned will
  be namespace-qualified by the table from which they are drawn:

  addressbook/id, addressbook/first_name, etc, department/name"
  [db]
  (sql/query (db)
             ["
select a.*, d.name
 from addressbook a
 join department d on a.department_id = d.id
 order by a.last_name, a.first_name
"]))

(defn save-user
  "Save a user record. If ID is present and not zero, then
  this is an update operation, otherwise it's an insert."
  [db user]
  (let [id (:addressbook/id user)]
    (if (and id (not (zero? id)))
      ;; update
      (sql/update! (db) :addressbook
                   (dissoc user :addressbook/id)
                   {:id id})
      ;; insert
      (sql/insert! (db) :addressbook
                   (dissoc user :addressbook/id)))))

(defn delete-user-by-id
  "Given a user ID, delete that user."
  [db id]
  (sql/delete! (db) :addressbook {:id id}))
