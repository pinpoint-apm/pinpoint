## Project Reactor
* Version: 1.0
* Since: Pinpoint 2.0.0
* See: https://github.com/reactor/reactor-core
* See: [Project Reactor](https://projectreactor.io)
* Range: io.projectreactor/reactor-core [3.0.0.RELEASE, 3.3.1.RELEASE]

### Pinpoint Configuration
pinpoint.config

#### Set enable options.
~~~
###########################################################
# Reactor
###########################################################
profiler.reactor.enable=true
# Set whether to trace the Subscriber.onError(Throwable t) method
profiler.reactor.trace.subscribe.error=true
~~~

### Trace

#### Flux
A Reactive Streams Publisher with basic flow operators.

#### Mono
A Reactive Streams Publisher constrained to ZERO or ONE element with appropriate operators.

### TODO

#### Schedulers
Reactor uses a Scheduler as a contract for arbitrary task execution. It provides some guarantees required by Reactive Streams flows like FIFO execution.

#### ParallelFlux
