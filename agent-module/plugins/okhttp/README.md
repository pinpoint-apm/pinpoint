## OkHttp 
* Since: Pinpoint 1.5.0
* See: https://square.github.io/okhttp/
* Range: com.squareup.okhttp/okhttp [2.0, 2.7]
* Range: com.squareup.okhttp3/okhttp [3.0, 3.14]

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
