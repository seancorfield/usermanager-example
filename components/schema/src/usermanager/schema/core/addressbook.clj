(ns usermanager.schema.core.addressbook
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [usermanager.database.interface :as database]))

(def ^:private initial-user-data
  "Seed the database with this data."
  [{:first_name "Sean" :last_name "Corfield"
    :email "sean@worldsingles.com" :department_id 4}])

(defn create+populate
  "Called at application startup. Attempts to create the
  addressbook table and populate it. Takes no action if the
  database table already exists."
  [db db-type]
  (try
    (jdbc/execute-one! (db)
                       [(str "
create table addressbook (
  id            integer " (database/auto-increment-key db-type) ",
  first_name    varchar(32),
  last_name     varchar(32),
  email         varchar(64),
  department_id integer not null
)")])
    (println "Created addressbook table!")
      ;; if table creation was successful, it didn't exist before
      ;; so populate it...
    (try
      (doseq [row initial-user-data]
        (sql/insert! (db) :addressbook row))
      (println "Populated address book!")
      (catch Exception e
        (println "Exception:" (ex-message e))
        (println "Unable to populate address book -- proceed with caution!")))
    (catch Exception e
      (println "Exception:" (ex-message e))
      (println "Looks like the database is already setup?"))))