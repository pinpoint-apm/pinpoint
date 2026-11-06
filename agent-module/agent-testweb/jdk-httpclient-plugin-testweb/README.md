
## Install
```
$ ./mvnw -pl agent-module/agent-testweb/jdk-httpclient-plugin-testweb install -Dmaven.test.skip=true
```

## Run
```
$ ./mvnw -pl agent-module/agent-testweb/jdk-httpclient-plugin-testweb spring-boot:start
```
You can then access here: https://localhost:18443/

## Stop
```
$ ./mvnw -pl agent-module/agent-testweb/jdk-httpclient-plugin-testweb spring-boot:stop
```
