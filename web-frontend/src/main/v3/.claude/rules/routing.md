---
paths:
  - "apps/web/src/routes/**"
  - "packages/ui/src/loader/**"
  - "packages/ui/src/constants/path.ts"
---

# 라우팅 규칙

## 구조
- `createBrowserRouter`를 사용하는 React Router v6
- 라우트는 `apps/web/src/routes/index.tsx`에 정의
- 라우트 경로는 `packages/ui/src/constants/path.ts`의 `APP_PATH`에 정의
- 서브 경로 배포를 위한 `BASE_PATH` 환경 변수

## 새 라우트 추가 방법
1. `packages/ui/src/constants/path.ts`의 `APP_PATH`에 경로 상수 정의
2. `packages/ui/src/pages/`에 페이지 컴포넌트 생성
3. `apps/web/src/pages/`에 얇은 페이지 래퍼 생성 (필요 시 `useAtomValue`로 `configurationAtom` 읽기)
4. 날짜 검증이 필요한 경우 `packages/ui/src/loader/`에 라우트 로더 생성
5. `apps/web/src/routes/index.tsx`의 `InitialFetchOutlet` children에 라우트 추가 (설정 페이지는 `ConfigurationOutlet`)
6. 코드 분할을 위해 `React.lazy()` 사용 (기본 라우트 제외)

## 중첩 레이아웃 라우트 구조
라우트는 중첩 `<Outlet />` 컴포넌트를 통해 공통 관심사를 처리합니다:
```
SideNavigationOutlet
  ├── /apiCheck (InitialFetchOutlet 밖)
  └── InitialFetchOutlet (설정 조회, URL → 아톰 동기화)
       ├── 페이지 라우트 (로더 포함)
       ├── ConfigurationOutlet (설정 레이아웃으로 설정 페이지 감쌈)
       │    └── 설정 페이지 라우트
       └── * (NotFound)
```

## 라우트 로더 (`packages/ui/src/loader/`)
- URL 날짜 파라미터(from/to) 검증
- 유효하지 않으면 올바른 날짜 형식으로 리다이렉트
- URL 파라미터에서 application 파싱
- 유효한 날짜 범위를 위해 `periodMax` 설정과 대조
- 표준 형식으로 `SEARCH_PARAMETER_DATE_FORMAT` 사용

## URL 패턴
- 애플리케이션 라우트: `/:pageName/:application?`
- 설정 라우트: `/config/:configPage`
- 날짜 파라미터: `?from=YYYY-MM-DD-HH-mm-ss&to=YYYY-MM-DD-HH-mm-ss`
- URL의 애플리케이션 형식: `applicationName@serviceType`
