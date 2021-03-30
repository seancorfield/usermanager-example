(ns usermanager.app-state.interface
  (:require [usermanager.app-state.core :as app-state]))

(defn create
  "Return your application component, fully configured.

  In this simple case, we just pass the whole configuration into
  the application (a hash map containing a :repl flag).

  The application depends on the database (which is created in
  new-system below and automatically passed into Application by
  Component itself, before calling start)."
  [config]
  (app-state/create config))