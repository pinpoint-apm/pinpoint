## Ktor
* Since: Pinpoint 3.0.1
* See: https://ktor.io/

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

~~~
