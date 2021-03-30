(ns usermanager.app-state.core
  (:require [com.stuartsierra.component :as component]))

;; Implement your application's lifecycle here:
;; Although the application config is not used in this simple
;; case, it probably would be in the general case -- and the
;; application state here is trivial but could be more complex.
(defrecord Application [config   ; configuration (unused)
                        database ; dependency
                        state]   ; behavior
  component/Lifecycle
  (start [this]
    ;; Component ensures that dependencies are fully initialized and
    ;; started before invoking this component.
    (assoc this :state "Running"))
  (stop  [this]
    (assoc this :state "Stopped")))

(defn create
  "Return your application component, fully configured.

  In this simple case, we just pass the whole configuration into
  the application (a hash map containing a :repl flag).

  The application depends on the database (which is created in
  new-system below and automatically passed into Application by
  Component itself, before calling start)."
  [config]
  (component/using (map->Application {:config config})
                   [:database]))
