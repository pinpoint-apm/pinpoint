## Kotlin Corutines
* Since: Pinpoint 2.4.0
* See: https://github.com/Kotlin/kotlinx.coroutines
* Range: log4j/log4j [1.0.1,]

### Pinpoint Configuration
pinpoint.config

#### Trace options
~~~
###########################################################
# Kotlin Corutines
# v1.0.1 ~
###########################################################
profiler.kotlin.coroutines.enable=false

#Trace the name of the thread.
#This is important information to check whether the developer's intention and the behavior of the coroutine match.
#Recommend that you use it in the development environment and not in the production environment.
profiler.kotlin.coroutines.record.threadName=false

#Track cancellations and the propagation of cancellations.
#This is important information to check whether the developer's intention and the behavior of the coroutine match.
#Recommend that you use it in the development environment and not in the production environment.
profiler.kotlin.coroutines.record.cancel=false
~~~

### Coroutines Lifecycle
~~~
           Before                   Run                  After

         +-----------+
         | Start     | -------------+
         +-----------+              |
               |                    |      +---------+
               +------------------- | -----| Executor|-------+
               |                    |      +---------+       |
               |                    |                        |
               V                    V                        |
         +-----------+       +--------------+       +------------+
         | Scheduler | ----> | Continuation | ----> | Dispatcher |
         | Ldispatch |       + L resumeWith |       + Ldispatch  |
         +-----------+       +--------------+       +------------+
                                    |                       |
                                    |                       V
                                    |               +------------+
                                    + ------------> |  Finish    |
                                                    +------------+
~~~