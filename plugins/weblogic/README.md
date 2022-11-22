## Jetty Web Server
* Since: Pinpoint 1.8.0
* See: https://www.oracle.com/java/weblogic/
* Range: [10, 12]

### Pinpoint Configuration
pinpoint.config

#### Set enable options.
~~~
profiler.weblogic.enable=true
# Classes for detecting application server type. Comma separated list of fully qualified class names. Wildcard not supported.
profiler.weblogic.bootstrap.main=weblogic.Server
# trace param in request  ,default value is true
profiler.weblogic.tracerequestparam=true
# URLs to exclude from tracing.
# Support ant style pattern. e.g. /aa/*.html, /??/exclude.html
profiler.weblogic.excludeurl=
# HTTP Request methods to exclude from tracing
#profiler.weblogic.excludemethod=
# Hide pinpoint headers.
profiler.weblogic.hidepinpointheader=true

# original IP address header
# https://en.wikipedia.org/wiki/X-Forwarded-For
#profiler.weblogic.realipheader=X-Forwarded-For
# nginx real ip header
#profiler.weblogic.realipheader=X-Real-IP
# optional parameter, If the header value is ${profiler.weblogic.realipemptyvalue}, Ignore header value.
#profiler.weblogic.realipemptyvalue=unknown
~~~

### Notice
Add the agent parameter (-javaagent:$AGENT_PATH....) in start shell under the domains

For example:
1. In Windows, you should find the file: 'startWeblogic.cmd'. Then, add the agent parameter (-javaagent:$AGENT_PATH....) after 'set SAVE_JAVA_OPTIONS=%JAVA_OPTIONS%' as following:

set SAVE_JAVA_OPTIONS=%JAVA_OPTIONS%  -javaagent:$AGENT_PATH....

2. In Linux(Unix), you should find the file: 'startWeblogic.sh'. Then, add the agent parameter (-javaagent:$AGENT_PATH....) after 'SAVE_JAVA_OPTIONS="${JAVA_OPTIONS}' as following:

SAVE_JAVA_OPTIONS="${JAVA_OPTIONS} -javaagent:$AGENT_PATH...." 
