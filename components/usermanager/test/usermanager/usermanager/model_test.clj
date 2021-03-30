;; copyright (c) 2019-2021 Sean Corfield, all rights reserved

(ns usermanager.usermanager.model-test
  "These tests use H2 in-memory."
  (:require [clojure.test :as test :refer [deftest is testing]]
            [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [usermanager.usermanager.model :as model]))

(def ^:private test-db (atom nil))

(def ^:private db-spec {:dbtype "h2:mem"
                        :dbname "usermanager_test"
                        :database_to_upper false})

(defn- with-test-db
  "A test fixture that sets up an in-memory H2 database for running tests."
  [t]
  ;; clear out any existing in-memory data
  (let [ds (jdbc/get-datasource db-spec)]
    (try
      (jdbc/execute-one! ds ["drop table department"])
      (jdbc/execute-one! ds ["drop table addressbook"])
      (catch Exception _)))
  (let [db (component/start
            (model/map->Database {:db-spec db-spec}))]
    (reset! test-db db)
    (t)
    (component/stop db)))

(test/use-fixtures :once with-test-db)

(deftest department-test
  (is (= #:department{:id 1 :name "Accounting"}
         (model/get-department-by-id @test-db 1)))
  (is (= 4 (count (model/get-departments @test-db)))))

(deftest user-test
  (is (= 1 (:addressbook/id (model/get-user-by-id @test-db 1))))
  (is (= "Sean" (:addressbook/first_name
                 (model/get-user-by-id @test-db 1))))
  (is (= 4 (:addressbook/department_id
            (model/get-user-by-id @test-db 1))))
  (is (= 1 (count (model/get-users @test-db))))
  (is (= "Development" (:department/name
                        (first
                         (model/get-users @test-db))))))

(deftest save-test
  (is (= "sean@corfield.org"
         (:addressbook/email
          (do
            (model/save-user @test-db {:addressbook/id 1
                                       :addressbook/email "sean@corfield.org"})
            (model/get-user-by-id @test-db 1)))))
  (is (= "seancorfield@hotmail.com"
         (:addressbook/email
          (do
            (model/save-user @test-db {:addressbook/first_name "Sean"
                                       :addressbook/last_name "Corfield"
                                       :addressbook/department_id 4
                                       :addressbook/email "seancorfield@hotmail.com"})
            (model/get-user-by-id @test-db 2))))))
