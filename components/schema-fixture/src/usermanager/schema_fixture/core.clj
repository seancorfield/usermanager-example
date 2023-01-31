;; copyright (c) 2019-2023 Sean Corfield, all rights reserved

(ns usermanager.schema-fixture.core
  (:require [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [usermanager.schema.interface :as schema]))

(def ^:private current-test-db (atom nil))

(def ^:private db-spec {:dbtype "h2:mem"
                        :dbname "usermanager_test"
                        :database_to_upper false})

(defn with-test-db
  "A test fixture that sets up an in-memory H2 database for running tests."
  [t]
  ;; clear out any existing in-memory data
  (let [ds (jdbc/get-datasource db-spec)]
    (try
      (jdbc/execute-one! ds ["drop table department"])
      (jdbc/execute-one! ds ["drop table addressbook"])
      (catch Exception _)))
  (let [db (component/start (schema/setup-database db-spec))]
    (reset! current-test-db db)
    (t)
    (component/stop db)))

(defn test-db
  "Return the test database."
  []
  @current-test-db)