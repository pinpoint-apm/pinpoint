# pinpoint-collector

## build

```
mvnw install -pl collector -am -Dmaven.test.skip=true
```
or
```
cd collector
../mvnw install -pl collector -am -Dmaven.test.skip=true -f ../pom.xml
```


## run
```
java -jar -Dpinpoint.zookeeper.address=$ZOOKEEPER_ADDRESS collector/target/deploy/pinpoint-collector-boot-$VERSION.jar
```

spring-profiles
```
java -jar -Dpinpoint.zookeeper.address=$ZOOKEEPER_ADDRESS -Dspring.profiles.active=release collector/target/deploy/pinpoint-collector-boot-$VERSION.jar
```

-----------

## Collector port
## gRPC port
| port | protocol | type
| ---- | ---- | ----
| 9991 | TCP  | agent
| 9992 | TCP  | span
| 9993 | TCP  | stat

## Thrift port
| port | protocol | type
| ---- | ---- | ----
| 9994 | TCP  | agent
| 9995 | UDP  | span
| 9996 | UDP  | stat


## Configuration for development environment
Use /config directory [External Application Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.files)
- Create property file in `/resources/config/`
  web/src/main/resources/config/MyConfig.properties