(ns usermanager.schema.core
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

;; our database connection and initial data

(def my-db
  "SQLite database connection spec."
  {:dbtype "sqlite" :dbname "usermanager_db"})

(def departments
  "List of departments."
  ["Accounting" "Sales" "Support" "Development"])

(def initial-user-data
  "Seed the database with this data."
  [{:first_name "Sean" :last_name "Corfield"
    :email "sean@worldsingles.com" :department_id 4}])

;; database initialization

(defn populate
  "Called at application startup. Attempts to create the
  database table and populate it. Takes no action if the
  database table already exists."
  [db db-type]
  (let [auto-key (if (= "sqlite" db-type)
                   "primary key autoincrement"
                   (str "generated always as identity"
                        " (start with 1 increment by 1)"
                        " primary key"))]
    (try
      (jdbc/execute-one! (db)
                         [(str "
create table department (
  id            integer " auto-key ",
  name          varchar(32)
)")])
      (jdbc/execute-one! (db)
                         [(str "
create table addressbook (
  id            integer " auto-key ",
  first_name    varchar(32),
  last_name     varchar(32),
  email         varchar(64),
  department_id integer not null
)")])
      (println "Created department and addressbook table!")
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
        (println "Looks like the database is already setup?")))))

(defn create-schema [db-spec]
  {:db-spec (or db-spec my-db) :init-fn #'populate})
