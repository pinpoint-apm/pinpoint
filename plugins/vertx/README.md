## Vertx HTTP Server/Client.

Currently supports vertx.io 3.3.x, 3.4.1, 3.4.2

## Configuration

First, set the main class for execution.
The default value is "io.vertx.core.Starter"
~~~
# Classes for detecting application server type. Comma separated list of fully qualified class names. Wildcard not supported.
profiler.vertx.bootstrap.main=io.vertx.core.Starter
~~~

Set the base packages to specify the handler for asynchronous invocations.
~~~
# Track Vertx.runOnContext() & Vertx.executeBlocking().
# Sets the base packages that implements io.vertx.core.Handler.
profiler.vertx.handler.base-packages=
# e.g. com.service.handler, com.server.http.handler
~~~

