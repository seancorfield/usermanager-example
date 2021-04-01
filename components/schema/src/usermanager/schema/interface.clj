(ns usermanager.schema.interface
  (:require [usermanager.schema.core :as schema]))

(defn setup-database
  "Given an optional db-spec (hash map), return a fully
  initialized database."
  [& [db-spec]]
  (schema/setup-database db-spec))
