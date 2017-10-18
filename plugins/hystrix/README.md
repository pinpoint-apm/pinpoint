### Pinpoint Hystrix Plugin (beta)

### Hystrix Plugin Configuration
Hystrix plugin is disabled by default and for [release](https://github.com/naver/pinpoint/blob/master/agent/src/main/resources-release/pinpoint.config) profile until the beta status is lifted. To enable, set the following option in *pinpoint.config*:
```
profiler.hystrix=true
```

### rx.Observable support
In order to trace observables (via **HystrixCommand** and **HystrixObservableCommand**'s `observe()` and `toObservable()`), you must enable the RxJava plugin as well.
```
profiler.rxjava=true
```