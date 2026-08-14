## Project Reactor
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
# Set whether to trace the onErrorComplete(), onErrorResume(), onErrorMap(), onErrorReturn() methods
profiler.reactor.trace.onError=false
profiler.reactor.mark.error.onError=false
# Set whether to trace the publishOn(), subscribeOn() methods
profiler.reactor.trace.publishOn=true
profiler.reactor.trace.subscribeOn=true
profiler.reactor.trace.delay=true
profiler.reactor.trace.interval=true
# retry, retryWhen, timeout
profiler.reactor.trace.retry=true
profiler.reactor.mark.error.retry=false
profiler.reactor.trace.timeout=true
# FLUX/MONO subscribe
profiler.reactor.trace.subscribe=true
~~~

#### Experimental options.
~~~
# Trace the one-shot scheduler task carriers, so that a task submitted to a Scheduler keeps the
# trace across the thread hop even when it is not an operator subscriber. See Schedulers below.
profiler.reactor.trace.scheduler.task=false
# Periodic scheduler tasks are excluded from instrumentation by default. When enabled, every
# execution is recorded as a new independent transaction and never continues the trace that
# happened to schedule the job. See Periodic tasks below.
profiler.reactor.trace.scheduler.task.periodic=false
# Instrument the generic per-operator CoreSubscriber layer. See Lightweight mode below.
profiler.reactor.subscriber.instrument=true
# publishOn-only seam wrapper: replace the legacy publisher field injection on publishOn results
# with a wrapping subscriber. Parked after its cost/benefit measurement - see the note below.
profiler.reactor.wrap.publisher.publishOn=false
~~~

### Trace

#### Flux
A Reactive Streams Publisher with basic flow operators.

#### Mono
A Reactive Streams Publisher constrained to ZERO or ONE element with appropriate operators.

### Lightweight mode

By default the plugin instruments every `CoreSubscriber` implementation in
`reactor.core.publisher` - 200+ classes get an injected `AsyncContextAccessor` field plus
constructor, `onSubscribe`, `onNext` and `run` interceptors. That generic per-operator layer is
what carries the trace along an operator chain, and it is also where most of the plugin's CPU
cost sits, because every operator type shares the same context read.

Setting `profiler.reactor.subscriber.instrument=false` skips the registration of that layer
entirely, so the classes it would have matched are never transformed and carry no residency
cost. Everything registered by exact class name stays: the timeout, retry and onError
subscribers, the interval and delay timer tasks, the scheduler task carriers, the publisher
field injection and the subscribe relay.

The one-shot scheduler task carrier is **required** in this mode and is therefore enabled
automatically (with a log line) even if `profiler.reactor.trace.scheduler.task` is left at
`false`. Without the generic layer a scheduler hop has no operator relay, so user code running
after the hop - for example a WebClient call assembled inside `flatMap` - would run outside the
trace and outbound header propagation would silently break.

The mode is experimental and off by default. Enable it only if the coverage trade-off below is
acceptable for your service.

#### Coverage trade-off

| Signal | Default | Lightweight |
|---|---|---|
| Distributed trace propagation (outbound headers, async links) | traced | traced, through the scheduler task carrier and the subscribe relay instead of the operator chain |
| Operator boundary events - the REACTOR frames from operator construction, `onNext` and `run` | recorded | not recorded |
| Scheduler hops | linked by the operator relay | linked by the carrier's `run()` window. A hop that finds no async context on the task falls back to the current trace and records one REACTOR event per task, so a hop-intensive workload sees noticeably more boundary events than it does by default |
| `retry` / `retryWhen` re-subscription | every attempt stays in the originating transaction: the two retry subscribers are exact-name transforms that seed an async context at construction and restore it around `resubscribe()` | unchanged - the seed and the re-subscription window are exact-name transforms and survive this mode |
| Per-attempt retry failure events | not recorded at the re-subscription boundary - the restore-only window deliberately records no span event per attempt (`retryWhen` failures are still visible through its companion `whenError` instrumentation) | same |
| `timeout` | traced | unchanged |
| `Flux` / `Mono` subscribe, `publishOn` / `subscribeOn`, `delay` / `interval`, `onError` | traced | unchanged - these are exact-name transforms and are not gated |

Weigh the boundary-event loss against the throughput gain: single-endpoint WebFlux benchmarks
measured 5 to 8% more throughput with the mode on (JDK 25), and how much of that a given service
sees depends on how operator-dense its chains are.

### Schedulers

Reactor uses a Scheduler as a contract for arbitrary task execution. It provides some guarantees
required by Reactive Streams flows like FIFO execution.

A task crossing to a worker thread keeps its trace in one of two ways:

* The scheduled task is itself an instrumented operator subscriber - `publishOn` and
  `subscribeOn` schedule their own subscriber - so the generic per-operator layer carries the
  context across the hop. This is what covers scheduler hops by default.
* The task is wrapped by one of reactor's one-shot scheduler task carriers, which Pinpoint
  instruments when `profiler.reactor.trace.scheduler.task` is enabled. This is the only thing
  that links a plain `Runnable` handed straight to `Scheduler.schedule()`, and it is what
  replaces the operator relay in lightweight mode.

#### Periodic tasks

A periodic task is constructed once and runs until it is disposed. If it adopted the trace that
happened to schedule it - a request-scoped trace, say - every tick would keep appending async
chunks to that one transaction for as long as the job lives, and the transaction could never
end. The periodic carriers (`PeriodicSchedulerTask`, `PeriodicWorkerTask`,
`InstantPeriodicWorkerTask`) are therefore not instrumented at all by default.

`profiler.reactor.trace.scheduler.task.periodic=true` opts in with different semantics: each
execution is recorded as a new independent transaction (rpc `Reactor periodic scheduler task`,
endpoint `LOCAL`), and a trace already bound to the worker thread is suspended for the duration
of the tick and restored afterwards, never continued into the periodic work. Reactor's own
periodic sources are unaffected either way - `Flux.interval` runs an instrumented
`FluxInterval$IntervalRunnable`, which carries its context through its own exact-name
instrumentation, not through the periodic carrier.

#### Carrier coverage

Reactor asks custom `Scheduler` implementations to pass every submitted task through
`Schedulers.onSchedule()`, but the carrier that Pinpoint instruments is created by
`Schedulers.directSchedule()` / `Schedulers.workerSchedule()` a step later. A custom scheduler
that calls `onSchedule` and then submits the task itself is therefore invisible to the carrier.

| Scheduler | Task carrier | Covered |
|---|---|---|
| Built-in schedulers (`parallel`, `single`, `boundedElastic`, `newParallel`, ...), which go through `Schedulers.directSchedule()` / `workerSchedule()` | `SchedulerTask`, `WorkerTask` for one-shot schedules | yes |
| The same built-in schedulers, periodic schedules | `PeriodicSchedulerTask`, `PeriodicWorkerTask`, `InstantPeriodicWorkerTask` | excluded by default; opt-in records each tick as an independent transaction (see Periodic tasks) |
| `Schedulers.fromExecutor()` | `ExecutorScheduler$ExecutorPlainRunnable`, `ExecutorScheduler$ExecutorTrackedRunnable` | yes |
| `Schedulers.fromExecutorService()` | `SchedulerTask` for direct schedules, `WorkerTask` for worker schedules | yes |
| Virtual-thread `boundedElastic` (reactor 3.6+ on JDK 21+) | `BoundedElasticThreadPerTaskScheduler$SchedulerTask` | yes |
| `Schedulers.immediate()` | none - the task runs inline | not applicable, there is no thread hop to cross |
| Custom `Scheduler` that calls `Schedulers.onSchedule()` and then submits the task through its own carrier | its own | no |
| Custom `Scheduler` that does not call `Schedulers.onSchedule()` at all | its own | no |

The virtual-thread `boundedElastic` scheduler is worth a note: reactor-core is a multi-release
JAR, and the `BoundedElasticThreadPerTaskScheduler` on the Java 8 class path is a stub whose
methods all throw `UnsupportedOperationException`. The real implementation, and the nested
`SchedulerTask` that Pinpoint instruments, live under `META-INF/versions/21`. Reading only the
base entry makes the scheduler look uninstrumented when it is not.

### publishOn seam wrapper (parked)

`profiler.reactor.wrap.publisher.publishOn=true` replaces the legacy field injection on
`publishOn` results with a wrapping subscriber that delivers every signal inside a trace window.
The experiment is parked: an A/B on a response-coupled 1000-element publishOn chain measured a
consistent 2-3% throughput cost from the per-signal window and the fusion suppression the
wrapper requires, cancelling the relay-removal gain it was meant to earn. The gate stays off by
default and its propagation IT (`ReactorPublishOnSeam_IT`) is disabled; both carry pointers to
the measurement record.

### TODO

#### ParallelFlux
