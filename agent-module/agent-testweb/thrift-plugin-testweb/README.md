## Install
```
$ mvnw -P pinpoint-thrift-plugin-testweb install -Dmaven.test.skip=true
```

## Run
```
$ mvnw -P pinpoint-thrift-plugin-testweb spring-boot:start
```
You can then access here: http://localhost:18080/server/sync


## Stop
```
$ mvnw -P pinpoint-thrift-plugin-testweb spring-boot:stop
```
