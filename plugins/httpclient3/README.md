## Apache HttpClient 3.x
* Since: Pinpoint 1.5.0
* See: https://hc.apache.org/index.html
* Range: commons-httpclient/commons-httpclient [3.0, 3.1]

### Pinpoint Configuration
pinpoint.config

#### HttpClient options.
~~~
# Record Parameter.
profiler.apache.httpclient3.param=true

# Record Cookies.
profiler.apache.httpclient3.cookie=false

# When to dump cookies. Either ALWAYS or EXCEPTION.
profiler.apache.httpclient3.cookie.dumptype=EXCEPTION
# 1 out of n cookies will be sampled where n is the rate. (1: 100%)
profiler.apache.httpclient3.cookie.sampling.rate=1
profiler.apache.httpclient.cookie.dumpsize=1024

# Dump entities of POST and PUT requests. Limited to entities where HttpEntity.isRepeatable() == true.
profiler.apache.httpclient3.entity=false

# When to dump entities. Either ALWAYS or EXCEPTION.
profiler.apache.httpclient3.entity.dumptype=EXCEPTION
# 1 out of n entities will be sampled where n is the rate. (10: 10%)
profiler.apache.httpclient3.entity.sampling.rate=1
profiler.apache.httpclient3.entity.dumpsize=1024

# Record IO time.
profiler.apache.httpclient3.io=true
~~~
