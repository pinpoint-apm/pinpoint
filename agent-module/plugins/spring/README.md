## Spring Framework
* Since: Pinpoint 1.5.0
* See: https://spring.io/
* Range: org.springframework/spring-context [3.0, 5.3]

### Pinpoint Configuration
pinpoint.config

#### Spring Beans options.
~~~
# Profile spring-beans
profiler.spring.beans=true

# Only public methods are tracked.
# filters
#    filter
#    filter OR filters
# filter
#    value
#    value AND filter
# value
#    token
#    token OR token
# token
#    profiler.spring.beans.n.scope= [component-scan | post-processor] default is component-scan.
#    profiler.spring.beans.n.base-packages= [package name, ...]
#    profiler.spring.beans.n.name.pattern= [regex pattern, regex:regex pattern, antstyle:antstyle pattern, ...]
#    profiler.spring.beans.n.class.pattern= [regex pattern, regex:regex pattern, antstyle:antstyle pattern, ...]
#    profiler.spring.beans.n.annotation= [annotation name, ...]
#
# Scope:
# component-scan: <context:component-scan ... /> or @ComponentScan
# post-processor: BeanPostProcessor - Slow!!!
#
# ANT Style pattern rules:
# ? - matches on character
# * - matches zero or more characters
# ** - matches zero or more 'directories' in a path

# Examples
# profiler.spring.beans.1.scope=component-scan
# profiler.spring.beans.1.base-packages=com.foo, com.bar
# profiler.spring.beans.1.name.pattern=.*Foo, regex:.*Bar, antstyle:*Controller
# profiler.spring.beans.1.class.pattern=
# profiler.spring.beans.1.annotation=org.springframework.stereotype.Controller,org.springframework.stereotype.Service,org.springframework.stereotype.Repository
#
# profiler.spring.beans.2.scope=post-processor
# profiler.spring.beans.2.base-packages=com.foo
# profiler.spring.beans.2.name.pattern=
# profiler.spring.beans.2.class.pattern=antstyle:com.foo.repository.*Repository, antstyle:com.foo.Service.Main*
# profiler.spring.beans.2.annotation=

profiler.spring.beans.1.scope=component-scan
profiler.spring.beans.1.base-packages=
profiler.spring.beans.1.name.pattern=
profiler.spring.beans.1.class.pattern=
profiler.spring.beans.1.annotation=org.springframework.stereotype.Controller,org.springframework.stereotype.Service,org.springframework.stereotype.Repository

profiler.spring.beans.mark.error=false
~~~

### Spring @Async 
~~~
profiler.spring.async.enable=true
# Add custom AsyncTaskExecutor classes. Comma separated list of fully qualified class names. Wildcard not supported.
# Default values
#     org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
#     org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
#     org.springframework.core.task.SimpleAsyncTaskExecutor
#     org.springframework.scheduling.quartz.SimpleThreadPoolTaskExecutor
#     org.springframework.core.task.support.TaskExecutorAdapter
#     org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
#     org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
#     org.springframework.jca.work.WorkManagerTaskExecutor
#     org.springframework.scheduling.commonj.WorkManagerTaskExecutor
profiler.spring.async.executor.class.names=
# Add custom AsyncTask classes. Comma separated list of fully qualified class names. Wildcard not supported.
# Default values
#     org.springframework.aop.interceptor.AsyncExecutionInterceptor$1
#     org.springframework.aop.interceptor.AsyncExecutionInterceptor$$Lambda$
# ex. add org.springframework.http.client.SimpleBufferingAsyncClientHttpRequest$1 and
# org.springframework.http.client.SimpleBufferingAsyncClientHttpRequest$$Lambda$
# to trace SimpleBufferingAsyncClientHttpRequest.executeAsync()
profiler.spring.async.task.class.names=
~~~


