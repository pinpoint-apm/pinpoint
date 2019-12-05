## Reactor Netty
* Version: 1.0
* Since: Pinpoint 2.0.0
* See: https://github.com/reactor/reactor-netty
* See: [Project Reactor](https://projectreactor.io/)
* See: [Spring Boot - Servlet Containers](https://spring.io/projects/spring-boot)
* Range: io.projectreactor.netty/reactor-netty [0.8.0.RELEASE, 0.9.2.RELEASE]

### Pinpoint Configuration
pinpoint.config

#### Set enable options.
~~~
###########################################################
# Reactor Netty
###########################################################
profiler.reactor-netty.enable=true

# Classes for detecting application server type. Comma separated list of fully qualified class names. Wildcard not supported.
profiler.reactor-netty.server.bootstrap.main=

# trace param in request  ,default value is true
profiler.reactor-netty.server.tracerequestparam=true
# URLs to exclude from tracing.
# Support ant style pattern. e.g. /aa/*.html, /??/exclude.html
profiler.reactor-netty.server.excludeurl=
# HTTP Request methods to exclude from tracing
#profiler.reactor-netty.server.excludemethod=

# original IP address header
# https://en.wikipedia.org/wiki/X-Forwarded-For
#profiler.reactor-netty.server.realipheader=X-Forwarded-For
# nginx real ip header
#profiler.reactor-netty.server.realipheader=X-Real-IP
# optional parameter, If the header value is ${profiler.reactor-netty.realipemptyvalue}, Ignore header value.
#profiler.reactor-netty.server.realipemptyvalue=unknown
~~~

If you use Spring boot starter, if you set main class as profiler.spring boot.bootstrap.main setting value.
It will be displayed as spring-boot type in pinpoint server-map.
~~~
profiler.springboot.bootstrap.main=foo.bar.SampleApplication
~~~

### Web Server
* Netty(Reactor Netty) HTTP Server
