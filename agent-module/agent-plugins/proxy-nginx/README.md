## Monitoring Proxy Server(Nginx)
* Since: Pinpoint 2.0.0
* See: http://nginx.org/en/docs/http/ngx_http_core_module.html
* Ses: http://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_set_header 

### Pinpoint Configuration
pinpoint.config

#### Enable options.
~~~
profiler.proxy.http.header.enable=true
~~~

### Nginx

#### Add HTTP header.
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