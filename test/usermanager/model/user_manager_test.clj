;; copyright (c) 2019-2024 Sean Corfield, all rights reserved

(ns usermanager.model.user-manager-test
  "These tests use XTDB in-process."
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [com.stuartsierra.component :as component]
            [usermanager.model.user-manager :as model]
            [xtdb.node :as xtn]))

(def ^:private test-db (atom nil))

(def ^:private test-pg-port 5433)

(defn- with-test-db
  "A test fixture that sets up an in-process XTDB node for running tests."
  [t]
  (with-open [_ (xtn/start-node {:pgwire-server {:port test-pg-port}})]
    (let [db (component/start
              (model/map->Database {:db-spec {:dbtype "postgresql"
                                              :host "localhost"
                                              :port test-pg-port}}))]
      (reset! test-db db)
      (t)
      (component/stop db))))

(use-fixtures :once with-test-db)

(deftest department-test
  (is (= {:_id 1 :name "Accounting"}
         (model/get-department-by-id @test-db 1)))
  (is (= 4 (count (model/get-departments @test-db)))))

(deftest user-test
  (let [id (:_id (first (model/get-users @test-db)))]
    (is (= id (:_id (model/get-user-by-id @test-db id))))
    (is (= "Sean" (:first_name
                   (model/get-user-by-id @test-db id))))
    (is (= 4 (:department_id
              (model/get-user-by-id @test-db id))))
    (is (= 1 (count (model/get-users @test-db))))
    (is (= "Development" (:name
                          (first
                           (model/get-users @test-db)))))))

(deftest save-test
  (let [id (:_id (first (model/get-users @test-db)))]
    (is (= "sean@corfield.org"
           (:email
            (do ; update
              (model/save-user @test-db {:_id id :email "sean@corfield.org"})
              (model/get-user-by-id @test-db id)))))
    (is (= 1 (count (model/get-users @test-db))))
    (model/save-user @test-db {:first_name "Sean"
                               :last_name "Corfield"
                               :department_id 4
                               :email "seancorfield@hotmail.com"})
    ;; insert:
    (is (= 2 (count (model/get-users @test-db))))
    (is (= "seancorfield@hotmail.com"
           (:email
            (let [new-user (first (filter #(not= id (:_id %))
                                          (model/get-users @test-db)))]
              (model/get-user-by-id @test-db (:_id new-user))))))))
