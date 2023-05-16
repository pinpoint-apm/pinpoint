# build

## all
```
mvnw install -Dmaven.test.skip=true
```

## agent
```
mvnw install -pl agent -am -Dmaven.test.skip=true
```

## collector
```
mvnw install -pl collector -am -Dmaven.test.skip=true
```

## web
```
mvnw install -pl web -am -Dmaven.test.skip=true
```
Skip frontend build
```
mvnw install -pl web -am -Dbuild.frontend.skip=true -Dmaven.test.skip=true
```

