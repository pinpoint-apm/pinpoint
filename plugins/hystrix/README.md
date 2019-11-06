### Pinpoint Hystrix Plugin (beta)

### Hystrix Plugin Configuration
Hystrix plugin is disabled by default and for [release](https://github.com/naver/pinpoint/blob/master/agent/src/main/resources-release/pinpoint.config) profile until the beta status is lifted. To enable, set the following options in *pinpoint.config* (**Note that you must also enable rxjava plugin**) :
```
profiler.rxjava=true

profiler.hystrix=true
```
