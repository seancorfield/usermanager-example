(ns usermanager.schema.core
  (:require [usermanager.database.interface :as database]
            [usermanager.schema.interface.addressbook :as addressbook]
            [usermanager.schema.interface.department :as department]))

;; our database connection and initial data

(def ^:private my-db
  "SQLite database connection spec."
  {:dbtype "sqlite" :dbname "usermanager_db"})

;; database initialization

(defn- create+populate
  "Called at application startup. Attempts to create the
  database table and populate it. Takes no action if the
  database table already exists."
  [db db-type]
  (department/create+populate db db-type)
  (addressbook/create+populate db db-type))

(defn setup-database [db-spec]
  (database/create (or db-spec my-db) #'create+populate))
