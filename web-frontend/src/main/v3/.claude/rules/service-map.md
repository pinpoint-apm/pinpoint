# Service / ServiceMap 규칙

`experimental.enableServiceMap` 설정이 켜져 있을 때만 존재하는 개념이다. 꺼져 있으면 백엔드가
모든 요청을 기본 service(`DEFAULT`)로 해석하므로, 화면도 service 개념이 없는 것처럼 동작해야 한다.

## 로드맵 (중요 — 설계 판단의 전제)

**servermap 메뉴는 없어지고 servicemap이 그 자리를 대체한다.** 코드나 git 이력만 봐서는 알 수 없는
정보라서 여기에 적어둔다. 이 전제 때문에 아래 판단들이 나온다.

### servicemap은 servermap의 껍데기를 재사용한다 (복제하지 않는다)

```
pages/ServiceMap.tsx  →  ServerMapPage 를 감싸는 얇은 래퍼 (MapView 만 교체)
ServerMapPage         →  헤더 + application 선택 박스 + 날짜 선택기 + 좌우 분할 레이아웃
                         └ 우측 패널로 ServerMapChartsBoard 를 직접 렌더
```

`ServerMap`이라는 이름이 붙어 있어도 **그 파일들이 servicemap 화면을 그리는 코드**다. servermap이
사라지면 이 껍데기가 그대로 servicemap의 껍데기가 된다. 그래서 복제하지 않고 플래그로 분기한다.
복제해 두면 나중에 두 벌 중 한 벌을 지우면서 그 사이 갈라진 수정들을 다시 맞춰야 한다.

### `requiresApplication` 분기는 임시 코드가 아니다

servermap이 사라진 뒤에도 **DEFAULT service 사용자는 여전히 application을 골라야 한다.**
(이슈 #10373: "service 단위의 노드표현을 DEFAULT때도 표현해야하므로 DEFAULT service
사용자들도 servicemap을 사용해야합니다.") 두 모드는 servicemap에 영구히 남는다.

### servermap이 실제로 빠질 때 정리할 것

- `ServerMapPage` / `ServerMapChartsBoard` → servicemap 전용이 되므로 rename
- `ServiceMapPage`가 `ServerMapPage`를 감싸는 구조 → 한 페이지로 합치기
- `loader/serverMap.ts`, `loader/serverMap.test.ts` → 파일째로 삭제
  (그래서 serviceMap 로더는 날짜 정규화를 **일부러 복사**해 자기 완결적으로 두었다.
  공용 모듈로 빼면 그 시점에 "호출자가 하나뿐인 모듈"을 다시 정리해야 한다.)

  > **이 "복사" 근거는 servermap과 공유하는 코드에만 적용된다.** servicemap 로더끼리
  > (`serviceMap.ts` ↔ `serviceMapRealtime.ts`) 겹치는 부분은 둘 다 끝까지 남으므로 복사하지
  > 않는다. 경로 분해는 `parseServiceScopedPath` 하나를 공유한다 — 세그먼트 규칙이 바뀔 때
  > 고칠 곳이 하나여야 한다.
- 실시간 보기 → `/serviceMap/realtime`이 이미 있다. `RealtimePage`/`Realtime`(컴포넌트)은
  `MapView` prop으로 map만 갈아 끼우는 구조이므로, servermap이 빠지면 `getRealtimePath`와
  `loader/realtime.ts`, `pages/ServerMap/Realtime.tsx`를 지우고 기본값을 servicemap 쪽으로 옮긴다.
- `enableServiceMap` 설정 분기 → 설정이 없어지면 `useIsDefaultService`가 "DEFAULT인가?"만 보면 됨

## servicemap의 두 모드

map이 어떤 application을 모으는가만 다르다. **DEFAULT도 다른 service와 똑같이 선택된
service다** — 모든 조회에 `pServiceName` 헤더가 실리고 캐시도 service 단위로 갈린다.

| service | map | application 선택 박스 |
|---|---|---|
| `DEFAULT` | 고른 application 하나를 기준으로 그린다 | 노출 (골라야 그린다) |
| 그 외 | 소속된 **모든** application을 모아 그린다 | 미노출 (고를 대상이 없다) |

- 판단은 `useIsDefaultService()` 하나로 한다. `ServerMapPage`에는 `requiresApplication` prop으로
  전달된다(기본값 `true` → servermap은 영향 없음).
- 백엔드도 같은 규칙이다: `MapController#getSourceApplications`.
- application이 많아 노드가 과도해지면 service를 나누도록 가이드한다 (선택 박스를 주지 않는 이유).
- 비DEFAULT 모드에서는 기준 application이 없으므로: 특정 노드를 센터링/선택하지 않고,
  경로에 들어온 application 세그먼트는 지운다.
- **두 모드는 실시간 보기(`/serviceMap/realtime`)에도 그대로 있다.** 비DEFAULT에서는 우측 패널
  (스캐터/액티브 스레드)의 조회 대상이 map에서 클릭한 노드로 정해진다. 아무것도 고르지 않았으면
  map만 그리고, 우측 패널은 조회를 시작하지 않은 채 노드를 고르라는 안내 문구를 띄운다
  (`SERVER_MAP.SELECT_NODE_FOR_CHART`, `SERVER_MAP.REAL_TIME.SELECT_NODE`).
  로딩 스켈레톤을 그대로 두면 영원히 로딩 중인 화면처럼 보이기 때문이다.

## serviceName은 URL 경로에 싣는다

`/{page}/{serviceName}/{applicationName}@{serviceType}?` — **세그먼트 표기 하나만** 쓴다.

- 진실의 원천은 URL이다. 전역 선택값(`selectedServiceAtom`)은 탭 간 공유 저장소라, 링크를 새 탭에
  열어 둔 뒤 원래 탭에서 service를 바꾸면 화면과 어긋난다.
- 요청 헤더(`resolveRequestService`)와 캐시 키(`serviceScopedQueryKeyHashFn`)가 **같은 규칙**에서
  파생돼야 한다. 다르면 헤더는 A service로 나가는데 캐시는 B service 키에 쌓인다.
- 싣는 화면 목록: `SERVICE_NAME_SEGMENT_PAGES` (`utils/helper/application.ts`).
  **앞으로는 serviceName을 싣는 것이 기본**이고, 아직 안 옮긴 화면은 줄어드는 예외다.
  그 경로에서는 serviceName을 읽을 수 없어 전역 선택값으로 폴백한다.
- serviceName은 백엔드가 형식을 검증하지 않으므로(`ServiceNameRequest`에 제약이 없다) `/`나 `@`가
  들어올 수 있다. 반드시 `encodeURIComponent`로 싣는다.
- **읽을 때는 인코딩된 raw pathname을 넘긴다.** react-router의 `params`는 디코딩된 값이라
  `%2F`가 `/`로 풀려 세그먼트 경계가 어긋난다. 로더에서 특히 주의.
- 첫 세그먼트가 `{app}@{type}`으로 파싱되면 serviceName이 아니다. serviceName 세그먼트가 생기기
  전 형태(`/serviceMap/myApp@TOMCAT`)의 링크·북마크를 살리기 위한 가드다.

## 함정 (실제로 겪은 것들)

- **아톰은 화면 remount로 지워지지 않는다.** `InitialFetchOutlet`의 `key={requestService}`는
  컴포넌트 state만 초기화한다. service를 바꿀 때 이전 service의 map에서 고른 선택
  (`serverMapCurrentTargetAtom`, `currentServerAtom`)을 명시적으로 비워야 한다. 안 비우면
  ChartsBoard가 없는 노드를 기준으로 조회를 시작하고, 새 경로에는 기준 application도 없어서
  `applicationName` 없는 요청이 나가 400을 받는다. → `useClearApplicationOnServiceChange`
- **경로에 남은 application 세그먼트가 통계 조회의 기준이 된다.** 비DEFAULT 모드에서 다른 화면
  링크를 타고 application이 실려 들어오면, 클릭한 노드와 다른 application의 수치를 보여준다.
- **통계 API는 기준 application이 필수다.** service 전체 map에는 URL에 application이 없으므로,
  선택된 노드(링크는 출발지 노드)를 기준으로 삼는다. → `useGetHistogramStatistics`의
  `fallbackApplication`
- **`useSuspenseQuery`는 `enabled`를 지원하지 않는다.** `useGetApdexScore`처럼 `shouldPoll`에 따라
  suspense를 쓰는 훅은 `enabled`로 막을 수 없다. `skipToken`을 쓰면 영원히 suspend 되므로,
  컴포넌트에서 조회 대상이 없을 때 fetcher를 마운트하지 않는 쪽으로 막는다.

## 관련 파일

| 관심사 | 위치 |
|---|---|
| DEFAULT 여부 판단 | `hooks/utility/useIsDefaultService.ts` |
| 경로에 실린 serviceName 읽기 | `utils/helper/application.ts` (`getServiceNameFromPath`) |
| 경로 분해 (로더용) | `utils/helper/application.ts` (`parseServiceScopedPath`) |
| 경로 만들기 | `utils/helper/route.ts` (`getServiceMapPath`, `getServiceMapRealtimePath`, `getTransactionListPath`) |
| 요청 헤더 주입 | `hooks/api/serviceNameFetchInterceptor.ts` |
| service 단위 캐시 키 | `hooks/api/reactQueryHelper.tsx` (`serviceScopedQueryKeyHashFn`) |
| service 변경 시 초기화 | `hooks/utility/useClearApplicationOnServiceChange.ts` |
| 라우트 로더 | `loader/serviceMap.ts`, `loader/serviceMapRealtime.ts` |
