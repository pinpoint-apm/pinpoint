---
name: create-page
description: Pinpoint 프로젝트 패턴에 따라 새 페이지를 생성합니다. 애플리케이션에 새 라우트/페이지를 추가하거나, 새 화면을 만들거나, 기존 라우팅 구조에 페이지를 추가할 때 반드시 이 스킬을 사용하세요.
---

# 새 페이지 생성

**인자 형식**: `/create-page <PageName> [설명]`
- `$0` — 생성할 페이지 이름 (PascalCase, 예: `RealTimeDashboard`)
- `$ARGUMENTS` — 페이지 목적 설명 (선택 사항, 예: `실시간 메트릭 대시보드`)
- 인자 없이 실행하면 사용자에게 페이지 이름과 목적을 먼저 질문합니다.

`$ARGUMENTS` 설명을 가진 `$0` 페이지를 생성합니다.

다음 단계를 엄격히 따르세요:

## 1. 기존 페이지 분석
- `packages/ui/src/constants/path.ts`를 읽어 APP_PATH 패턴 파악
- `apps/web/src/routes/index.tsx`를 읽어 라우팅 구조 확인
- `packages/ui/src/pages/`의 유사한 기존 페이지와 `apps/web/src/pages/`의 래퍼 읽기

## 2. 파일 생성 (순서대로)

### 2a. 라우트 경로 추가
`packages/ui/src/constants/path.ts`의 `APP_PATH`에 새 경로 상수 추가.

### 2b. 페이지 컴포넌트 생성
`packages/ui/src/pages/{PageName}.tsx` 생성:
- `@pinpoint-fe/ui`에서 훅과 컴포넌트 임포트
- `configuration` prop 받기 (페이지 래퍼의 `configurationAtom`에서 읽음)
- 적절한 검색 파라미터 훅 사용
- 데이터 페칭에 React Query 훅 사용

### 2c. 페이지 컴포넌트 익스포트
`packages/ui/src/pages/index.ts`에서 페이지 익스포트 (없으면 생성).

### 2d. 라우트 로더 생성 (날짜 파라미터가 필요한 경우)
기존 로더의 패턴을 따라 `packages/ui/src/loader/{pageName}.ts` 생성:
- URL 파라미터에서 application 파싱
- `periodMax` 설정에 대해 날짜 범위 검증
- 유효하지 않으면 올바른 날짜로 리다이렉트

### 2e. 페이지 래퍼 생성
`apps/web/src/pages/<PageName>.tsx` 생성:
```tsx
// <PageName>을 실제 페이지 이름으로 교체 (예: Inspector, ErrorAnalysis)
import { useAtomValue } from 'jotai';
import { <PageName>Page } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

export default function <PageName>() {
  const configuration = useAtomValue(configurationAtom);
  return <<PageName>Page configuration={configuration} />;
}
```

### 2f. 라우트 추가
`apps/web/src/routes/index.tsx`에 라우트 추가:
- 상단에 lazy 임포트 추가
- `InitialFetchOutlet` children에 라우트 항목 추가 (설정 페이지는 `ConfigurationOutlet`)
- 로더와 엘리먼트 포함

## 3. i18n 키 추가
사용자 노출 문자열을 `packages/ui/src/constants/locales/`의 `en.json`과 `ko.json` 모두에 추가.

## 4. 검증
- 라우트 경로가 고유한지 확인
- 모든 임포트가 올바른지 확인
- 라우트 트리에서 페이지가 올바른 레이아웃 outlet 내에 배치되었는지 확인
