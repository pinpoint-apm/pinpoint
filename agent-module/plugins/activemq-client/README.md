## ActiveMQ 
* Since: Pinpoint 1.6.0
* See: https://activemq.apache.org/
* Range: org.apache.activemq/activemq-client [5.1.0, 5.16]

### Pinpoint Configuration
pinpoint.config

#### Client options.
~~~
profiler.activemq.client.enable=true
profiler.activemq.client.producer.enable=true
profiler.activemq.client.consumer.enable=true
profiler.activemq.client.trace.message=false

# ActiveMQ destination path separator (default is ".")
profiler.activemq.client.destination.separator=

# ActiveMQ destinations to exclude from tracing (comma seprated list of ant-matched destinations)
profiler.activemq.client.destination.exclude=
~~~
