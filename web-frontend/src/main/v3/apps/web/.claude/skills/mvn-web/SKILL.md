---
name: mvn-web
description: Build web-frontend, web, and web-starter Maven modules sequentially with skipTests.
---

# Maven Web Build

## Step 0: Find the Pinpoint root directory

Starting from the current working directory, traverse up the directory tree to find the nearest ancestor directory named `pinpoint` that contains `web-frontend`, `web`, and `web-starter` subdirectories. Store this path as `PINPOINT_ROOT`.

```bash
PINPOINT_ROOT=$(d="$PWD"; while [ "$d" != "/" ]; do if [ "$(basename "$d")" = "pinpoint" ] && [ -d "$d/web-frontend" ] && [ -d "$d/web" ] && [ -d "$d/web-starter" ]; then echo "$d"; break; fi; d="$(dirname "$d")"; done)
```

If `PINPOINT_ROOT` is empty, stop and report that no pinpoint root directory was found.

## Step 1-3: Build modules sequentially

Run `mvn clean install -DskipTests=true` sequentially for the following modules in strict order. Each step MUST complete successfully before proceeding to the next. If any step fails, stop immediately and report the error.

1. `cd $PINPOINT_ROOT/web-frontend && mvn clean install -DskipTests=true`
2. `cd $PINPOINT_ROOT/web && mvn clean install -DskipTests=true`
3. `cd $PINPOINT_ROOT/web-starter && mvn clean install -DskipTests=true`
