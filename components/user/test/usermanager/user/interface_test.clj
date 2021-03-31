;; copyright (c) 2019-2021 Sean Corfield, all rights reserved

(ns usermanager.user.interface-test
  "These tests use H2 in-memory."
  (:require [clojure.test :as test :refer [deftest is testing]]
            [usermanager.department.interface :as department]
            [usermanager.test-helper.interface :as test-helper]
            [usermanager.user.interface :as user]))

(test/use-fixtures :once test-helper/with-test-db)

(deftest department-test
  (is (= #:department{:id 1 :name "Accounting"}
         (department/get-by-id @test-db 1)))
  (is (= 4 (count (department/get-departments @test-db)))))

(deftest user-test
  (is (= 1 (:addressbook/id (user/get-by-id @test-db 1))))
  (is (= "Sean" (:addressbook/first_name
                 (user/get-by-id @test-db 1))))
  (is (= 4 (:addressbook/department_id
            (user/get-by-id @test-db 1))))
  (is (= 1 (count (user/get-users @test-db))))
  (is (= "Development" (:department/name
                        (first
                          (user/get-users @test-db))))))

(deftest save-test
  (is (= "sean@corfield.org"
         (:addressbook/email
          (do
            (user/save @test-db {:addressbook/id         1
                                 :addressbook/email "sean@corfield.org"})
            (user/get-by-id @test-db 1)))))
  (is (= "seancorfield@hotmail.com"
         (:addressbook/email
          (do
            (user/save @test-db {:addressbook/first_name         "Sean"
                                 :addressbook/last_name     "Corfield"
                                 :addressbook/department_id 4
                                 :addressbook/email         "seancorfield@hotmail.com"})
            (user/get-by-id @test-db 2))))))
