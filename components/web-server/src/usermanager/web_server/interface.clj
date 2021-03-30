(ns usermanager.web-server.interface
  (:require [usermanager.web-server.core :as web-server]))

(defn create
  "Return a WebServer component that depends on the application.

  The handler-fn is a function that accepts the application (Component) and
  returns a fully configured Ring handler (with middeware)."
  [handler-fn port]
  (web-server/create handler-fn port))
