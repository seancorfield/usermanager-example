(ns usermanager.department.interface
  (:require [usermanager.department.core :as department]))

(defn get-department-by-id
  "Given a department ID, return the department record."
  [db id]
  (department/get-department-by-id db id))

(defn get-departments
  "Return all available department records (in order)."
  [db]
  (department/get-departments db))
