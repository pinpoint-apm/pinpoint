
## Install
```
$ mvnw -P pinpoint-jdk-httpclient-plugin-testweb install -Dmaven.test.skip=true
```

## Run
```
$ mvnw -P pinpoint-jdk-httpclient-plugin-testweb spring-boot:start
```
You can then access here: https://localhost:18443/

## Stop
```
$ mvnw -P pinpoint-jdk-httpclient-plugin-testweb spring-boot:stop
```
