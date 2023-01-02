## Monitoring User Specified HTTP Headers
* Since: Pinpoint 2.3.0

### Pinpoint Configuration
pinpoint.config

#### HTTP headers option.
~~~
# User-specified HTTP headers
# e.g. profiler.proxy.http.headers=X-Trace, X-Request
profiler.proxy.http.headers=
~~~

### Mobile app
Add HTTP header.
~~~
# If profile.proxy.http.headers=x-trace setting
x-trace: t=1670487808091 d=3775428 
~~~

* t: The Unix epoch (or Unix time or POSIX time or Unix timestamp) is the number of seconds that have elapsed since January 1, 1970 (midnight UTC/GMT)
  * **required**
* d: duration time microseconds
   * optional

### System flow
~~~

         +-----------+       +--------------+       +------------------+
         |   Mobile  | ----> |   Unknown    | ----> |        WAS       |
         |    App    |       +   Gateway    |       + (pinpoint agent) |
         +-----------+       +--------------+       +------------------+
               |                                            |
               |                                      +--------------+
               + -----------------------------------> | elapsed time |
                                                      +--------------+

~~~