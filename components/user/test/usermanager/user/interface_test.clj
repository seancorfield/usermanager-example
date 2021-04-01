;; copyright (c) 2019-2021 Sean Corfield, all rights reserved

(ns usermanager.user.interface-test
  (:require [clojure.test :as test :refer [deftest is testing]]
            [usermanager.user.interface :as sut]
            [usermanager.schema-fixture.interface :as fixture]))

(test/use-fixtures :once fixture/with-test-db)

(deftest user-test
  (is (= 1 (:addressbook/id (sut/get-user-by-id (fixture/test-db) 1))))
  (is (= "Sean" (:addressbook/first_name
                 (sut/get-user-by-id (fixture/test-db) 1))))
  (is (= 4 (:addressbook/department_id
            (sut/get-user-by-id (fixture/test-db) 1))))
  (is (= 1 (count (sut/get-users (fixture/test-db)))))
  (is (= "Development" (:department/name
                        (first
                         (sut/get-users (fixture/test-db)))))))

(deftest save-test
  (is (= "sean@corfield.org"
         (:addressbook/email
          (do
            (sut/save-user (fixture/test-db) {:addressbook/id 1
                                              :addressbook/email "sean@corfield.org"})
            (sut/get-user-by-id (fixture/test-db) 1)))))
  (is (= "seancorfield@hotmail.com"
         (:addressbook/email
          (do
            (sut/save-user (fixture/test-db) {:addressbook/first_name "Sean"
                                              :addressbook/last_name "Corfield"
                                              :addressbook/department_id 4
                                              :addressbook/email "seancorfield@hotmail.com"})
            (sut/get-user-by-id (fixture/test-db) 2))))))