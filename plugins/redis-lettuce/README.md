## Redis Lettuce Client.
* Version: 1.0
* Since: Pinpoint 1.8.1
* See: https://lettuce.io/
* Range: io.lettuce/lettuce-core [5.0.0.RELEASE, 5.1.2.RELEASE] (5.0.0.RELEASE <= x <= 5.1.2.RELEASE)

### Pinpoint Configuration
pinpoint.config

#### Set enable options.
~~~
# Enable/Disable
# Default value is true.
profiler.redis.lettuce.enable=true
~~~

## TODO
It will be implemented in future versions.
* Asynchronous tracking
  * We are looking for a way to apply the java.util.concurrent.CompletableFuture class.
* Reactive feature tracking
  * We are looking for a way to support projectreactor.io
* IO(read/write) time tracking
  * It is difficult to measure the read time, so we are looking for a way.
