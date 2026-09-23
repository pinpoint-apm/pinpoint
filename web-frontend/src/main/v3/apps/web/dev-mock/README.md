# dev-mock — 로컬 확인용 임시 mock (#10497, #10744)

> **이 디렉터리는 임시입니다.** 사내 이슈(#10497, #10744)
> 확인이 끝나면 아래 "삭제 방법"대로 지우면 됩니다. 프로덕션 번들에는 들어가지 않습니다
> (`apply: 'serve'`라 vite dev 서버에서만 동작합니다).

두 mock은 서로 독립적입니다.

| mock | 켜는 법 | 무엇을 흉내 내는가 |
|---|---|---|
| #10497 servicemap | `yarn dev:mock` | service가 둘 이상이고 서로 호출하는 저장소 |
| #10744 권한 없음 | `yarn dev:forbidden` | 특정 (화면, application)에 대한 `403 Access denied` |

---

# #10744 — 권한 없음(403) mock

## 왜 필요한가

권한 검사(`@naverPermissionEvaluator`)의 구현이 이 저장소에 없어서 로컬 백엔드는 403을 내지
않습니다. dev 서버가 지정한 (화면, application) 조합만 백엔드로 넘기지 않고 403으로 대신
돌려줍니다. 응답 모양은 실제 서버와 같습니다(`CustomExceptionHandler#handleAccessDeniedException`).

```json
{ "type": "about:blank", "title": "Forbidden", "status": 403, "detail": "Access denied", "instance": "..." }
```

## 사용 방법

```bash
yarn dev:forbidden     # = MOCK_FORBIDDEN=1 yarn dev
```

규칙은 [`forbidden.json`](./forbidden.json)에 적습니다. **요청마다 다시 읽으므로 dev 서버를
재시작할 필요가 없습니다** — "관리자가 권한을 주었다"를 파일 수정만으로 재현하기 위해서입니다.

```json
{
  "forbidden": [
    { "page": "inspector", "application": "myApp" },
    { "page": "errorAnalysis", "application": "myApp" }
  ]
}
```

- `page`: `inspector` | `urlStatistic` | `threadDump` | `errorAnalysis` |
  `scatterFullScreenMode` | `heatmapFullScreenMode` | `config/agentStatistic` | `*`
- `application`: application 이름, 또는 `*`(생략해도 전체)

터미널에 가로챈 요청이 찍힙니다.

```
[mock #10744] 403   /api/inspector/applicationStat/chart application=myApp
```

## 시나리오별 확인 방법

아래는 모두 `yarn dev:forbidden`으로 띄운 뒤 `http://localhost:3000`에서 확인합니다.
규칙은 **한 API라도 403이면 그 화면은 권한 없음**입니다. 엔드포인트를 가리지 않습니다.

### 1. 403이면 권한 없음 화면이 뜬다

```json
{ "forbidden": [{ "page": "inspector", "application": "myApp" }] }
```

`/inspector/myApp@TOMCAT` → 본문이 권한 없음 화면으로 바뀝니다.

- **헤더(application 선택 박스·날짜 선택기)와 사이드 네비게이션은 그대로 남아야 합니다.**
- **에러 토스트가 뜨면 안 됩니다.** 403은 어디서든 토스트를 띄우지 않습니다.

### 2. 주 데이터가 아니어도 403이면 덮는다

규칙은 1번 그대로 두고 `/errorAnalysis/myApp@TOMCAT`으로 갑니다 → **여기도 권한 없음 화면**입니다.

`/api/errors/*`는 200으로 오지만, 사이드바의 agent 목록(`/api/agents`)이 inspector 권한이라
403이 나기 때문입니다. "한 API라도 403이면" 규칙이 적용되는 곳이고, 실제 백엔드에서도 권한이
기능별로 갈리므로(`hasInspectorPermission` ↔ `hasExceptionTracePermission`) 만들어질 수 있는
상태입니다.

터미널 로그로 무엇이 막혔는지 확인할 수 있습니다.

```
[mock #10744] 403   /api/agents application=myApp
```

### 3. 막히지 않은 화면은 정상이다

```json
{ "forbidden": [{ "page": "errorAnalysis", "application": "myApp" }] }
```

이 규칙은 `/api/errors/*`만 막습니다.

- `/errorAnalysis/myApp@TOMCAT` → 권한 없음
- `/inspector/myApp@TOMCAT` → **정상** (inspector가 부르는 API는 아무것도 막히지 않음)

### 4. 다른 application은 정상이다

규칙을 1번으로 되돌리고, **헤더의 application 선택 박스에서** `myApp2`를 고릅니다 →
`/inspector/myApp2@TOMCAT`이 정상으로 그려집니다.
(이 단계가 "권한 없음 화면에서도 빠져나갈 수 있어야 한다"를 확인하는 곳입니다.)

### 5. 권한이 생기면 정상으로 돌아온다

`/inspector/myApp@TOMCAT`에서 권한 없음 화면을 본 상태로 **dev 서버를 그대로 두고**
`forbidden.json`의 규칙을 지웁니다.

- **다른 화면에 갔다가 돌아오기**(예: Servermap → Inspector): 정상으로 그려져야 합니다.
  화면을 떠날 때 판정을 버리기 때문입니다(`useClearForbiddenOnPathChange`).
  이 처리가 없으면 재조회 없이 계속 권한 없음 화면이 뜹니다.
- **새로고침(F5)**: 판정은 메모리에만 있으므로 역시 정상으로 그려집니다.

### 6. map은 덮이지 않는다 (유일한 예외)

```json
{ "forbidden": [{ "page": "scatterFullScreenMode", "application": "myApp" }] }
```

- `/serverMap/myApp@TOMCAT` → **map은 그대로 있고** 스캐터만 실패합니다(인라인 에러, 토스트 없음).
  map API는 권한 검사가 없어 403이 나는 것은 언제나 **고른 노드**를 묻는 조회입니다. 그것으로
  화면을 덮으면 다른 노드를 고를 방법이 사라집니다.
- `/scatterFullScreenMode/myApp@TOMCAT` → **권한 없음 화면**. 확대 화면은 차트 하나가 곧
  페이지라 막히면 남는 것이 없습니다.

같은 엔드포인트가 어느 화면에서 불렸는지로 갈리는 것을 확인하는 곳입니다.
`/serviceMap/...`, `/filteredMap/...`, 실시간 보기도 map과 같습니다.

### 7. 화면 안에서 대상을 고르는 화면 (Agent Management)

```json
{ "forbidden": [{ "page": "inspector", "application": "myApp" }] }
```

`/config/agentManagement`에서 `myApp`을 고르면 목록 영역이 권한 없음으로 바뀝니다.
**상단 application 선택 박스는 남아야 하고, 거기서 `myApp2`를 고르면 곧바로 목록이 보여야
합니다.** 이 화면은 대상을 경로가 아니라 화면 안 상태로 고르므로 경로가 바뀌지 않고,
`useClearForbiddenOnPathChange`가 판정을 버릴 기회가 없습니다. 그래서 선택 박스의
`onClickApplication`이 판정을 함께 버립니다 — 그 처리가 없으면 다른 application을 골라도
계속 권한 없음 화면에 갇힙니다.

### 8. 저장(뮤테이션) 403은 토스트로 남는다

알람 규칙 저장처럼 **변경 API**의 403은 화면을 덮지 않습니다(`MutationCache`로 가므로 이
판정에 닿지 않습니다). 저장이 막힌 것과 화면을 볼 수 없는 것은 다른 일이라, 전자는 토스트로
알립니다. 이 mock은 GET만 가로채므로 재현하려면 실제 백엔드가 필요합니다.

## 주의

`forbidden.ts`의 `PAGE_API_PREFIXES`는 **백엔드의 `@PreAuthorize`와 같은 짝**이어야 합니다
(`page`는 화면이 아니라 "그 권한이 막는 API 묶음"입니다). 프론트는 엔드포인트를 가리지 않으므로
(`coversPageOnForbidden`은 경로만 봅니다) 이 표가 틀려도 화면 판정 자체는 동작하지만, 실제
백엔드에서 나지 않을 403을 만들어 내면 있지도 않은 상태를 확인하게 됩니다.

---

# #10497 — servicemap mock

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

`MOCK #10497` / `MOCK #10744` 로 grep하면 손댄 곳이 전부 나옵니다.

```bash
cd web-frontend/src/main/v3
grep -rn "MOCK #10497\|MOCK #10744\|dev-mock\|dev:mock\|dev:forbidden" apps/web --exclude-dir=node_modules --exclude-dir=dist
```

1. `apps/web/dev-mock/` 디렉터리 삭제
2. `apps/web/vite.config.ts` — mock plugin import 1줄과 `plugins`의 호출 2줄 삭제 (`// [MOCK ...]` 주석 포함)
3. `apps/web/package.json` — `"dev:mock"`, `"dev:forbidden"` 스크립트 삭제
4. `package.json`(v3 루트) — `"dev:mock"`, `"dev:forbidden"` 스크립트 삭제
5. `apps/web/tsconfig.node.json` — `include`의 `"dev-mock/**/*.ts"` 삭제

둘 중 하나만 지우려면 해당 파일(`index.ts`+`mockData.ts` / `forbidden.ts`+`forbidden.json`)과
그 plugin 호출·스크립트만 지우면 됩니다.
