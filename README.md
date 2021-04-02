# Example Web Application in Clojure

This is a simple web application using [Component](https://github.com/stuartsierra/component), [Ring](https://github.com/ring-clojure/ring), [Compojure](https://github.com/weavejester/compojure), and [Selmer](https://github.com/yogthos/Selmer) connected to a local SQLite database.

On this branch, it has been migrated to the [Polylith](https://polylith.gitbook.io/) architecture:

![Workspace](/images/workspace.png "Workspace")

Step 1 was:

* The entire application was moved to `bases/web` without any renaming,
* `workspace.edn` was added,
* A development `deps.edn` file was added at the root,
* The application can be built in `projects/usermanager`,
* The application can be run in `:dev` mode,
* The tests can all be run via the `poly` tool.

Step 2:

* The application was split into `bases/web` and `components/usermanager`, namespaces were updated to reflect the full split (`usermanager.web.main` and `usermanager.usermanager.api` were the two entry points; the old `usermanager.model.user-manager` became `usermanager.usermanager.model` to implement the `api`).

Step 3 (several commits):

* In preparation for adding more `components` as I refactored the code, I switched back to the more standard `interface` naming convention from `api` (which worked for the somewhat monolithic component identified in Step 2). So `usermanager.usermanager.interface` became the main component entry point and `usermanager.usermanager.model` implemented that `interface`.
* Then I refactored the monolithic `usermanager` component into `app-state`, `database`, `department`, `schema`, `schema-fixture` (test-only), `user`, and `web-server` components.
* `schema` has a subinterface for each table, so that new tables can be added and they are created and populated on the first run of the application without affecting previously created tables.
* You still start the application with `clojure -M:dev -m usermanager.web.main` (with an optional port number).
* You still build the uberjar with `(cd projects/usermanager && clojure -X:uberjar)`
* You still run the uberjar with `java -jar projects/usermanager/usermanager.jar` (with an optional port number).
* While `clojure -M:poly test :dev` works, `clojure -M:poly test :project` does not -- _awaiting a bug fix in the `poly` test runner._

> Note: my "Step 1" above is a combination of what the Polylith documentations refers to as steps 1 (create the empty workspace) and 2 (move the legacy app into a base) -- so "Step 2" above is Polylith's step 3, and "Step 3" is multiple iterations of Polylith's step 4. Nothing confusing about that, eh?

Clojure beginners often ask for a "complete" example that they can look at to see how these common libraries fit together and for a long time I pointed them at the User Manager example in the Framework One for Clojure repo -- but since I EOL'd that framework and I'd already rewritten the example app to no longer use the framework, it's just confusing to point them there, so this is a self-contained repo containing just that web app example.

A variant of the non-Polylith version of this example application, using [Integrant](https://github.com/weavejester/integrant) and [Reitit](https://github.com/metosin/reitit) (instead of Component and Compojure), can be found in [Michaël Salihi's repo](https://github.com/PrestanceDesign/usermanager-reitit-integrant-example).

## Requirements

This example assumes that you have a recent version of the [Clojure CLI](https://clojure.org/guides/deps_and_cli) installed (at least 1.10.1.727), and provides a `deps.edn` file.

Clojure 1.10 (or later) is required. The "model" of this example app uses namespace-qualified keys in hash maps. It uses [next.jdbc](https://cljdoc.org/d/seancorfield/next.jdbc) -- the "next generation" JDBC library for Clojure -- which produces namespace-qualified hash maps from result sets.

## Usage

Clone the repo, `cd` into it, then see below to run the application (from the command-line or in a REPL), run the tests, build an uberjar, and run the uberjar.

### Run the Application

You can run the application from the root of the workspace:

```
clojure -M:dev -m usermanager.web.main
```

It should create a SQLite database (`usermanager_db`) and populate two tables (`department` and `addressbook`) and start a Jetty instance on port 8080.

If that port is in use, start it on a different port. For example, port 8100:

```
clojure -M:dev -m usermanager.web.main 8100
```

### Run the Application in a REPL

Start REPL

```
$ clj -M:dev
```

Once REPL starts, start the server on port 8888, for example:

```clj
user=> (require 'usermanager.web.main)                           ; load the code
user=> (in-ns 'usermanager.web.main)                             ; move to the namesapce
usermanager.web.main=> (def system (new-system 8888))            ; specify port
usermanager.web.main=> (alter-var-root #'system component/start) ; start the server
```

When you exit the REPL, the server will shutdown. You can shut it down in the REPL like this:

```clj
usermanager.web.main=> (alter-var-root #'system component/stop)   ; stop the server
```

### Run the Tests

You can run all the tests via Polylith's `poly` tool:

```
clojure -M:poly test :all :dev
```

_Normally you would just use `clojure -M:poly test` or `clojure -M:poly test :dev` to run tests that depend on code that has changed since your last stable commit (see the [Polylith documentation](https://polylith.gitbook.io/) for more details)._

### Build and Run an Uberjar

To build a compiled, runnable JAR file:

```
(cd projects/usermanager && clojure -X:uberjar)
```

This uses [`depstar`](https://github.com/seancorfield/depstar) under the hood to AOT-compile `usermanager.web.main` and build `usermanager.jar` in the `projects/usermanager` folder.

To run that uberjar:

```
java -jar projects/usermanager/usermanager.jar
```

As with running the application from the Clojure CLI or the REPL above, this should create a SQLite database (`usermanager_db`) and populate two tables (`department` and `addressbook`) and start a Jetty instance on port 8080.

If that port is in use, start it on a different port. For example, port 8100:

```
java -jar projects/usermanager/usermanager.jar 8100
```

Stop the application with ctrl-C (`^C`) on Linux/macOS or ctrl-Z (`^Z`) on Windows.

# License & Copyright

Copyright (c) 2015-2021 Sean Corfield.

Distributed under the Apache Source License 2.0.
