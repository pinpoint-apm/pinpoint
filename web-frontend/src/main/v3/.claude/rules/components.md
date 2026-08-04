---
paths:
  - "packages/ui/src/components/**/*.tsx"
  - "apps/web/src/pages/**/*.tsx"
  - "apps/web/src/components/**/*.tsx"
---

# React 컴포넌트 규칙

## 페이지 컴포넌트 (apps/web/src/pages/)
- 페이지는 얇은 래퍼: `@pinpoint-fe/ui`에서 페이지 컴포넌트를 임포트하여 렌더링
- 레이아웃 감싸기는 `apps/web/src/routes/index.tsx`의 중첩 레이아웃 라우트(`SideNavigationOutlet`, `InitialFetchOutlet`, `ConfigurationOutlet`)에서 처리
- **configuration은 prop으로 내려주지 않음** — 필요한 컴포넌트가 `useConfiguration()`으로 직접 읽음
- 넘길 prop이 없는 래퍼는 재수출로 끝냄:
  ```tsx
  export { SomePageComponent as default } from '@pinpoint-fe/ui';
  ```
- 이 저장소 전용 prop이 있을 때만 래퍼 컴포넌트를 유지:
  ```tsx
  import { SomePageComponent } from '@pinpoint-fe/ui';
  import { ApplicationCombinedList } from '@pinpoint-fe/web/src/components/Application/ApplicationCombinedList';

  export default function SomePage() {
    return <SomePageComponent ApplicationList={ApplicationCombinedList} />;
  }
  ```

## configuration 읽기
- `packages/ui`는 `useConfiguration()`(`@pinpoint-fe/ui/src/hooks`)으로 `configurationAtom`을 읽음 — prop으로 받지 않음
- 저장소는 ui의 `configurationAtom` 하나뿐. `apps/web`에서 별도 atom을 만들지 않음
- `Configuration`을 확장한 저장소는 래퍼 훅을 하나 두고, 호출부는 타입 인자 없이 사용:
  ```tsx
  // apps/web/src/hooks/useConfiguration.ts
  export const useConfiguration = () => useCommonConfiguration<Configuration>();
  ```
- React 밖(라우트 로더)에서는 atom이 아직 비어 있으므로 `getConfiguration()`을 직접 호출

## 도메인 컴포넌트 (packages/ui/src/components/)
- 도메인별 컴포넌트는 자체 서브디렉토리에 배치 (예: `ServerMap/`, `ErrorAnalysis/`)
- 비동기 데이터에는 Suspense 경계와 ErrorBoundary 사용
- `components/ui/`의 UI 프리미티브를 조합하여 구성
- 타입이 지정된 props 인터페이스 — 컴포넌트 위에 props 정의

## UI 프리미티브 (packages/ui/src/components/ui/)
- shadcn/ui 패턴 준수: Radix UI 프리미티브 + CVA(class-variance-authority) + Tailwind
- className 병합에는 `@pinpoint-fe/ui/src/lib/utils`의 `cn()` 사용 (clsx + tailwind-merge)
- 적절한 경우 Radix Slot을 통한 `asChild` prop 지원
- ref 전달이 필요한 컴포넌트에는 `React.forwardRef` 사용
- 다중 변형 컴포넌트(button, badge 등)에는 CVA로 `variants` 정의

## 스타일링
- Tailwind CSS 클래스만 사용 — 인라인 스타일이나 CSS 모듈 금지
- 커스텀 색상은 CSS 변수 사용: `--ui-primary`, `--ui-border` 등
- 상태 색상: `status-success`, `status-good`, `status-warn`, `status-fail`
- 속도 색상: `fast`, `normal`, `delay`, `slow`, `error`
- `class` 전략을 통한 다크 모드 지원

## 지연 로딩
- ServerMap(기본 라우트)을 제외한 모든 페이지는 `React.lazy()` 사용
- 지연 컴포넌트는 `apps/web/src/routes/index.tsx`에 정의
