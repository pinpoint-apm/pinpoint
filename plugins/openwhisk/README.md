## Openwhisk
* Since: Pinpoint 1.8.0
* See: https://openwhisk.apache.org/
* Range: internal.com.apache.openwhisk/openwhisk-common [2.0, 2.7]

### Pinpoint Configuration
pinpoint.config

#### HttpClient options.
~~~
profiler.okhttp.enable=true
# Record param.
profiler.okhttp.param=true

# Record Cookies.
profiler.okhttp.cookie=false
# When to dump cookies. Either ALWAYS or EXCEPTION.
profiler.okhttp.cookie.dumptype=EXCEPTION
# 1 out of n cookies will be sampled where n is the rate. (1: 100%)
profiler.okhttp.cookie.sampling.rate=1
profiler.okhttp.cookie.dumpsize=1024
# enqueue operation
profiler.okhttp.async=true
profiler.okhttp.entity.statuscode=true
~~~
