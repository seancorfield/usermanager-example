;; copyright (c) 2019-2023 Sean Corfield, all rights reserved

(ns usermanager.department.interface-test
  (:require [clojure.test :as test :refer [deftest is testing]]
            [usermanager.department.interface :as sut]
            [usermanager.schema-fixture.interface :as fixture]))

(test/use-fixtures :once fixture/with-test-db)

(deftest department-test
  (is (= #:department{:id 1 :name "Accounting"}
         (sut/get-by-id (fixture/test-db) 1)))
  (is (= 4 (count (sut/get-all (fixture/test-db))))))
