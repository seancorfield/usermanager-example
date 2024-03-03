(ns usermanager.web-server.core
  (:require [com.stuartsierra.component :as component]
            [ring.adapter.jetty :refer [run-jetty]]))

;; Standard web server component -- knows how to stop and start the
;; web server (with the application component as a dependency, and
;; the handler function as a parameter):
(defrecord WebServer [handler-fn server port ; parameters
                      application            ; dependencies
                      http-server shutdown]  ; state
  component/Lifecycle
  (start [this]
         ;; it's important for your components to be idempotent: if you start
         ;; them more than once, only the first call to start should do anything
         ;; and subsequent calls should be an no-op -- the same applies to the
         ;; stop calls: only stop the system if it is running, else do nothing
         (if http-server
           this
           (assoc this
                  ;; start a Jetty web server -- use :join? false
                  ;; so that it does not block (we use a promise
                  ;; to block on in -main):
                  :http-server (run-jetty (handler-fn application)
                                          {:port port :join? false})
                  ;; this promise exists primarily so -main can
                  ;; wait on something, since we start the web
                  ;; server in a non-blocking way:
                  :shutdown (promise))))
  (stop  [this]
         (if http-server
           (do
             ;; shutdown Jetty: call .stop on the server object:
             (.stop http-server)
             ;; deliver the promise to indicate shutdown (this is
             ;; really just good housekeeping, since you're only
             ;; going to call stop via the REPL when you are not
             ;; waiting on the promise):
             (deliver shutdown true)
             (assoc this :http-server nil))
           this)))

(defn create
  "Return a WebServer component that depends on the application.

  The handler-fn is a function that accepts the application (Component) and
  returns a fully configured Ring handler (with middeware)."
  [handler-fn port]
  (component/using (map->WebServer {:handler-fn handler-fn
                                    :port port})
                   [:application]))
