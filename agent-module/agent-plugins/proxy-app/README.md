## Monitoring Proxy Server(App)
* Since: Pinpoint 2.0.0
* See: https://httpd.apache.org/docs/2.4/en/mod/mod_headers.html

### Pinpoint Configuration
pinpoint.config

#### Enable options.
~~~
profiler.proxy.http.header.enable=true
~~~

### Common proxy servers
Milliseconds since epoch (13 digits) and app information.

#### Add HTTP header.
~~~
Pinpoint-ProxyApp: t=1594316309407 app=foo-bar
~~~    

**t=epoch is required value.**