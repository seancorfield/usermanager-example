(ns usermanager.database.interface
  (:require [usermanager.database.schema :as schema]))

(defn create-schema
  "Given an optional db-spec (hash map), return a fully
  initialized database."
  [& [db-spec]]
  (schema/create-schema db-spec))
