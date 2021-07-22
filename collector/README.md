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