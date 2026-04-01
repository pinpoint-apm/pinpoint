---
paths:
  - "**/*.test.ts"
  - "**/*.test.tsx"
---

# 테스트 규칙

## 프레임워크 및 설정
- Jest with ts-jest preset, jsdom 테스트 환경
- 테스트 파일은 소스와 함께 위치: `*.test.ts` / `*.test.tsx`
- 모듈 별칭: `@pinpoint-fe/ui/src/*`는 `<rootDir>/src/*`로 해석

## 테스트 실행
- 전체 워크스페이스: `yarn test`
- 특정 패키지: `yarn workspace @pinpoint-fe/ui test`
- 단일 파일: `yarn workspace @pinpoint-fe/ui jest path/to/file.test.ts`

## 테스트 패턴

### 아톰 테스트
```typescript
import { renderHook, act } from '@testing-library/react';
import { useAtom } from 'jotai';
import { someAtom } from './someAtom';

describe('someAtom', () => {
  test('should initialize with default value', () => {
    const { result } = renderHook(() => useAtom(someAtom));
    expect(result.current[0]).toEqual(expectedDefault);
  });

  test('should update correctly', () => {
    const { result } = renderHook(() => useAtom(someAtom));
    act(() => { result.current[1](newValue); });
    expect(result.current[0]).toEqual(newValue);
  });
});
```

### 유틸리티 함수 테스트
```typescript
import { someUtil } from './someUtil';

describe('someUtil', () => {
  test('should handle normal input', () => {
    expect(someUtil(input)).toBe(expected);
  });

  test('should handle edge cases', () => {
    expect(someUtil(edgeInput)).toBe(edgeExpected);
  });
});
```

## 가이드라인
- 구현 세부사항이 아닌 동작을 테스트
- 테스트 파일은 소스와 이름 일치: `myUtils.ts` → `myUtils.test.ts`
- 설명적인 테스트 이름 사용: `should [behavior] when [condition]`
- API 훅은 `fetch`를 모킹하고 쿼리 파라미터 및 응답 처리 검증
