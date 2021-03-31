(ns usermanager.database.interface
  (:require [usermanager.database.core :as core]))

(defn create [schema]
  ;; todo: update this description + description for the 'schema' component.
  "Given a database spec (hash map) and an initialization
  function (that accepts a database component and a string
  specifying the database type), return an initialized
  database component.

  The component can be invoked with no arguments to return
  the underlying data source (javax.sql.DataSource).

  The init-fn should be able to initialize a freshly-created
  database but also gracefully handle an existing database."
  (core/create schema))
