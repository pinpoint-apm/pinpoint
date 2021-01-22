## Install
```
$ mvnw -P pinpoint-reactor-netty-plugin-testweb install -Dmaven.test.skip=true
```

## Run
```
$ mvnw -P pinpoint-reactor-netty-plugin-testweb spring-boot:start
```
You can then access here: http://localhost:18080/client/get

## Stop
```
$ mvnw -P pinpoint-reactor-netty-plugin-testweb spring-boot:stop
```
