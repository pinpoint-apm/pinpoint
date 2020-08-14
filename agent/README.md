# pinpoint-agent

## build
```
mvnw install -pl agent -am -Dmaven.test.skip=true
```   

```
$ROOT_DIR/agent> ../mvnw install -pl agent -am -Dmaven.test.skip=true -f ../pom.xml
```

## executable file
```
target/pnipoint-agent-$VERSION/pinpoint-bootstrap-$VERSION.jar
```