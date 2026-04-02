---
description: Maven build commands for multi-module project, frontend build
globs:
---

## Build Commands

Maven wrapper (`./mvnw`) is the build tool. This is a multi-module Maven project with 30+ top-level modules.

```bash
# Build everything (skip tests)
./mvnw install -Dmaven.test.skip=true

# Build specific modules with dependencies
./mvnw install -pl agent -am -Dmaven.test.skip=true
./mvnw install -pl collector -am -Dmaven.test.skip=true
./mvnw install -pl web -am -Dmaven.test.skip=true

# Build web without frontend
./mvnw install -pl web -am -Dbuild.frontend.skip=true -Dmaven.test.skip=true
```

## Frontend

Located at `web-frontend/src/main/v3/`. Built via Maven frontend plugin using Yarn. Can be skipped with `-Dbuild.frontend.skip=true`.