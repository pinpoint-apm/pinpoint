---
description: Test execution commands and test stack info
globs:
---

## Testing

```bash
# Run all unit tests
./mvnw test

# Run tests for a specific module
./mvnw test -pl commons

# Run a single test class
./mvnw test -pl commons -Dtest=ClassName

# Run a single test method
./mvnw test -pl commons -Dtest=ClassName#methodName

# Integration tests (failsafe, verify phase; skipped by default)
./mvnw verify -DskipITs=false

# With coverage (JaCoCo)
./mvnw -Pcode.coverage package

# Plugin integration tests
./mvnw clean install -f agent-module/plugins-it -DskipITs=false
```

Test stack: JUnit 5, Mockito 4, Spring Test, TestContainers, AssertJ.