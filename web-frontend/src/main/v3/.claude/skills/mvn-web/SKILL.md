---
name: mvn-web
description: web-frontend, web, web-starter Maven 모듈을 skipTests 옵션으로 순차 빌드합니다.
---

# Maven Web 빌드

## 0단계: Pinpoint 루트 디렉토리 탐색

현재 작업 디렉토리부터 상위 디렉토리로 올라가며 `web-frontend`, `web`, `web-starter` 서브디렉토리를 포함하는 `pinpoint`라는 이름의 가장 가까운 상위 디렉토리를 찾습니다. 이 경로를 `PINPOINT_ROOT`로 저장합니다.

```bash
PINPOINT_ROOT=$(d="$PWD"; while [ "$d" != "/" ]; do if [ "$(basename "$d")" = "pinpoint" ] && [ -d "$d/web-frontend" ] && [ -d "$d/web" ] && [ -d "$d/web-starter" ]; then echo "$d"; break; fi; d="$(dirname "$d")"; done)
```

`PINPOINT_ROOT`가 비어 있으면 즉시 중단하고 pinpoint 루트 디렉토리를 찾을 수 없다고 보고합니다.

## 1~3단계: 모듈 순차 빌드

아래 모듈에 대해 정해진 순서대로 `mvn clean install -DskipTests=true`를 순차 실행합니다. 각 단계는 반드시 성공해야 다음 단계로 진행할 수 있습니다. 실패 시 즉시 중단하고 오류를 보고합니다.

1. `cd $PINPOINT_ROOT/web-frontend && mvn clean install -DskipTests=true`
2. `cd $PINPOINT_ROOT/web && mvn clean install -DskipTests=true`
3. `cd $PINPOINT_ROOT/web-starter && mvn clean install -DskipTests=true`
