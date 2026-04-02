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

# Integration tests
./mvnw verify

# With coverage (JaCoCo)
./mvnw -Pcode.coverage package

# Plugin integration tests
./mvnw clean install -Pit-module -pl agent-module/plugins-it
```

Test stack: JUnit 5, Mockito 4, Spring Test, TestContainers, AssertJ.