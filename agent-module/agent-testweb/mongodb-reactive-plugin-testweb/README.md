
## Install
```
$ ./mvnw -pl agent-module/agent-testweb/mongodb-reactive-plugin-testweb install -Dmaven.test.skip=true
```

## Run
```
$ ./mvnw -pl agent-module/agent-testweb/mongodb-reactive-plugin-testweb spring-boot:start
```
You can then access here: http://localhost:18080/

## Stop
```
$ ./mvnw -pl agent-module/agent-testweb/mongodb-reactive-plugin-testweb spring-boot:stop
```
