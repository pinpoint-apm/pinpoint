## Websphere Application Server
* Since: Pinpoint 1.7.0
* See: https://www.ibm.com/
* Range: [6.1, 8]

### Pinpoint Configuration
pinpoint.config

#### Set enable options.
~~~
profiler.websphere.enable=true
# Classes for detecting application server type. Comma separated list of fully qualified class names. Wildcard not supported.
profiler.websphere.bootstrap.main=
# trace param in request  ,default value is true
profiler.websphere.tracerequestparam=true
# URLs to exclude from tracing.
# Support ant style pattern. e.g. /aa/*.html, /??/exclude.html
profiler.websphere.excludeurl=
# HTTP Request methods to exclude from tracing
profiler.websphere.excludemethod=
# Hide pinpoint headers.
profiler.websphere.hidepinpointheader=true

# original IP address header
# https://en.wikipedia.org/wiki/X-Forwarded-For
#profiler.websphere.realipheader=X-Forwarded-For
# nginx real ip header
#profiler.websphere.realipheader=X-Real-IP
#profiler.websphere.realipemptyvalue=unknown
~~~

#### Set Exclude URL options.
~~~
profiler.websphere.excludeurl=
~~~

