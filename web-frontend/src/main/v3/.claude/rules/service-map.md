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
- filteredMap → 경로에 serviceName이 항상 실리므로 `FilteredMapPage`의 돌아갈 map 분기
  (`Servermap` ↔ `Servicemap`)와 로더의 serviceName 없는 형태 처리를 지운다.
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

## filteredMap 연결

servicemap에서 필터를 걸면 `/filteredMap/{serviceName}/{application}@{serviceType}`으로 새 탭이
열린다. 화면은 복제하지 않는다 — map을 그리는 API(`/api/servermap/filterServerMap`)가 같아서
`FilteredMapPage` 하나로 servermap/servicemap 양쪽을 받는다.

- **경로에 serviceName이 실려 있는지가 "어느 map에서 왔는가"다.** 그것으로 헤더의 돌아갈 링크를
  정한다(`Servicemap / Filtered` ↔ `Servermap / Filtered`). 그래서 `useFilteredMapParameters`의
  `serviceName`은 전역 선택값으로 **폴백하지 않는다**(`useServiceNameForLink`와 다른 점).
  폴백하면 servermap에서 온 화면도 servicemap에서 온 것처럼 보인다.
- 같은 이유로 **로더는 빠진 serviceName 세그먼트를 채워 넣지 않는다.** servicemap 로더와 반대다.
- 경로에 실을 serviceName은 `ServerMapPage`의 `serviceName` prop에서 온다(servicemap 계열에서만
  주어진다). filteredMap 안에서 필터를 더 걸 때는 지금 경로의 serviceName을 그대로 잇는다.
- 날짜 정규화는 `loader/mapDateRange.ts`를 servicemap 로더와 공유한다. 둘 다 servermap이 빠진
  뒤에도 남는 화면이라 규칙이 바뀔 때 고칠 곳이 하나여야 한다.
  (예전에는 filteredMap이 servermap 로더를 그대로 써서, 날짜를 고치는 리다이렉트가 화면을
  `/serverMap/...`으로 보내며 filter까지 떨어뜨렸다. from/to 외의 query string은 목적지에도 싣는다.)

### TODO: DEFAULT가 아닌 service

**아직 논의 중이다.** 지금은 DEFAULT와 똑같이 필터 대상 application을 싣는다
(`/filteredMap/myService/{application}@{serviceType}`). map은 service 전체를 그리지만
filteredMap은 기준 application 없이는 조회가 성립하지 않기 때문이다
(`useGetFilteredServerMapData`가 `applicationName`을 필수로 요구한다).
service 전체를 대상으로 필터를 걸 수 있게 할지 정해지면 경로 형태를 그때 맞춘다.

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

## map 노드/링크 id 형식이 API마다 다르다

| API | render | node key |
|---|---|---|
| `/api/servermap/serverMap` | `NodeRender.forServerMap()` | **항상** `applicationName^serviceType` (2단) |
| `/api/servermap/serviceMap` | `NodeRender.forServiceMap()` | **항상** `serviceName^applicationName^serviceType` (3단) |
| `/api/servermap/filterServerMap` | `NodeRender.detailedRender(mapProperties)` | **`enableServiceMap` 설정에 따라 2단/3단** |

link key는 노드 이름 둘을 `~`로 이은 것이라 같은 규칙을 따른다(`{from}~{to}`).

> **filteredMap의 형식은 "어디서 왔는가"가 아니라 설정에 달려 있다.** servermap에서 들어온
> filteredMap도 `enableServiceMap`이 켜져 있으면 3단으로 온다. 경로에 serviceName이 실렸는지와
> 무관하다.

- **id에서 application을 읽을 때는 `parseNodeApplication`을 쓴다.** 뒤 두 토큰이 application이다.
  URL 세그먼트용인 `getApplicationTypeAndName`으로 읽으면 3단에서 applicationName에
  `serviceName^applicationName`이 들어온다(정규식이 greedy).
- **노드/링크를 application으로 찾을 때는 `findNodeOfApplication`·`findLinkOfApplications`를 쓴다.**
  `key`의 뒤 두 토큰으로 비교하므로 2단/3단 어느 쪽이든 찾는다.
- serviceName은 escape되지 않으므로(applicationName만 `ApplicationNameEscaper`로 escape된다)
  앞쪽 토큰이 더 늘어날 수 있다. 그래서 **앞에서 세지 않고 뒤에서 센다.**
- **2단짜리 `nodeKey`/`linkKey` 필드와 비교하는 것으로는 부족하다.** 그 필드의 serviceType은
  `ServiceType.getName()` 형식인데 `key`와 URL은 `getDesc()` 형식이고, 둘이 다른 타입이 있다
  (`UNKNOWN_DB_EXECUTE_QUERY`→`UNKNOWN_DB`, `SPRING_ORM_IBATIS`→`SPRING`).
- 스캐터(`applicationScatterData`)의 키는 `ScatterDataMapView`가 `NodeName`으로 만들어
  **항상 2단**이다. `getApplicationKey`로 그대로 조회하면 된다.
- servicemap의 **service group**(접힌 service) 노드·링크는 id가 serviceName 하나뿐이라 기준
  application이 없다. filteredMap은 기준 application 없이 조회가 성립하지 않으므로 연결하지
  않는다 → 아래 "filteredMap으로 연결되는 것은 Application→Application뿐" 참고.
- 접히는 기준은 백엔드 `ServiceMapViewBuilder`의 `expandedServiceNames`다. 보고 있는 service는
  펼쳐지므로 그 service의 노드는 3단 app 노드로, 다른 service는 group으로 온다.

### filteredMap으로 연결되는 것은 Application→Application뿐

servicemap의 edge는 네 가지다. 이 중 **Application→Application만** filteredMap으로 연결한다.
나머지 셋(Application→Service, Service→Application, Service→Service)은 한쪽 끝이 service
group이라 그쪽 application이 없고, 그대로 열면 필터가 반쪽만 걸린 filteredMap이 열린다.
service 단위로 필터를 걸 수 있게 할지는 위 TODO와 같은 미결 사안이다.

**판별은 `subLinks`/`subNodes`로 한다.** 백엔드는 양쪽 끝이 모두 펼쳐진 service일 때만 평범한
링크로 내려주고(`ServiceMapViewBuilder#buildLinks`의 `fromExpanded && toExpanded`), 한쪽이라도
접혀 있으면 `type:'service'`로 묶어 내려준다. `flattenServiceMapResponse`가 그것을 `subLinks`에
담으므로, **`subLinks`가 있으면 곧 Application→Application이 아니다.** 노드도 같다(`subNodes`).
servermap/filteredMap 응답에는 이 필드가 없어 그 화면들의 동작은 달라지지 않는다.
→ `findServiceGroupNode`, `findServiceGroupLink` (`utils/helper/serviceMap.ts`)

막는 방식은 **우클릭 메뉴를 아예 열지 않는 것**이다. 필터를 걸 수 없는 노드에 메뉴를 띄우지 않는
기존 처리(`getTransactionInfo`가 undefined면 메뉴 없음)와 같은 방식이다. 메뉴를 띄우고 항목만
무반응으로 두면 눌러도 아무 일이 없는 죽은 버튼이 된다.

- **엣지의 `transactionInfo`를 비우는 방식은 안 된다.** 그 필드는 엣지 라벨(호출 수·평균
  응답시간, `renderEdgeLabel`)이 쓴다. group 엣지도 합산 수치는 보여줘야 하므로 별도 판별이 필요하다.
- 안전망으로 `getFilterTargetApplication`은 **링크의 양쪽 끝이 모두 application일 때만** 기준을
  돌려준다. 기준으로 삼을 한쪽만 보고 통과시키면 Application→Service가 새어나간다(출발지가
  WAS라 출발지를 기준으로 잡고 열린다). 어느 쪽이 기준인지(`sourceIsWas`)와 무관하게 양쪽을 본다.

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
| 경로 만들기 | `utils/helper/route.ts` (`getServiceMapPath`, `getServiceMapRealtimePath`, `getFilteredMapPath`, `getTransactionListPath`) |
| 요청 헤더 주입 | `hooks/api/serviceNameFetchInterceptor.ts` |
| service 단위 캐시 키 | `hooks/api/reactQueryHelper.tsx` (`serviceScopedQueryKeyHashFn`) |
| service 변경 시 초기화 | `hooks/utility/useClearApplicationOnServiceChange.ts` |
| 라우트 로더 | `loader/serviceMap.ts`, `loader/serviceMapRealtime.ts`, `loader/filteredMap.ts` |
| 로더의 날짜 정규화 (공유) | `loader/mapDateRange.ts` |
| filteredMap 경로 읽기 | `hooks/searchParameters/useFilteredMapParameters.ts` |
