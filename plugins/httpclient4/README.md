## Apache HttpClient 4.x
* Since: Pinpoint 1.5.0
* See: https://hc.apache.org/index.html
* Range: org.apache.httpcomponents/httpclient [4.0, 4.5]

### Pinpoint Configuration
pinpoint.config

#### HttpClient options.
~~~
# Record Parameter.
profiler.apache.httpclient4.param=true

# Record cookies.
profiler.apache.httpclient4.cookie=false

# When cookies should be dumped. It could be ALWAYS or EXCEPTION.
profiler.apache.httpclient4.cookie.dumptype=EXCEPTION

# 1 out of n cookies will be sampled where n is the rate. (1: 100%)
profiler.apache.httpclient4.cookie.sampling.rate=1

profiler.apache.httpclient4.cookie.dumpsize=1024

# Dump entities of POST and PUT requests. Limited to entities where HttpEntity.isRepeatable() == true.
profiler.apache.httpclient4.entity=false

# When to dump entities. Either ALWAYS or EXCEPTION.
profiler.apache.httpclient4.entity.dumptype=EXCEPTION

# 1 out of n entities will be sampled where n is the rate. (10: 10%)
profiler.apache.httpclient4.entity.sampling.rate=1

profiler.apache.httpclient4.entity.dumpsize=1024

# Allow profiling status code value.
profiler.apache.httpclient4.entity.statuscode=true

# Record IO time.
profiler.apache.httpclient4.io=true

# Not supported yet.
#profiler.apache.nio.httpclient4=true

~~~
