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
