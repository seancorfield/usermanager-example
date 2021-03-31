(ns usermanager.schema.interface
  (:require [usermanager.schema.core :as core]))

(defn create [& db-spec]
  (core/create-schema db-spec))
