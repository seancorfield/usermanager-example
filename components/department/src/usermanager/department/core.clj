;; copyright (c) 2019-2023 Sean Corfield, all rights reserved

(ns usermanager.department.core
  (:require [next.jdbc.sql :as sql]))

(defn get-by-id
  "Given a department ID, return the department record."
  [db id]
  (sql/get-by-id (db) :department id))

(defn get-all
  "Return all available department records (in order)."
  [db]
  (sql/query (db) ["select * from department order by name"]))
