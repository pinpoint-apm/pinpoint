## Resin Web Server
* Since: Pinpoint 1.6.0
* See: https://caucho.com/
* Range: [4.0, 4.max]

### Pinpoint Configuration
pinpoint.config

#### Set enable options.
~~~
# default enable resin plugin
profiler.resin.enable=true
# if empty , default value is : com.caucho.server.resin.Resin
profiler.resin.bootstrap.main=
# trace param in request  ,default value is true
profiler.resin.tracerequestparam=true
# URLs to exclude from tracing.
# Support ant style pattern. e.g. /aa/*.html, /??/exclude.html
profiler.resin.excludeurl=
# Hide pinpoint headers.
profiler.resin.hidepinpointheader=true
# HTTP Request methods to exclude from tracing
#profiler.resin.excludemethod=

# original IP address header
# https://en.wikipedia.org/wiki/X-Forwarded-For
#profiler.resin.realipheader=X-Forwarded-For
# nginx real ip header
#profiler.resin.realipheader=X-Real-IP
# optional parameter, If the header value is ${profiler.resin.realipemptyvalue}, Ignore header value.
#profiler.resin.realipemptyvalue=unknown
~~~

### Resin 3.x Configuration
We currently do not support Resin 3.x.

### Resin 4.x Configuration
Add the following options to the *<server>* configuration in */conf/resin.xml*:
```
<jvm-arg>-javaagent:$PINPOINT_AGENT_HOME/pinpoint-bootstrap-$PINPOINT_VERSION.jar</jvm-arg>
<jvm-arg>-Dpinpoint.agentId=$AGENT_ID</jvm-arg>
<jvm-arg>-Dpinpoint.applicationName=$APPLICATION_NAME</jvm-arg>
```

Add the following option to the *<class-loader>* configuration under *<web-app>* in */config/resin.xml*:
```
<library-loader path="$PINPOINT_AGENT_HOME/plugin"/>
```
