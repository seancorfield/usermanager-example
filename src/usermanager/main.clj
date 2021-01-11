;; copyright (c) 2019-2021 Sean Corfield, all rights reserved

(ns usermanager.main
  "This is an example web application, using just a few basic Clojure
  libraries: Ring, Compojure, Component, Selmer, and next.jdbc.

  I recommend this as a good way to get started building web applications
  in Clojure so that you understand the basic moving parts in any web app.

  Ring is pretty much the fundamental building block of all web apps
  in Clojure. It provides an abstraction that maps HTTP requests to
  simple Clojure hash maps. Your handler processes those hash maps
  and produces another hash map containing :status and :body that
  Ring turns into an HTTP response.

  Compojure is the most widely used routing library. It lets you
  define mappings from URL patterns -- routes -- to handler functions.

  Selmer is a templating library that lets you write your web pages
  as HTML templates that follow the Django style of simple variable
  substitution, conditionals, and loops. Another popular approach
  for building web pages is Hiccup, which takes Clojure data structures
  and transforms them to HTML. If you need designers to deal with your
  HTML templates, Selmer is going to be a lot easier for them to work with.

  next.jdbc is the next generation JDBC library for Clojure, replacing
  clojure.java.jdbc. It provides a fast, idiomatic wrapper around the
  complexity that is Java's JDBC class hierarchy.

  This example uses a local SQLite database to store data."
  (:require [com.stuartsierra.component :as component]
            [compojure.coercions :refer [as-int]]
            [compojure.core :refer [GET POST let-routes]]
            [compojure.route :as route]
            ;; we use Jetty by default but if you want to use
            ;; http-kit instead, uncomment this line...
            ;; [org.httpkit.server :refer [run-server]]
            ;; ...and comment out this Jetty line:
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :as ring-defaults]
            [ring.util.response :as resp]
            [usermanager.controllers.user :as user-ctl]
            [usermanager.model.user-manager :as model]))

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

(defn my-application
  "Return your application component, fully configured.

  In this simple case, we just pass the whole configuration into
  the application (a hash map containing a :repl flag).

  The application depends on the database (which is created in
  new-system below and automatically passed into Application by
  Component itself, before calling start)."
  [config]
  (component/using (map->Application {:config config})
                   [:database]))

(defn my-middleware
  "This middleware runs for every request and can execute before/after logic.

  If the handler returns an HTTP response (like a redirect), we're done.
  Else we use the result of the handler to render an HTML page."
  [handler]
  (fn [req]
    (let [resp (handler req)]
      (if (resp/response? resp)
        resp
        (user-ctl/render-page resp)))))

;; Helper for building the middleware:
(defn- add-app-component
  "Middleware to add your application component into the request. Use
  the same qualified keyword in your controller to retrieve it."
  [handler application]
  (fn [req]
    (handler (assoc req :application/component application))))

;; This is Ring-specific, the specific stack of middleware you need for your
;; application. This example uses a fairly standard stack of Ring middleware
;; with some tweaks for convenience
(defn middleware-stack
  "Given the application component and middleware, return a standard stack of
  Ring middleware for a web application."
  [app-component app-middleware]
  (fn [handler]
    (-> handler
        (app-middleware)
        (add-app-component app-component)
        (ring-defaults/wrap-defaults (-> ring-defaults/site-defaults
                                         ;; disable XSRF for now
                                         (assoc-in [:security :anti-forgery] false)
                                         ;; support load balancers
                                         (assoc-in [:proxy] true))))))

;; This is the main web handler, that builds routing middleware
;; from the application component (defined above). The handler is passed
;; into the web server component (below).
;; Note that Vars are used -- the #' notation -- instead of bare symbols
;; to make REPL-driven development easier. See the following for details:
;; https://clojure.org/guides/repl/enhancing_your_repl_workflow#writing-repl-friendly-programs
(defn my-handler
  "Given the application component, return middleware for routing.

  We use let-routes here rather than the more usual defroutes because
  Compojure assumes that if there's a match on the route, the entire
  request will be handled by the function specified for that route.

  Since we need to deal with page rendering after the handler runs,
  and we need to pass in the application component at start up, we
  need to define our route handlers so that they can be parameterized."
  [application]
  (let-routes [wrap (middleware-stack application #'my-middleware)]
    (GET  "/"                        []              (wrap #'user-ctl/default))
    ;; horrible: application should POST to this URL!
    (GET  "/user/delete/:id{[0-9]+}" [id :<< as-int] (wrap #'user-ctl/delete-by-id))
    ;; add a new user:
    (GET  "/user/form"               []              (wrap #'user-ctl/edit))
    ;; edit an existing user:
    (GET  "/user/form/:id{[0-9]+}"   [id :<< as-int] (wrap #'user-ctl/edit))
    (GET  "/user/list"               []              (wrap #'user-ctl/get-users))
    (POST "/user/save"               []              (wrap #'user-ctl/save))
    ;; this just resets the change tracker but really should be a POST :)
    (GET  "/reset"                   []              (wrap #'user-ctl/reset-changes))
    (route/resources "/")
    (route/not-found "Not Found")))

;; Standard web server component -- knows how to stop and start your chosen
;; web server... uses Jetty but explains how to use http-kit instead:
;; lifecycle for the specified web server in which we run
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
                  ;; to block on in -main).
                  ;; to start an http-kit web server instead:
                  ;; 1. call run-server instead of run-jetty
                  ;; 2. omit :join? false since http-kit does
                  ;; not block when it starts
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
             ;; shutdown http-kit: invoke the server (as a function):
             ;; (http-server)
             ;; deliver the promise to indicate shutdown (this is
             ;; really just good housekeeping, since you're only
             ;; going to call stop via the REPL when you are not
             ;; waiting on the promise):
             (deliver shutdown true)
             (assoc this :http-server nil))
           this)))

(defn web-server
  "Return a WebServer component that depends on the application.

  The handler-fn is a function that accepts the application (Component) and
  returns a fully configured Ring handler (with middeware)."
  [handler-fn port]
  (component/using (map->WebServer {:handler-fn handler-fn
                                    :port port})
                   [:application]))

;; This is the piece that combines the generic web server component above with
;; your application-specific component defined at the top of the file, and
;; any dependencies your application has (in this case, the database):
;; Note that a Var is used -- the #' notation -- instead of a bare symbol
;; to make REPL-driven development easier. See the following for details:
;; https://clojure.org/guides/repl/enhancing_your_repl_workflow#writing-repl-friendly-programs
(defn new-system
  "Build a default system to run. In the REPL:

  (def system (new-system 8888))

  (alter-var-root #'system component/start)

  (alter-var-root #'system component/stop)

  See the Rich Comment Form below."
  ([port] (new-system port true))
  ([port repl]
   (component/system-map :application (my-application {:repl repl})
                         :database    (model/setup-database)
                         :web-server  (web-server #'my-handler port))))

(comment
  (def system (new-system 8888))
  (alter-var-root #'system component/start)
  (alter-var-root #'system component/stop)
  ;; the comma here just "anchors" the closing paren on this line,
  ;; which makes it easier to put you cursor at the end of the lines
  ;; above when you want to evaluate them into the REPL:
  ,)

(defonce ^:private
  ^{:doc "This exists so that if you run a socket REPL when
  you start the application, you can get at the running
  system easily.

  Assuming a socket REPL running on 50505:

  nc localhost 50505
  user=> (require 'usermanager.main)
  nil
  user=> (in-ns 'usermanager.main)
  ...
  usermanager.main=> (require '[next.jdbc :as jdbc])
  nil
  usermanager.main=> (def db (-> repl-system deref :application :database))
  #'usermanager.main/db
  usermanager.main=> (jdbc/execute! (db) [\"select * from addressbook\"])
  [#:addressbook{:id 1, :first_name \"Sean\", :last_name \"Corfield\", :email \"sean@worldsingles.com\", :department_id 4}]
  usermanager.main=>"}
  repl-system
  (atom nil))

(defn -main
  [& [port]]
  (let [port (or port (get (System/getenv) "PORT" 8080))
        port (cond-> port (string? port) Integer/parseInt)]
    (println "Starting up on port" port)
    ;; start the web server and application:
    (-> (component/start (new-system port false))
        ;; then put it into the atom so we can get at it from a REPL
        ;; connected to this application:
        (->> (reset! repl-system))
        ;; then wait "forever" on the promise created:
        :web-server :shutdown deref)))
