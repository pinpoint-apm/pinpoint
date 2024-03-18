## Apache Dubbo
* Since: Pinpoint 2.0.0
* See: https://dubbo.apache.org/
* Range: org.apache.dubbo/dubbo [2.7, 3.1]

### Pinpoint Configuration
pinpoint.config

#### Dubbo options.
~~~
profiler.apache.dubbo.enable=true
# Classes for detecting application server type. Comma separated list of fully qualified class names. Wildcard not supported.
profiler.apache.dubbo.bootstrap.main=org.apache.dubbo.container.Main
~~~
