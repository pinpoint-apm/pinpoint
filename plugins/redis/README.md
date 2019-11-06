## Redis Client.
* Version: 1.1
* Since: Pinpoint 1.0
* See: https://github.com/xetorthio/jedis
* Range: redis.clients/jedis [2.4.x, 2.9.0] (2.4.x <= x <= 2.9.0)

### Pinpoint Configuration
pinpoint.config

#### Set enable options.
~~~
# Enable/Disable
# Default value is true.
# profiler.redis.enable=true (Deprecated)
profiler.redis.jedis.enable=true

# IO(write/read) times.
# Default value is true.
# profiler.redis.io=true (Deprecated)
profiler.redis.jedis.io=true

# Redis pipeline
# Default value is true.
# profiler.redis.pipeline=true (Deprecated)
profiler.redis.jedis.pipeline=true
~~~
