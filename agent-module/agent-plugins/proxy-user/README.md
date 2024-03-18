## Monitoring User Specified HTTP Headers
* Since: Pinpoint 2.3.0

### Pinpoint Configuration
pinpoint.config

#### HTTP headers option.
~~~
# User-specified HTTP headers
# Supports apache and nginx formats.
# e.g. profiler.proxy.http.headers=X-Trace, X-Request, X-ApacheLB, X-SSL-nginx
profiler.proxy.http.headers=
~~~

### Apache HTTP Server

* [http://httpd.apache.org/docs/2.4/en/mod/mod\_headers.html](http://httpd.apache.org/docs/2.4/en/mod/mod_headers.html)

Add HTTP header.

```text
X-ApacheLB: t=991424704447256 D=3775428
```

e.g.

httpd.conf

```text
<IfModule mod_jk.c>
...
RequestHeader set X-ApacheLB "%t %D"
...
</IfModule>
```

**%t is required value.**

### Nginx

* [http://nginx.org/en/docs/http/ngx\_http\_core\_module.html](http://nginx.org/en/docs/http/ngx_http_core_module.html)
* [http://nginx.org/en/docs/http/ngx\_http\_proxy\_module.html\#proxy\_set\_header](http://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_set_header)

Add HTTP header.

```text
X-SSL-nginx: t=1504248328.423 D=0.123
```

e.g.

nginx.conf

```text
...
  server {
        listen       9080;
        server_name  localhost;

        location / {
            ...
            set $pinpoint_proxy_header "t=$msec D=$request_time";
            proxy_set_header X-SSL-nginx $pinpoint_proxy_header;
        }
  }
...
```

or

```text
http {
...

    proxy_set_header X-SSL-nginx t=$msec;

...
}
```

**t=$msec is required value.**

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