## Monitoring Proxy Server(Apache)
* Since: Pinpoint 2.0.0
* See: https://httpd.apache.org/docs/2.4/en/mod/mod_headers.html

### Pinpoint Configuration
pinpoint.config

#### Enable options.
~~~
profiler.proxy.http.header.enable=true
~~~

### Apache HTTP Server

#### Add HTTP header.
~~~
Pinpoint-ProxyApache: t=991424704447256 D=3775428 i=51 b=49
~~~    

e.g.
httpd.conf

~~~
<IfModule mod_jk.c>
...
RequestHeader set Pinpoint-ProxyApache "%t %D %i %b"
...
</IfModule>
%t is required value.
~~~
