---
paths:
  - "**/*.test.ts"
  - "**/*.test.tsx"
---

# Testing Rules

## Framework & Configuration
- Jest with ts-jest preset, jsdom test environment
- Test files co-located with source: `*.test.ts` / `*.test.tsx`
- Module alias: `@pinpoint-fe/ui/src/*` resolves to `<rootDir>/src/*`

## Running Tests
- All workspaces: `yarn test`
- Specific package: `yarn workspace @pinpoint-fe/ui test`
- Single file: `yarn workspace @pinpoint-fe/ui jest path/to/file.test.ts`

## Test Patterns

### Atom Tests
```typescript
import { renderHook, act } from '@testing-library/react';
import { useAtom } from 'jotai';
import { someAtom } from './someAtom';

describe('Test someAtom', () => {
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

### Utility Function Tests
```typescript
import { someUtil } from './someUtil';

describe('someUtil', () => {
  test('should handle normal input', () => {
    expect(someUtil(input)).toBe(expected);
  });

  test('should handle edge case', () => {
    expect(someUtil(edgeInput)).toBe(edgeExpected);
  });
});
```

## Guidelines
- Test behavior, not implementation details
- Name test files matching source: `myUtils.ts` → `myUtils.test.ts`
- Use descriptive test names: `should [behavior] when [condition]`
- For API hooks, mock `fetch` and verify query parameters and response handling
