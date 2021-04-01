(ns usermanager.schema.interface.department
  (:require [usermanager.schema.core.department :as department]))

(defn create+populate
  "Called at application startup. Attempts to create the
  department table and populate it. Takes no action if the
  database table already exists."
  [db db-type]
  (department/create+populate db db-type))
