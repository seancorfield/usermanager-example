(ns usermanager.database.interface
  (:require [usermanager.database.core :as database]))

(defn auto-increment-key
  "Given a database type (e.g., sqlite), return a string
  that can be used to declare a column as an auto-increment
  primary key in a CREATE TABLE statement."
  [db-type]
  (database/auto-increment-key db-type))

(defn create
  "Given a database spec (hash map) and an initialization
  function (that accepts a database component and a string
  specifying the database type), return an initialized
  database component.

  The component can be invoked with no arguments to return
  the underlying data source (javax.sql.DataSource).

  The init-fn should be able to initialize a freshly-created
  database but also gracefully handle an existing database."
  [db-spec init-fn]
  (database/create db-spec init-fn))
