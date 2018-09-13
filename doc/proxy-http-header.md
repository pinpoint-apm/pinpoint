---
title: Monitoring Proxy Server
keywords: proxy, http, header
last_updated: Feb 1, 2018
sidebar: mydoc_sidebar
permalink: proxyhttpheader.html
disqus: false
---

# Proxy monitoring using HTTP headers
It is used to know the elapsed time between proxy and WAS.

![overview](images/proxy-http-header-overview.png)

## Pinpoint Configuration

pinpoint.config
~~~
profiler.proxy.http.header.enable=true
~~~

## Proxy Configuration
### Apache HTTP Server
* http://httpd.apache.org/docs/2.4/en/mod/mod_headers.html

Add HTTP header.
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
~~~
**%t is required value.**

### Nginx
* http://nginx.org/en/docs/http/ngx_http_core_module.html
* http://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_set_header

Add HTTP header.
~~~
Pinpoint-ProxyNginx: t=1504248328.423 D=0.123
~~~

e.g.

nginx.conf
~~~
...
  server {
        listen       9080;
        server_name  localhost;

        location / {
            ...
            set $pinpoint_proxy_header "t=$msec D=$request_time";
            proxy_set_header Pinpoint-ProxyNginx $pinpoint_proxy_header;
        }
  }
...
~~~
or
~~~
http {
...

    proxy_set_header Pinpoint-ProxyNginx t=$msec;

...
}
~~~
**t=$msec is required value.**

### App
Milliseconds since epoch and app information.

Add HTTP header.
~~~
Pinpoint-ProxyApp: t=1502861340 app=foo-bar
~~~
**t=epoch is required value.**
