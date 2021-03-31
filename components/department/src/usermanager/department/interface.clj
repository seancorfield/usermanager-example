(ns usermanager.department.interface
  (:require [next.jdbc.sql :as sql]))

(defn get-by-id
  "Given a department ID, return the department record."
  [db id]
  (sql/get-by-id (db) :department id))

(defn get-departments
  "Return all available department records (in order)."
  [db]
  (sql/query (db) ["select * from department order by name"]))
