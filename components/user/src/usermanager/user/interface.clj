(ns usermanager.user.interface
  (:require [usermanager.user.core :as user]))

(defn delete-user-by-id
  "Given a user ID, delete that user."
  [db id]
  (user/delete-user-by-id db id))

(defn get-user-by-id
  "Given a user ID, return the user record."
  [db id]
  (user/get-user-by-id db id))

(defn get-users
  "Return all available users, sorted by name.

  Since this is a join, the keys in the hash maps returned will
  be namespace-qualified by the table from which they are drawn:

  addressbook/id, addressbook/first_name, etc, department/name"
  [db]
  (user/get-users db))

(defn save-user
  "Save a user record. If ID is present and not zero, then
  this is an update operation, otherwise it's an insert."
  [db user]
  (user/save-user db user))
