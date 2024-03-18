## Apache HttpClient 5.x
* Since: Pinpoint 2.5.0
* See: https://hc.apache.org/index.html
* Range: org.apache.httpcomponents.client5/httpclient5 [5.0, 5.2]

### Pinpoint Configuration
pinpoint.config

#### HttpClient options.
~~~
profiler.apache.httpclient5.enable=true
# Record Parameter.
profiler.apache.httpclient5.param=true

# Record cookies.
profiler.apache.httpclient5.cookie=false

# When cookies should be dumped. It could be ALWAYS or EXCEPTION.
profiler.apache.httpclient5.cookie.dumptype=EXCEPTION

# 1 out of n cookies will be sampled where n is the rate. (1: 100%)
profiler.apache.httpclient5.cookie.sampling.rate=1

profiler.apache.httpclient5.cookie.dumpsize=1024

# Dump entities of POST and PUT requests. Limited to entities where HttpEntity.isRepeatable() == true.
profiler.apache.httpclient5.entity=false

# When to dump entities. Either ALWAYS or EXCEPTION.
profiler.apache.httpclient5.entity.dumptype=EXCEPTION

# 1 out of n entities will be sampled where n is the rate. (10: 10%)
profiler.apache.httpclient5.entity.sampling.rate=1

profiler.apache.httpclient5.entity.dumpsize=1024

# Allow profiling status code value.
profiler.apache.httpclient5.entity.statuscode=true

~~~
