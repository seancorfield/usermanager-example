# Example Web Application in Clojure

This is a simple web application using [Component](https://github.com/stuartsierra/component), [Ring](https://github.com/ring-clojure/ring), [Compojure](https://github.com/weavejester/compojure), and [Selmer](https://github.com/yogthos/Selmer) connected to a local SQLite database.

Clojure beginners often ask for a "complete" example that they can look at to see how these common libraries fit together and for a long time I pointed them at the User Manager example in the Framework One for Clojure repo -- but since I EOL'd that framework and I'd already rewritten the example app to no longer use the framework, it's just confusing to point them there, so this is a self-contained repo containing just that web app example.

A variant using [Integrant](https://github.com/weavejester/integrant) and [Reitit](https://github.com/metosin/reitit) (instead of Component and Compojure), inspired by this example repo, can be found in [Michaël Salihi's repo](https://github.com/PrestanceDesign/usermanager-reitit-integrant-example).

A version of this application that uses the [Polylith architecture](https://polylith.gitbook.io/) is also available, on the [`polylith` branch](https://github.com/seancorfield/usermanager-example/tree/polylith).

## Requirements

This example assumes that you have a recent version of the [Clojure CLI](https://clojure.org/guides/deps_and_cli) installed (at least 1.10.3.933), and provides a `deps.edn` file, and a `build.clj` file.

Clojure 1.10 (or later) is required. The "model" of this example app uses namespace-qualified keys in hash maps. It uses [next.jdbc](https://cljdoc.org/d/seancorfield/next.jdbc) -- the "next generation" JDBC library for Clojure -- which produces namespace-qualified hash maps from result sets.

## Usage

Clone the repo, `cd` into it, then follow below to _Run the Application_ or _Run the application in REPL_
or _Run the tests_ or _Build an Uberjar_.

### Run the Application
```
clojure -M -m usermanager.main
```

It should create a SQLite database (`usermanager_db`) and populate two tables (`department` and `addressbook`) and start a Jetty instance on port 8080.

If that port is in use, start it on a different port. For example, port 8100:

```
clojure -M -m usermanager.main 8100
```

### Run the Application in REPL

Start REPL

```
$ clj
```

Once REPL starts, start the server as an example on port 8888:

```clj
user=> (require 'usermanager.main)                             ; load the code
user=> (in-ns 'usermanager.main)                               ; move to the namesapce
usermanager.main=> (def system (new-system 8888))              ; specify port
usermanager.main=> (alter-var-root #'system component/start)   ; start the server
```

### Run the tests with:

```
clojure -T:build test
```

You should see something like this:

```
Running task for: test

Running tests in #{"test"}
2022-05-25 18:19:45.138:INFO::main: Logging initialized @6494ms to org.eclipse.jetty.util.log.StdErrLog

Testing usermanager.model.user-manager-test
Created database and addressbook table!
Populated database with initial data!

Ran 3 tests containing 9 assertions.
0 failures, 0 errors.
```

This uses the `:build` alias to load the `build.clj` file, based on [`tools.build`](https://clojure.org/guides/tools_build), and run the `test` task.
That in turn runs the `run-tests` task from my [`build-clj`](https://github.com/seancorfield/build-clj) wrapper for `tools.build`, that provide "sane" defaults for the myriad options in `tools.build` so you can write simpler `build.clj` files.

## Build an Uberjar

For production deployment, you typically want to build an "uberjar" -- a `.jar` file that contains Clojure itself and all of the code from your application and its dependencies, so that you can run it with the `java -jar` command.

The `build.clj` file -- mentioned above -- contains a `ci` task that:

* runs all the tests
* cleans up the `target` folder
* compiles the application (sometimes called "AOT compilation")
* produces a standalone `.jar` file

```
clojure -T:build ci
```

That should produce the same output as `test` above, followed by something like:

```
Cleaning target...

Skipping pom.xml because :lib and/or :version were omitted...
Copying src, resources...
Compiling usermanager.main...
2022-05-25 18:20:13.069:INFO::main: Logging initialized @3981ms to org.eclipse.jetty.util.log.StdErrLog
Building uberjar target/example-standalone.jar...
```

The `target` folder will be created if it doesn't exist and it will include a `classes` folder containing all of the compiled Clojure source code from the `usermanager` application _and all of its dependencies_ including Clojure itself:

```
$ ls target/classes/
cheshire  clojure  clout  com  compojure  crypto  instaparse  json_html  layouts  medley  next  public  ring  selmer  usermanager  views
```

It will also include the standalone `.jar` file which you can run like this:

```
java -jar target/example-standalone.jar
```

This should behave the same as the _Run the Application_ example above.

This JAR file can be deployed to any server that have Java installed and run with no other external dependencies or files.

## Stuff I Need To Do

* I might add a `datafy`/`nav` example.

# License & Copyright

Copyright (c) 2015-2022 Sean Corfield.

Distributed under the Apache Source License 2.0.
