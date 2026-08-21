# dev-mock — 로컬 확인용 임시 mock (#10497)

> **이 디렉터리는 임시입니다.** 사내 이슈(#10497)
> 확인이 끝나면 아래 "삭제 방법"대로 지우면 됩니다. 프로덕션 번들에는 들어가지 않습니다
> (`apply: 'serve'`라 vite dev 서버에서만 동작합니다).

## 왜 필요한가

#10497은 **service가 둘 이상이고 서로 호출하는** 저장소에서만 재현됩니다.

```
(A service) -> (B service)
```

A service의 servicemap에는 B service가 group 노드로 함께 그려지고, 그 group을 펼쳐 `b-1`을 고르면
우측 ChartsBoard 조회가 `pServiceName: B`로 나가야 합니다(고치기 전에는 화면의 service인 `A`로 나감).
로컬 저장소에는 보통 service가 하나뿐이라 이 상황 자체를 만들 수 없어, dev 서버가 map 응답을
대신 내려줍니다.

## 사용 방법

```bash
yarn dev:mock          # = MOCK_SERVICE_MAP=1 yarn dev
```

그 다음 브라우저에서:

```
http://localhost:3000/serviceMap/A
```

1. map에 `a-1`, `a-2` (service **A**) 와 `B` group 노드가 그려집니다.
2. `B` group 노드를 클릭하면 팝업에 `b-1`, `b-2`가 나옵니다. `b-1`을 고릅니다.
3. 우측 ChartsBoard에서 **VIEW SERVERS**를 눌러 서버 목록의 agent 이름을 봅니다.
   - `b-1-agent / pServiceName=B` → 정상 (고쳐진 동작)
   - `b-1-agent / pServiceName=A` → 이슈 재현 (화면의 service로 나감)
4. 터미널에도 `/api` 요청마다 실려 나간 헤더가 찍힙니다.
   ```
   [mock #10497] MOCK  /api/histogram/statistics pServiceName=B
   [mock #10497] pass  /api/applications pServiceName=A
   ```
   `MOCK`은 mock이 대신 응답한 것, `pass`는 실제 백엔드로 넘긴 것입니다.

`a-1`을 골랐을 때는 `pServiceName=A`로 나가야 합니다. 같이 확인하세요.

## 무엇을 가로채는가

| 경로 | 동작 |
|---|---|
| `/api/configuration` | 실제 백엔드 응답에 `experimental.enableServiceMap.value: true`만 강제로 켜서 내려줍니다. 백엔드가 안 떠 있으면 최소 설정으로 폴백합니다. |
| `/api/v2/services` | 실제 목록에 mock service `A`, `B`를 덧붙입니다. |
| `/api/servermap/serviceMap` | `pServiceName`이 `A`/`B`일 때만 mock map을 내려줍니다. |
| `/api/histogram/statistics`(`/links`), `/api/getApdexScore`, `/api/agents/overview`, `/api/getScatterData`, `/api/heatmap/applicationData` | 조회 대상이 mock application(`a-1`, `a-2`, `b-1`, `b-2`)이거나 service group 노드(`A`, `B`)일 때만 가로챕니다. 응답에 **요청에 실려 온 `pServiceName`을 그대로 박아** 내려줍니다. |
| 그 외 `/api/*` | 실제 백엔드로 그대로 넘어갑니다(로그만 남김). |

그래서 mock을 켜 둔 채로도 나머지 화면은 평소대로 쓸 수 있습니다.
실제 백엔드 주소는 `MOCK_UPSTREAM` 환경변수로 바꿀 수 있습니다(기본값 `http://localhost:8080`).

## 삭제 방법

`MOCK #10497` 로 grep하면 손댄 곳이 전부 나옵니다.

```bash
cd web-frontend/src/main/v3
grep -rn "MOCK #10497\|dev-mock\|dev:mock" apps/web --exclude-dir=node_modules --exclude-dir=dist
```

1. `apps/web/dev-mock/` 디렉터리 삭제
2. `apps/web/vite.config.ts` — `serviceMapMockPlugin` import 1줄과 `plugins`의 호출 1줄 삭제 (`// [MOCK #10497]` 주석 포함)
3. `apps/web/package.json` — `"dev:mock"` 스크립트 삭제
4. `package.json`(v3 루트) — `"dev:mock"` 스크립트 삭제
5. `apps/web/tsconfig.node.json` — `include`의 `"dev-mock/**/*.ts"` 삭제
