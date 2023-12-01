## Vertx Client/Server
* Since: Pinpoint 1.6.0
* See: https://vertx.io/
* Range: [3.3, 4.max]

### Pinpoint Configuration
pinpoint.config

#### Compatibility setting
Vert.x 3.6 ~ 4.5
~~~
profiler.vertx.http.server.request-handler.method.name=io.vertx.ext.web.impl.RouterImpl.handle
~~~

Vert.x 3.5
~~~
profiler.vertx.http.server.request-handler.method.name=io.vertx.ext.web.impl.RouterImpl.accept
~~~

#### Set enable options.
~~~
profiler.vertx.enable=true
...
# HTTP server
profiler.vertx.http.server.enable=true
...
# HTTP client
profiler.vertx.http.client.enable=true
~~~

#### Set the main class for execution.
The default value is "io.vertx.core.Starter"
~~~
profiler.vertx.bootstrap.main=io.vertx.core.Starter
~~~

#### Set the base packages to specify the handler for asynchronous invocations.
~~~
profiler.vertx.handler.base-packages=
~~~

#### Set the HttpServerRequestHandler method name.
The argument is io.vertx.core.http.HttpServerRequest. (Since pinpoint version 1.7.2)
~~~
profiler.vertx.http.server.request-handler.method.name=io.vertx.ext.web.impl.RouterImpl.accept
~~~

### Examples
If the main class looks like this:
~~~
package com.navercorp.test.pinpoint;
...
public class Server {
    private Vertx vertx;

    public static void main(String[] args) {
        this.vertx = Vertx.vertx();

        DeploymentOptions options = new DeploymentOptions();
        options.setInstances(1);

        this.vertx.deployVerticle(HttpServerVerticle.class.getName(), options);
...
~~~

~~~
profiler.vertx.bootstrap.main=com.navercorp.test.pinpoint.Server

profiler.vertx.handler.base-packages=com.navercorp.test.pinpoint
~~~

If used the Router class:
~~~
Router router = Router.router(vertx);
...
vertx.createHttpServer().requestHandler(router::accept).listen(8090);
~~~

~~~
profiler.vertx.http.server.request-handler.method.name=io.vertx.ext.web.impl.RouterImpl.accept
~~~
