(ns usermanager.usermanager.interface
  (:require [usermanager.usermanager.model :as model]))

(defn delete-user-by-id
  "Given a user ID, delete that user."
  [db id]
  (model/delete-user-by-id db id))

(defn get-departments
  "Return all available department records (in order)."
  [db]
  (model/get-departments db))

(defn get-user-by-id
  "Given a user ID, return the user record."
  [db id]
  (model/get-user-by-id db id))

(defn get-users
  "Return all available users, sorted by name.

  Since this is a join, the keys in the hash maps returned will
  be namespace-qualified by the table from which they are drawn:

  addressbook/id, addressbook/first_name, etc, department/name"
  [db]
  (model/get-users db))

(defn save-user
  "Save a user record. If ID is present and not zero, then
  this is an update operation, otherwise it's an insert."
  [db user]
  (model/save-user db user))

(defn setup-database [] (model/setup-database))
