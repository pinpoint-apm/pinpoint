## Spring WebFlux
* Version: 1.0
* Since: Pinpoint 2.0.0
* See: https://spring.io/
* See: [Web on Reactive Stack](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html)
* See: [Spring Boot - Servlet Containers](https://spring.io/projects/spring-boot)
* Range: org.springframework/spring-webflux [5.0.0.RELEASE, 5.2.2.RELEASE]
* Range: org.springframework.boot/spring-boot-starter [2.0.0.RELEASE, 2.2.1.RELEASE]
  * Netty server is not supported in spring boot starter 2.0.x version.

### Pinpoint Configuration
pinpoint.config

#### Set enable options.
~~~
###########################################################
# Spring WebFlux
###########################################################
profiler.spring.webflux.enable=true

# Client
# Record Parameter.
profiler.spring.webflux.client.param=true
# Record cookies.
profiler.spring.webflux.client.cookie=true
# When cookies should be dumped. It could be ALWAYS or EXCEPTION.
profiler.spring.webflux.client.cookie.dumptype=ALWAYS
# 1 out of n cookies will be sampled where n is the rate. (1: 100%)
profiler.spring.webflux.client.cookie.sampling.rate=1
# Cookie dump size.
profiler.spring.webflux.client.cookie.dumpsize=1024
~~~

If you use Spring boot starter, if you set main class as profiler.spring boot.bootstrap.main setting value.
It will be displayed as spring-boot type in pinpoint server-map.
~~~
profiler.springboot.bootstrap.main=foo.bar.SampleApplication
~~~

If you want to track Spring beans, you need to set `profiler.spring.beans.2.scope = post-processor`.
~~~
profiler.spring.beans.2.scope=post-processor
profiler.spring.beans.2.base-packages=
profiler.spring.beans.2.name.pattern=
profiler.spring.beans.2.class.pattern=
profiler.spring.beans.2.annotation=org.springframework.stereotype.Controller,org.springframework.stereotype.Service,org.springframework.stereotype.Repository
~~~

### Web Server
* Netty(Reactor Netty) HTTP Server
* Tomcat
* Undertow
* Jetty

### HTTP Client
* WebClient
