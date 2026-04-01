---
name: create-component
description: 프로젝트의 shadcn/ui + Tailwind 패턴에 따라 새 React 컴포넌트를 생성합니다. UI 프리미티브(버튼, 다이얼로그 등) 또는 도메인 컴포넌트(ServerMap, Inspector 등)를 새로 만들 때 반드시 이 스킬을 사용하세요.
---

# 새 컴포넌트 생성

`$ARGUMENTS` 설명을 가진 `$0` 컴포넌트를 생성합니다.

## 1. 컴포넌트 유형 결정

### UI 프리미티브 (packages/ui/src/components/ui/)
재사용 가능한 일반 UI 컴포넌트(버튼 변형, 다이얼로그, 입력 등):
- shadcn/ui 패턴 준수: Radix UI + CVA + Tailwind
- `@pinpoint-fe/ui/src/lib/utils`의 `cn()` 사용
- 적절한 경우 Radix Slot을 통한 `asChild` 지원
- ref 전달을 위한 `React.forwardRef` 사용
- CVA(class-variance-authority)로 variants 정의

### 도메인 컴포넌트 (packages/ui/src/components/{DomainName}/)
기능별 컴포넌트(ServerMap, ErrorAnalysis 등):
- `packages/ui/src/components/` 아래 서브디렉토리 생성
- UI 프리미티브를 조합하여 구성
- 데이터 페칭에 React Query 훅 사용
- 공유 상태에 Jotai 아톰 사용
- 비동기 콘텐츠를 Suspense + ErrorBoundary로 감싸기

## 2. 기존 예시 읽기
- UI 프리미티브: `packages/ui/src/components/ui/button.tsx` 또는 `dialog.tsx` 읽기
- 도메인 컴포넌트: 타겟과 유사한 기존 도메인 컴포넌트 읽기

## 3. 컴포넌트 생성

### UI 프리미티브 템플릿
```tsx
import * as React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@pinpoint-fe/ui/src/lib/utils';

const componentVariants = cva('base-classes', {
  variants: { variant: { default: '...', outline: '...' }, size: { default: '...', sm: '...' } },
  defaultVariants: { variant: 'default', size: 'default' },
});

export interface ComponentProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof componentVariants> {}

const Component = React.forwardRef<HTMLDivElement, ComponentProps>(
  ({ className, variant, size, ...props }, ref) => (
    <div ref={ref} className={cn(componentVariants({ variant, size, className }))} {...props} />
  ),
);
Component.displayName = 'Component';
export { Component, componentVariants };
```

### 도메인 컴포넌트 템플릿
```tsx
import React, { Suspense } from 'react';
import { ErrorBoundary } from 'react-error-boundary';
import { useTranslation } from 'react-i18next';
import { useAtomValue } from 'jotai';
import { cn } from '@pinpoint-fe/ui/src/lib/utils';
import { someSearchParamAtom } from '@pinpoint-fe/ui/src/atoms';
import { useGetMyData } from '@pinpoint-fe/ui/src/hooks/api';

export interface MyComponentProps {
  className?: string;
}

const MyComponentContent = ({ className }: MyComponentProps) => {
  const { t } = useTranslation();
  const searchParam = useAtomValue(someSearchParamAtom);
  const { data } = useGetMyData({ param: searchParam });

  if (!data) return null;

  return <div className={cn('...', className)}>{/* 렌더링 */}</div>;
};

export const MyComponent = ({ className }: MyComponentProps) => (
  <ErrorBoundary fallback={<div>Error</div>}>
    <Suspense fallback={<div>Loading...</div>}>
      <MyComponentContent className={className} />
    </Suspense>
  </ErrorBoundary>
);
```

## 4. 스타일링 규칙
- Tailwind CSS만 사용 — 인라인 스타일, CSS 모듈 금지
- 테마에는 CSS 변수 사용: `var(--ui-primary)`, `var(--ui-border)`
- 모든 className 합성에 `cn()` 사용
- Tailwind 브레이크포인트로 반응형 디자인

## 5. 완료
- `packages/ui/src/components/index.ts` (또는 해당 서브디렉토리의 `index.ts`)에서 컴포넌트 익스포트
- `yarn build`로 TypeScript 오류 없음 확인
