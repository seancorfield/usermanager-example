;; copyright (c) 2019-2021 Sean Corfield, all rights reserved

(ns usermanager.user.core-test
  "These tests use H2 in-memory."
  (:require [clojure.test :as test :refer [deftest is testing]]
            [usermanager.usermanager.model :as sut]))
