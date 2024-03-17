## Alibaba Dubbo
* Since: Pinpoint 1.5.0
* See: https://www.alibabacloud.com/help/en/enterprise-distributed-application-service/latest/dubbo-overview
* Range: com.alibaba/dubbo [2.5, 2.6]

### Pinpoint Configuration
pinpoint.config

#### Dubbo options.
~~~
profiler.dubbo.enable=true
# Classes for detecting application server type. Comma separated list of fully qualified class names. Wildcard not supported.
profiler.dubbo.bootstrap.main=com.alibaba.dubbo.container.Main
~~~
