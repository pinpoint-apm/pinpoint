# pinpoint-web

## build

```  
mvnw install -pl web -am -Dmaven.test.skip=true
```
 
Skip frontend build
```
mvnw install -pl web -am -Dbuild.frontend.skip=true -Dmaven.test.skip=true
```

``` 
cd web
../mvnw install -pl web -am -Dbuild.frontend.skip=true -Dmaven.test.skip=true -f ../pom.xml
```

## run
```
java -jar -Dpinpoint.zookeeper.address=$ZOOKEEPER_ADDRESS web/target/deploy/pinpoint-web-boot-$VERSION.jar
```

spring-profiles
```
java -jar -Dpinpoint.zookeeper.address=$ZOOKEEPER_ADDRESS -Dspring.profiles.active=release web/target/deploy/pinpoint-web-boot-$VERSION.jar
```
