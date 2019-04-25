FW/1 in Clojure [![Join the chat at https://gitter.im/framework-one/fw1-clj](https://badges.gitter.im/framework-one/fw1-clj.svg)](https://gitter.im/framework-one/fw1-clj?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
===============

This was an interesting thought experiment based on my initial thinking that Clojure needed a very simple, convention-based web framework, like Framework One (FW/1) from the CFML world. I thought it would be a gentle on-ramp for CFML developers who were interested in trying Clojure -- and that part was successful in that it piqued the interest of a number of them to build toy applications with FW/1 for Clojure.

At World Singles, we started using FW/1 for Clojure in 2016, as a migration path from our FW/1 for CFML applications. For those applications, I found I either needed explicit Compojure routes (for our REST APIs) or a more flexible Ring middleware stack, or both. The convention-based views and cascading layouts just weren't useful in the real world programs we were building. In December 2016, I started to [think about refactoring FW/1 to more standard Ring middleware](https://github.com/framework-one/fw1-clj/blob/master/RING.md) and as I started the actual refactoring, I realized that FW/1 really added very little in terms of convenience but a fair bit in terms of non-standard behavior (controllers were not quite regular Ring handlers, CORS handling was custom, non-HTML responses were complex and custom, and HTML responses were full of magic).

By mimicking FW/1 for CFML, I'd lost a lot of the simplicity and elegance that web applications in Clojure can exhibit. That's why I'm sunsetting FW/1 for Clojure before it gets any traction. "So long, and thanks for all the fish!", as they say.

I've converted the [user manager example](https://github.com/framework-one/fw1-clj/tree/master/examples/usermanager) to no longer need FW/1 at all. Yes, there's a little bit more boilerplate in `main.clj` around creating and starting the web server, and it could easily be streamlined if you only wanted to use Jetty (or only http-kit). Yes, you now have to specify the routes explicitly instead of the magic that was there before -- but in any reasonable web application you will want that level of control over your "public API" (the URLs) and you will want to keep it decoupled from your handlers. Yes, you have to deal with the views and layouts yourself, but as you can see in [the `usermanager` controller's `after` function](https://github.com/framework-one/fw1-clj/blob/master/examples/usermanager/controllers/user.clj#L17-L24), it's just a few lines of simple code to deal with this. With those small trade-offs, you get the benefits of standard Ring handlers, composable middleware, and the freedom to choose how you start/stop your applications and how you deal with HTML templates. You can swap out Compojure for Bidi or something else, you can swap out Selmer for something else. If you're building a REST API, you can drop Selmer and rely on ring-json to turn your responses into JSON for you. The world becomes your Clojure-powered oyster!

Porting FW/1 from CFML to Clojure taught me a lot about the Ring ecosystem and also about Clojure's overall preference for simplicity, elegance, and composability. Time to move on.

The Original README
===================

This was based on a port from CFML to Clojure of Framework One (FW/1). Most concepts carry over but, like Clojure itself, the emphasis is on simplicity.

FW/1 in Clojure is based on [Ring](https://github.com/ring-clojure/ring), [Compojure](https://github.com/weavejester/compojure), and [Selmer](https://github.com/yogthos/Selmer).
FW/1 is a lightweight, (partially) convention-based MVC framework.

The easiest way to get started with FW/1 is to use the
[fw1-template](https://github.com/framework-one/fw1-template) template
for Boot. The template can create a basic FW/1 skeleton
project for you that "just works" and provides the directory structure
and some basic files for you to get started with.

Assuming you have [Boot](http://boot-clj.com) installed, you can create a new skeleton FW/1 app like this:

    boot -d seancorfield/boot-new new -t fw1 -n myfw1app

This will create a skeleton FW/1 app in the `myfw1app` folder. You can run it like this:

    cd myfw1app
    boot run -p 8111

If you omit the `-p` / `--port` argument, it will default to port 8080, unless overridden by an environment variable:

    PORT=8111 boot run

URL Structure
-------------

In a FW/1 application, Controller functions and Views are automatically located based on standard patterns - with site sections and items within each section. Layouts are applied, if provided, in a cascade from item to section to site. You specify the site and item as a namespaced keyword `:section/item` and FW/1 will locate Controllers, Views, and Layouts based on that.

Actual URL route processing is handled via Compojure and FW/1 provides a default set of routes that should serve most purposes. The `usermanager` example leverages that default in the `fw1-handler` function:

``` clojure
(defn fw1-handler
  "Build the FW/1 handler from the application. This is where you can
  specify the FW/1 configuration and the application routes."
  [application]
  (fw1/default-handler application
                       {:application-key "usermanager"
                        :home            "user.default"}))
```

The default handler behavior is equivalent to this:

``` clojure
(defn fw1-handler
  "Build the FW/1 handler from the application. This is where you can
  specify the FW/1 configuration and the application routes."
  [application]
  (let-routes [fw1 (fw1/configure-router {:application     application
                                          :application-key "usermanager"
                                          :home            "user.default"})]
    (route/resources "/")
    (ANY "/" [] (fw1))
    (context "/:section" [section]
             (ANY "/"                  []     (fw1 (keyword section)))
             (ANY "/:item"             [item] (fw1 (keyword section item)))
             (ANY "/:item/:id{[0-9]+}" [item] (fw1 (keyword section item))))
    (route/not-found "Not Found"))
```

As above, the handler is initialized with an application Component. It obtains a router from FW/1 by providing configuration for FW/1. It then defines routes using Compojure, starting with a general `resources` route, followed by a few standard route patterns that map to `:section/item` keywords.

Project Structure
-----------------

The standard file structure for a FW/1 application is:

* `resources/`
  * `public/` - folder containing web-accessible (public) assets
* `src/`
  * `app_key/` - matches the `:application-key` value specified in the configuration above.
    * `controllers/` - contains a `.clj` file for each _section_ that needs business logic.
    * `layouts/` - contains per-_item_, per-_section_ and per-site layouts as needed.
    * `views/` - contains a folder for each _section_, containing an HTML view for each _item_.
    * `main.clj` - the entry point for your application.

Your Model can be anywhere since it will be `require`d into your controller namespaces as needed.

Request Lifecycle
-----------------

Controllers can have _before(rc)_ and _after(rc)_ handler functions that apply to all requests in a _section_.

A URL of `/section/item` will cause FW/1 to call:

* `(controllers.section/before rc)`, if defined.
* `(controllers.section/item rc)`, if defined.
* `(controllers.section/after rc)`, if defined.

A handler function should return the `rc`, updated as necessary. Strictly speaking, FW/1 will also call any `:before` / `:after` handlers defined in the configuration -- see below. This sequence of controller calls will be interrupted if `(abort rc)` has been called.

If one of the `render-xxx` functions has been called, FW/1 will render the data as specified. If the `redirect` function has been called, FW/1 will respond with a redirect (a `Location` header containing a URL). Otherwise FW/1 will look for an HTML view template:

* `views/section/item.html`

The suffix can be controlled by the `:suffix` configuration option but defaults to `"html"`.

FW/1 looks for a cascade of layouts (again, the suffix configurable):

* `layouts/section/item.html`,
 * Replacing `{{body}}` with the view (and not calling any transforms).
* `layouts/section.html`,
 * Replacing `{{body}}` with the view so far.
* `layouts/default.html`,
 * Replacing `{{body}}` with the view so far. The `:layout` configuration is ignored.

Rendering Data
--------------

If a `render-xxx` function is called, FW/1 will return a response that has the given status code (or 200 if none were specified) and the set a `Content-Type` header based on the data type specified for the rendering. The body will be the expression, converted per the data type.

The following content types are built-in:

* `:html` - `text/html; charset=utf-8`
* `:json` - `application/json; charset=utf-8`
* `:raw-json` - `application/json; charset=utf-8`
* `:text` - `text/plain; charset=utf-8`
* `:xml` - `text/xml; charset=utf-8`

By default, `:html`, `:raw-json`, and `:text` render the data as-is. `:json` uses Cheshire's `generate-string` to render the data as a JSON-encoded string, using the `:json-config` settings from the FW/1 configuration if specified. `:xml` uses `clojure.data.xml`'s `sexp-as-element` and then `emit` to render the data as an XML-encoded string.

You can override these in the FW/1 config, via the `:render-types` key. You can also add new data types that way. Each data type is specified by a keyword (as above) and a map with two keys:

* `:type` - the content type string to use (as shown above).
* `:body` - a function that accepts two arguments - the FW/1 configuration and the data to render - and returns a string that represents the converted value of the data.

Convenience functions are provided for the five built-in data types: `render-html`, `render-json`, `render-raw-json`, `render-text`, and `render-xml`. The `render-data` function can be used for custom data types. In particular, `render-data` can be used to specify runtime data rendering:

    (fw1/render-data rc render-fn expr)
    (fw1/render-data rc status render-fn expr)

The `render-fn` should be a function of two arities. When called with no arguments, it should either return a content type string for the rendering, or one of the known data types (including custom ones from the `:render-types` configuration) and that data type's content type string will be used. When called with two arguments - the FW/1 configuration and the data to render - it should return a string that represents the converted value of the data.

As a convenience, you can use the `render-by` function to turn your `render-fn` into a function that behaves like the built-in `render-xxx` data type renderers:

    (def render-custom (fw1/render-by my-render-fn))
    ...
    (render-custom rc expr)

Framework API
-------------

Any controller function also has access to the the FW/1 API (after `require`ing `framework.one`):

* `(abort rc)` - abort the controller lifecycle -- do not apply any more controllers (of the :before "before" item "after" :after lifecycle).
* `(cookie rc name)` - returns the value of `name` from the cookie scope.
* `(cookie rc name value)` - sets `name` to `value` in the cookie scope, and returns the updated `rc`.
* `(event rc name)` - returns the value of `name` from the event scope (`:action`, `:section`, `:item`, or `:config`).
* `(event rc name value)` - sets `name` to `value` in the event scope, and returns the updated `rc`.
* `(flash rc name value)` - sets `name` to `value` in the flash scope, and returns the updated `rc`.
* `(header rc name)` - return the value of the `name` HTTP header, or `nil` if no such header exists.
* `(header rc name value)` - sets the `name` HTTP header to `value` for the response, and returns the updated `rc`.
* `(parameters rc)` - returns just the form / URL parameters from the request context (plus whatever parameters have been added by controllers). This is useful when you want to iterate over the data elements without worrying about any of the 'special' data that FW/1 puts in `rc`.
* `(redirect rc url)` or `(redirect rc status url)` - returns `rc` containing information to indicate a redirect to the specified `url`.
* `(redirecting? rc)` - returns `true` if the current request will redirect, i.e., `(redirect rc ...)` has been called.
* `(reload? rc)` - returns `true` if the current request includes URL parameters to force an application reload.
* `(remote-addr rc)` - returns the IP address of the remote requestor (if available). Checks the `"x-forwarded-for"` header (set by load balancers) then Ring's `:remote-addr` field.
* `(render-by render-fn)` - a convenience to produce a `render-xxx` function. See the last section of **Rendering Data** above for details.
* `(render-data rc type data)` or `(render rc status type data)` - low-level function to tell FW/1 to render the specified `data` as the specified `type`, optionally with the specified `status` code. Prefer the `render-xxx` convenience functions that follow is you are rendering standard data types.
* `(render-xxx rc data)` or `(render-xxx rc status data)` - render the specified `data`, optionally with the specified `status` code, in format _xxx_: `html`, `json`, `raw-json`, `text`, `xml`.
* `(rendering? rc)` - returns `true` if the current request will render data (instead of a page), i.e., `(render-xxx rc ...)` has been called.
* `(ring rc)` - returns the original Ring request.
* `(ring rc req)` - sets the Ring request data. Intended to be used mostly for testing controllers, to make it easier to set up test `rc` data.
* `(servlet-request rc)` - returns a "fake" `HttpServletRequest` object that delegates `getParameter` calls to pull data out of `rc`, as well as implementing several other calls (delegating to the Ring request data); used for interop with other HTTP-centric libraries.
* `(session rc name)` - returns the value of `name` from the session scope.
* `(session rc name value)` - sets `name` to `value` in the session scope, and returns the updated `rc`.
* `(to-long val)` - converts `val` to a long, returning zero if it cannot be converted (values in `rc` come in as strings so this is useful when you need a number instead and zero can be a sentinel for "no value").

The following symbols from Selmer are exposed as aliases via the FW/1 API:

* `add-tag!`, `add-filter!`

Application Startup & Configuration
-----------------------------------

As noted above, you can start the server on port 8080, running the User Manager example if you cloned this repository, with:

    boot run

You can specify a different port like this:

    boot run -p 8111

or:

    PORT=8111 boot run

In your main namespace -- `main.clj` in the example here -- the call to `(fw1/configure-router)` can be passed configuration parameters either
as a map (preferred) or as an arbitrary number of inline key / value pairs (legacy support):

* `:after` - a function (taking / returning `rc`) which will be called after invoking any controller
* `:application-key` - the namespace prefix for the application, default none.
* `:before` - a function (taking / returning `rc`) which will be called before invoking any controller
* `:default-section` - the _section_ used if none is present in the URL, default `"main"`.
* `:default-item` - the _item_ used if none is present in the URL, default `"default"`.
* `:error` - the action - _"section.item"_ - to execute if an exception is thrown from the initial request, defaults to `:default-section` value and `"error"` _[untested]_.
* `:home` - the _"section.item"_ pair used for the / URL, defaults to `:default-section` and `:default-item` values.
* `:json-config` - specify formatting information for Cheshire's JSON `generate-string`, used in `render-json` (`:date-format`, `:ex`, `:key-fn`, etc).
* `:lazy-load` - boolean, whether controllers should be lazily loaded. Default is false and all files in the `controllers` will be loaded just once at startup. When true, each controller is loaded when it is first requested, and if a request is a reload (see below) then the controller for that request is fully reloaded. `:lazy-load true` is useful for development, but should be turned off in production. _Note: in versions prior to 0.10.0, lazy loading was the default._
* `:middleware-default-fn` - an optional function that will be applied to Ring's site defaults; note that by default we do **not** enable the XSRF Anti-Forgery middleware that is normally part of the site defaults since that requires session scope and client knowledge which is not appropriate for many uses of FW/1. Specify `#(assoc-in % [:security :anti-forgery] true)` here to opt into XSRF Anti-Forgery (you'll probably also want to change the `:session :store` from the in-memory default unless you have just a single server instance).
* `:middleware-wrapper-fn` - an optional function that will be applied as the outermost piece of middleware, wrapping all of Ring's defaults (and the JSON parameters middleware).
* `:options-access-control` - specify what an `OPTIONS` request should return (`:origin`, `:headers`, `:credentials`, `:max-age`).
* `:password` - specify a password for the application reload URL flag, default `"secret"` - see also `:reload`.
* `:reload` - specify an `rc` key for the application reload URL flag, default `:reload` - see also `:password`.
* `:reload-application-on-every-request` - boolean, whether to reload controller, view and layout components on every request (intended for development of applications).
* `:render-types` - an optional map of data types to replace or augment the built-in data rendering. See **Rendering Data** above for more details.
* `:routes` - a vector of hash maps, specifying route patterns and what to map them to (full documentation coming in due course).
* `:selmer-tags` - you can specify a map that is passed to the Selmer parser to override what characters are used to identify tags, filters
* `:suffix` - the file extension used for views and layouts. Default is `"html"`.

For example: `(fw1/configure-router {:default-section "hello" :default-item "world"})` will tell FW/1 to use `hello.world` as the default action.

License & Copyright
===================

Copyright (c) 2015-2016 Sean Corfield.

Distributed under the Apache Source License 2.0.
