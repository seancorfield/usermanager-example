;; copyright (c) 2019-2023 Sean Corfield, all rights reserved

(ns usermanager.schema-fixture.interface
  (:require [usermanager.schema-fixture.core :as fixture]))

(defn with-test-db
  "A test fixture that sets up an in-memory H2 database for running tests."
  [t]
  (fixture/with-test-db t))

(defn test-db
  "Return the test database."
  []
  (fixture/test-db))
