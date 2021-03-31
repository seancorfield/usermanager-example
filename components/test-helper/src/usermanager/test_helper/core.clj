(ns usermanager.test-helper.core
  (:require [usermanager.database.interface :as database]
            [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]))

(def test-db (atom nil))

(def db-spec {:dbtype "h2:mem"
              :dbname "usermanager_test"
              :database_to_upper false})

(defn with-test-db
  [t]
  ;; clear out any existing in-memory data
  (let [ds (jdbc/get-datasource db-spec)]
    (try
      (jdbc/execute-one! ds ["drop table department"])
      (jdbc/execute-one! ds ["drop table addressbook"])
      (catch Exception _)))
  (let [db (component/start
             (database/create db-spec))]
    (reset! test-db db)
    (t)
    (component/stop db)))
