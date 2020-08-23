# pinpoint-agent

## build
```
mvnw install -pl agent -am -Dmaven.test.skip=true
```   

```           
cd agent
../mvnw install -pl agent -am -Dmaven.test.skip=true -f ../pom.xml
```

## executable file
```
/agent/target/pinpoint-agent-$VERSION
```