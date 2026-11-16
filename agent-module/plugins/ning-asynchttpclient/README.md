## Async Http Client 
* Since: Pinpoint 1.5.0
* See: https://github.com/AsyncHttpClient/async-http-client
* Range: org.asynchttpclient/async-http-client [2.0, 2.16], [3.0, 3.0] (3.x requires Java 11)
* com.ning/async-http-client 1.x (the old package, last released in 2016) is not supported since Pinpoint 4.1.0. Move to org.asynchttpclient.


### Pinpoint Configuration
pinpoint.config

#### HttpClient options.
~~~
# Profile Ning Async HTTP Client.
profiler.ning.asynchttpclient=true

# Record parameters.
profiler.ning.asynchttpclient.param=true

# Record cookies.
profiler.ning.asynchttpclient.cookie=true
# When to dump cookies. Either ALWAYS or EXCEPTION.
profiler.ning.asynchttpclient.cookie.dumptype=EXCEPTION
# Cookie dump size.
profiler.ning.asynchttpclient.cookie.dumpsize=1024
# 1 out of n cookies will be sampled where n is the rate. (1: 100%)
profiler.ning.asynchttpclient.cookie.sampling.rate=1

# Record Entities.
profiler.ning.asynchttpclient.entity=false
# When to dump entities. Either ALWAYS or EXCEPTION.
profiler.ning.asynchttpclient.entity.dumptype=EXCEPTION
# Entity dump size.
profiler.ning.asynchttpclient.entity.dumpsize=1024
# 1 out of n cookies will be sampled where n is the rate. (1: 100%)
profiler.ning.asynchttpclient.entity.sampling.rate=1
# Record parameters.
profiler.ning.asynchttpclient.param=false
# When to dump parameters. Either ALWAYS or EXCEPTION.
profiler.ning.asynchttpclient.param.dumptype=EXCEPTION
# Parameter dump size.
profiler.ning.asynchttpclient.param.dumpsize=1024
# 1 out of n parameters will be sampled where n is the rate. (1: 100%)
profiler.ning.asynchttpclient.param.sampling.rate=1
~~~
