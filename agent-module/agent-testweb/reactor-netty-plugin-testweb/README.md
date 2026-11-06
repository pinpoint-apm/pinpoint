## Install
```
$ ./mvnw -pl agent-module/agent-testweb/reactor-netty-plugin-testweb install -Dmaven.test.skip=true
```

## Run
```
$ ./mvnw -pl agent-module/agent-testweb/reactor-netty-plugin-testweb spring-boot:start
```
You can then access here: http://localhost:18080/client/get

## Stop
```
$ ./mvnw -pl agent-module/agent-testweb/reactor-netty-plugin-testweb spring-boot:stop
```
