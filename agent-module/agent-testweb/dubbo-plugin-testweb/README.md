
## Prerequirements

- Dubbo Plugin Test Web
  - [Dubbo samples](https://github.com/apache/dubbo-samples)

## Install
```
$ ./mvnw -pl agent-module/agent-testweb/dubbo-plugin-testweb install -Dmaven.test.skip=true
```

## Run
Provider
```
$ ./mvnw -pl agent-module/agent-testweb/dubbo-plugin-testweb -Djava.net.preferIPv4Stack=true -Dexec.mainClass=com.pinpoint.test.plugin.provider.DubboProviderApplication exec:java
```

Consumer
```
$ ./mvnw -pl agent-module/agent-testweb/dubbo-plugin-testweb spring-boot:start
```
You can then access here: http://localhost:18080/sayHi

## Stop
```
$ ./mvnw -pl agent-module/agent-testweb/dubbo-plugin-testweb spring-boot:stop
```
