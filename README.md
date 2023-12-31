# Example Web Application in Clojure

This is a simple web application using [Component](https://github.com/stuartsierra/component), [Ring](https://github.com/ring-clojure/ring), [Compojure](https://github.com/weavejester/compojure), and [Selmer](https://github.com/yogthos/Selmer) connected to [XTDB 2](https://xtdb.com) for the database.

Clojure beginners often ask for a "complete" web application example that they can look at to see how these common libraries fit together. Several variants of this example now exist: the original version on the [`develop` branch](https://github.com/seancorfield/usermanager-example/tree/develop), which links to a Polylith version and an Integrant/reitit version (instead of Component/Compojure). This is the XTDB version.

## Requirements

This example assumes that you have a recent version of the [Clojure CLI](https://clojure.org/guides/deps_and_cli) installed (at least 1.10.3.933), and provides a `deps.edn` file, and a `build.clj` file.

Clojure 1.10 (or later) is required. It uses [XTDB 2](https://xtdb.com) (early access) via [next.jdbc](https://cljdoc.org/d/seancorfield/next.jdbc) and [next.jdbc.xt](https://github.com/seancorfield/next.jdbc.xt).

You'll need [Docker](https://docker.com) installed in order to run an instance of XTDB locally. If you have a remote XTDB instance available, you can edit the `setup-database`function in `src/usermanager/model/user-manager.clj` to point to that instead.

## Usage

Clone the repo, `cd` into it, then follow below to _Run the Application_ or _Run the application in REPL_
or _Run the tests_ or _Build an Uberjar_.

### Run the Application

Use Docker to get a local copy of the most recent XTDB 2 early access release:

```
docker pull ghcr.io/xtdb/xtdb-standalone-ea
```

Then use Docker to run XTDB locally (this will keep this terminal window busy, so open a new terminal window to run the application):

```
docker run -tip 3000:3000 ghcr.io/xtdb/xtdb-standalone-ea
```

In a separate terminal:

```
clojure -M -m usermanager.main
```

It should populate two tables (`department` and `addressbook`) in the XTDB instance and start a Jetty instance on port 8080.

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
user=> (in-ns 'usermanager.main)                               ; move to the namespace
usermanager.main=> (def system (new-system 8888))              ; specify port
usermanager.main=> (alter-var-root #'system component/start)   ; start the server
```

### Run the tests with:

The tests use an in-process XTDB node so you don't need to run Docker for this.

```
clojure -T:build test
```

You should see something like this:

```
Running task for: test

Running tests in #{"test"}
SLF4J: No SLF4J providers were found.
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See https://www.slf4j.org/codes.html#noProviders for further details.

Testing usermanager.model.user-manager-test
Populated database with initial data!

Ran 3 tests containing 11 assertions.
0 failures, 0 errors.
```

This uses the `:build` alias to load the `build.clj` file, based on [`tools.build`](https://clojure.org/guides/tools_build), and run the `test` task.

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
Copying source...

Compiling usermanager.main...
SLF4J: No SLF4J providers were found.
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See https://www.slf4j.org/codes.html#noProviders for further details.

Building JAR...
```

The `target` folder will be created if it doesn't exist and it will include a `classes` folder containing all of the compiled Clojure source code from the `usermanager` application _and all of its dependencies_ including Clojure itself:

```
$ ls target/classes/
camel_snake_kebab  clout      com        crypto      juxt     medley  public  selmer         usermanager  xtdb
clojure            cognitect  compojure  instaparse  layouts  next    ring    time_literals  views
```

It will also include the standalone `.jar` file which you can run like this:

```
java -jar target/usermanager/example-standalone.jar
```

This should behave the same as the _Run the Application_ example above.

This JAR file can be deployed to any server that have Java installed and run with no other external dependencies or files.

## Stuff I Need To Do

* I might add a `datafy`/`nav` example.

# License & Copyright

Copyright (c) 2015-2023 Sean Corfield.

Distributed under the Apache Source License 2.0.
