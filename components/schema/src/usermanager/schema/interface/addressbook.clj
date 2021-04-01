(ns usermanager.schema.interface.addressbook
  (:require [usermanager.schema.core.addressbook :as addressbook]))

(defn create+populate
  "Called at application startup. Attempts to create the
  addressbook table and populate it. Takes no action if the
  database table already exists."
  [db db-type]
  (addressbook/create+populate db db-type))
