(ns build
  (:refer-clojure :exclude [test])
  (:require [org.corfield.build :as bb]))

(def lib 'usermanager/example)
(def main 'usermanager.main)

(defn test "Run the tests" [opts]
  (-> opts
      (bb/run-tests)))

(defn ci "Run the CI pipeline of tests (and build the uberjar)." [opts]
  (-> opts
      (assoc :lib lib :main main)
      (bb/run-tests)
      (bb/clean)
      (bb/uber)))
