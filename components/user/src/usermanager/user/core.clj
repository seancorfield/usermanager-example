;; copyright (c) 2019-2021 Sean Corfield, all rights reserved

(ns usermanager.user.core
  "The model for the application. This is where the persistence happens,
  although in a larger application, this would probably contain just the
  business logic and the persistence would be in a separate namespace."
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [usermanager.database.interface :as database]))

;; data model access functions

(defn get-user-by-id
  "Given a user ID, return the user record."
  [db id]
  (sql/get-by-id (db) :addressbook id))

(defn get-users
  "Return all available users, sorted by name.

  Since this is a join, the keys in the hash maps returned will
  be namespace-qualified by the table from which they are drawn:

  addressbook/id, addressbook/first_name, etc, department/name"
  [db]
  (sql/query (db)
             ["
select a.*, d.name
 from addressbook a
 join department d on a.department_id = d.id
 order by a.last_name, a.first_name
"]))

(defn save-user
  "Save a user record. If ID is present and not zero, then
  this is an update operation, otherwise it's an insert."
  [db user]
  (let [id (:addressbook/id user)]
    (if (and id (not (zero? id)))
      ;; update
      (sql/update! (db) :addressbook
                   (dissoc user :addressbook/id)
                   {:id id})
      ;; insert
      (sql/insert! (db) :addressbook
                   (dissoc user :addressbook/id)))))

(defn delete-user-by-id
  "Given a user ID, delete that user."
  [db id]
  (sql/delete! (db) :addressbook {:id id}))
