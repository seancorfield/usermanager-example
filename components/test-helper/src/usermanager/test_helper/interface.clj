(ns usermanager.test-helper.interface
  (:require [usermanager.test-helper.core :as core]))

(defn with-test-db
  "A test fixture that sets up an in-memory H2 database for running tests."
  [t]
  (core/with-test-db t))
