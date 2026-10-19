# pinpoint-web

## build

```  
mvnw install -pl web -am -Dmaven.test.skip=true
```
 
or

``` 
cd web
../mvnw install -pl web -am -Dbuild.frontend.skip=true -Dmaven.test.skip=true -f ../pom.xml
```


Skip frontend build
```
mvnw install -pl web -am -Dbuild.frontend.skip=true -Dmaven.test.skip=true
```

## run
```
java -jar -Dpinpoint.zookeeper.address=$ZOOKEEPER_ADDRESS web/target/deploy/pinpoint-web-boot-$VERSION.jar
```

spring-profiles
```
java -jar -Dpinpoint.zookeeper.address=$ZOOKEEPER_ADDRESS -Dspring.profiles.active=release web/target/deploy/pinpoint-web-boot-$VERSION.jar
```

## Configuration for development environment
Use /config directory [External Application Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.files)
- Create property file in `/resources/config/`
  web/src/main/resources/config/MyConfig.properties


## Admin API
The admin API (`/api/admin/**`) removes applications and agents.
It is disabled until you set a password:
```
admin.password=<your password>
```
Callers send the password in the `password` parameter.

If another module authorizes `/api/admin/**`, set `admin.password.enabled=false` to skip the password check.
