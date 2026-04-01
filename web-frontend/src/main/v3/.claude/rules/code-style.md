# 코드 스타일 규칙

## 포맷팅 (Prettier)
- 줄 너비: 100자
- 문자열에 작은따옴표 사용
- 객체와 배열에 후행 쉼표
- 세미콜론 필수
- 2칸 들여쓰기

## TypeScript
- Strict 모드 활성화
- 객체 형태에는 `interface`, 유니온/인터섹션에는 `type` 사용
- API 응답 타입은 네임스페이스 패턴 사용: `namespace GetServerMap { interface Response { ... } }`
- 타입 파일 위치: `packages/ui/src/constants/types/`
- `any` 사용 금지 — `unknown`을 사용하고 타입을 좁혀서 사용
- 리터럴 타입에는 `const` 단언 선호

## 임포트
- `@pinpoint-fe/ui`에서 딥 임포트: `@pinpoint-fe/ui/src/constants`, `@pinpoint-fe/ui/src/hooks` 등
- 임포트 그룹: React/외부 라이브러리 먼저, 그 다음 `@pinpoint-fe/*`, 마지막에 상대 경로
- 네임드 익스포트 사용; 페이지와 지연 로딩 컴포넌트를 제외한 default 익스포트 지양

## 네이밍 규칙
- 컴포넌트: PascalCase (`ServerMapCore.tsx`)
- 훅: `use` 접두사의 camelCase (`useGetServerMapDataV2`)
- API 훅: GET은 `useGet` 접두사, POST는 `usePost` 접두사, PATCH는 `usePatch`
- 아톰: `Atom` 접미사의 camelCase (`serverMapDataAtom`)
- 상수: 열거형/설정 키는 SCREAMING_SNAKE_CASE, 객체는 camelCase
- 유틸리티 함수: camelCase (`formatNumber`, `getDateRange`)
- 테스트 파일: 소스와 함께 위치하는 `*.test.ts` 또는 `*.test.tsx`

## ESLint
- `eslint.config.js`의 flat config, TypeScript-ESLint + Prettier 통합
- React Hooks 규칙 적용 (exhaustive deps)
- 주석에 명확한 이유 없이 ESLint 규칙 비활성화 금지
