(ns usermanager.schema.core.department
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [usermanager.database.interface :as database]))

(def ^:private departments
  "List of departments."
  ["Accounting" "Sales" "Support" "Development"])

(defn create+populate
  "Called at application startup. Attempts to create the
  department table and populate it. Takes no action if the
  database table already exists."
  [db db-type]
  (try
    (jdbc/execute-one! (db)
                       [(str "
create table department (
  id            integer " (database/auto-increment-key db-type) ",
  name          varchar(32)
)")])

    (println "Created department table!")
      ;; if table creation was successful, it didn't exist before
      ;; so populate it...
    (try
      (doseq [d departments]
        (sql/insert! (db) :department {:name d}))
      (println "Populated departments!")
      (catch Exception e
        (println "Exception:" (ex-message e))
        (println "Unable to populate departments -- proceed with caution!")))
    (catch Exception e
      (println "Exception:" (ex-message e))
      (println "Looks like the database is already setup?"))))