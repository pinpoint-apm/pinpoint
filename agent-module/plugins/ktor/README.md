## Ktor
* Since: Pinpoint 3.0.1
* See: https://ktor.io/
* Range (client): io.ktor/ktor-client-core-jvm [2.3, 3.5]
  (built against 2.3.12; validated against Ktor 3.5.1 clients in production deployments)

### Pinpoint Configuration
pinpoint.config

#### Set enable options.
~~~
###########################################################
# Ktor
###########################################################
profiler.ktor.enable=false

# Server
# Classes for detecting application server type. Comma separated list of fully qualified class names. Wildcard not supported.
profiler.ktor.server.bootstrap.main=
# trace param in request  ,default value is true
profiler.ktor.server.tracerequestparam=true
# URLs to exclude from tracing.
# Support ant style pattern. e.g. /aa/*.html, /??/exclude.html
profiler.ktor.server.excludeurl=
# profiler.ktor.server.trace.excludemethod=
# HTTP Request methods to exclude from tracing
#profiler.ktor.server.excludemethod=

# original IP address header
# https://en.wikipedia.org/wiki/X-Forwarded-For
#profiler.ktor.server.realipheader=X-Forwarded-For
# nginx real ip header
#profiler.ktor.server.realipheader=X-Real-IP
# optional parameter, If the header value is ${profiler.ktor.realipemptyvalue}, Ignore header value.
#profiler.ktor.server.realipemptyvalue=unknown

# Retransform
profiler.ktor.http.server.retransform.configure-routing=true

# Client (outbound)
# Traces io.ktor.client.plugins.HttpSend$DefaultSender.execute(...) and records
# an outbound span event per HTTP attempt (including HttpRequestRetry retries).
# Enabled by default; set false to skip the Ktor client transforms.
profiler.ktor.client.enable=true
# record the request URL on the span event, default true
profiler.ktor.client.param=true
# treat a thrown Throwable from the client as an error on the span event, default true
profiler.ktor.client.mark.error=true

~~~
