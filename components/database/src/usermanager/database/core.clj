(ns usermanager.database.core
  (:require [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]))

(defrecord Database [db-spec     ; configuration
                     init-fn     ; callback to initialize the database
                     datasource] ; state

  component/Lifecycle
  (start [this]
    (if datasource
      this ; already initialized
      (let [database (assoc this :datasource (jdbc/get-datasource db-spec))]
        ;; set up database if necessary
        (init-fn database (:dbtype db-spec))
        database)))
  (stop [this]
    (assoc this :datasource nil))

  ;; allow the Database component to be "called" with no arguments
  ;; to produce the underlying datasource object
  clojure.lang.IFn
  (invoke [_] datasource))

(defn create
  [schema]
  (map->Database schema))
