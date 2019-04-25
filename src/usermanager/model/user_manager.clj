;; copyright (c) 2019 Sean Corfield, all rights reserved

(ns usermanager.model.user-manager
  (:require [next.jdbc :as jdbc]
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

(defn setup-database
  "Called at application startup. Attempts to create the
  database table and populate it. Takes no action if the
  database table already exists."
  []
  (try
    (jdbc/execute-one! my-db
                       ["
create table department (
  id            integer primary key autoincrement,
  name          varchar(32)
)"])
    (jdbc/execute-one! my-db
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
        (sql/insert! my-db :department {:name d}))
      (doseq [row initial-user-data]
        (sql/insert! my-db :addressbook row))
      (println "Populated database with initial data!")
      (catch Exception e
        (println "Exception:" (ex-message e))
        (println "Unable to populate the initial data -- proceed with caution!")))
    (catch Exception e
      (println "Exception:" (ex-message e))
      (println "Looks like the database is already setup?"))))

(defn get-department-by-id
  "Given a department ID, return the department record.
  Uses in-memory lookup for non-changing data."
  [id]
  (sql/get-by-id my-db :department id))

(defn get-departments
  "Return all available department records (in order)."
  []
  (sql/query my-db ["select * from department order by name"]))

(defn get-user-by-id
  "Given a user ID, return the user record."
  [id]
  (sql/get-by-id my-db :addressbook id))

(defn get-users
  "Return all available users, sorted by name."
  []
  (sql/query my-db
             ["
select a.*, d.name
 from addressbook a
 join department d on a.department_id = d.id
 order by a.last_name, a.first_name
"]))

(defn save-user
  "Save a user record. If ID is present and not zero, then
  this is an update operation, otherwise it's an insert."
  [user]
  (let [id (:addressbook/id user)]
    (if (and id (not (zero? id)))
      ;; update
      (sql/update! my-db :addressbook
                   (dissoc user :addressbook/id)
                   {:id id})
      ;; insert
      (sql/insert! my-db :addressbook
                   (dissoc user :addressbook/id)))))

(defn delete-user-by-id
  "Given a user ID, delete that user."
  [id]
  (sql/delete! my-db :addressbook {:id id}))
