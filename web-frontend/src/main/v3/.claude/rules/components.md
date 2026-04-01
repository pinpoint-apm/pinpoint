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
- 설정이 필요한 페이지는 `useAtomValue`로 `configurationAtom`에서 읽음
- 패턴:
  ```tsx
  import { useAtomValue } from 'jotai';
  import { SomePageComponent } from '@pinpoint-fe/ui';
  import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

  export default function SomePage() {
    const configuration = useAtomValue(configurationAtom);
    return <SomePageComponent configuration={configuration} />;
  }
  ```

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
