## Jetty Web Server
* Since: Pinpoint 1.5.0
* See: https://www.eclipse.org/jetty/
* Range: [9, 11]

### Pinpoint Configuration
pinpoint.config

#### Set enable options.
~~~
profiler.jetty.enable=true
# Classes for detecting application server type. Comma separated list of fully qualified class names. Wildcard not supported.
profiler.jetty.bootstrap.main=org.eclipse.jetty.start.Main
# URLs to exclude from tracing.
# Support ant style pattern. e.g. /aa/*.html, /??/exclude.html
profiler.jetty.excludeurl=
# HTTP Request methods to exclude from tracing
#profiler.jetty.excludemethod=
# Hide pinpoint headers.
profiler.jetty.hidepinpointheader=true
profiler.jetty.tracerequestparam=true

# original IP address header
# https://en.wikipedia.org/wiki/X-Forwarded-For
#profiler.jetty.realipheader=X-Forwarded-For
# nginx real ip header
#profiler.jetty.realipheader=X-Real-IP
# optional parameter, If the header value is ${profiler.jetty.realipemptyvalue}, Ignore header value.
#profiler.jetty.realipemptyvalue=unknown
~~~

### Web Server
* Spring Boot starter

### Notice
Please read this if you meet this situation:
- pinpoint could collect server info (CPU, Memory) but no trace info.
- no unusual log (WARN or ERROR level). 
- your jetty use --exec parameter.

Problem:
when using jetty with parameter --exec, jetty will create a child process, if the agent parameter (-javaagent:$AGENT_PATH....) is added in start shell, the parameter is added to parent process instead of child process, so the class loaded by jetty will not be rewrited and trace data cannot be collected.

Solution:
add the agent parameter (-javaagent:$AGENT_PATH....) after --exec (in start.ini) instead of in start shell.

