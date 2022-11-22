## Apache Thrift
* Since: Pinpoint 1.5.0
* See: https://thrift.apache.org/
* Range: org.apache.thrift [0.6, 0.17]

### Pinpoint Configuration
pinpoint.config

#### Client/Processor options.
~~~
# Profile Thrift
profiler.thrift.client=true
profiler.thrift.client.async=true
# Profile processor.
profiler.thrift.processor=true
profiler.thrift.processor.async=true
# Allow recording arguments.
profiler.thrift.service.args=false
# Allow recording result.
profiler.thrift.service.result=false
~~~

### Notice
Thrift support is the default library only.
We do not support your customized server.