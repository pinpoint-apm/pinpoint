---
name: write-test
description: 변경되었거나 테스트가 없는 Pinpoint 프론트엔드 코드에 대한 Jest 테스트를 작성합니다. 훅, 아톰, 유틸리티, 컴포넌트에 테스트가 필요하거나 커버리지를 높여야 할 때 반드시 이 스킬을 사용하세요.
---

# 테스트 작성

`$ARGUMENTS`의 코드에 대한 Jest 테스트를 작성합니다 (경로가 없으면 최근 변경된 파일 대상).

## 1단계: 테스트가 필요한 파일 탐색

인자가 없는 경우:

```bash
git diff upstream/master --name-only
```

변경된 각 `.ts` / `.tsx` 파일에 대해 해당하는 `.test.ts` / `.test.tsx` 파일이 있는지 확인합니다.
테스트가 **없는** 파일 목록 — 이것이 우선 타겟입니다.

## 2단계: 코드 분석

각 타겟 파일을 완전히 읽고 파악합니다:

- **수행하는 것** (구현이 아닌 동작)
- **받는 입력** (props, 인자, 아톰 값, URL 파라미터)
- **생성하는 출력** (반환 값, 렌더링 출력, 사이드 이펙트, 아톰 업데이트)
- **의존성** (fetch 호출, 아톰, 다른 훅)

## 3단계: 테스트 작성

`.claude/rules/testing.md`의 프로젝트 테스트 패턴을 따릅니다.

> **규칙: 테스트 이름(`describe` / `test` / `it` 문자열)은 반드시 영어로 작성합니다.**

### 유틸리티 함수 테스트

```typescript
import { theUtil } from "./theUtil";

describe("theUtil", () => {
  test("should return [expected behavior] given [normal input]", () => {
    expect(theUtil(normalInput)).toBe(expected);
  });

  test("should handle null/undefined input gracefully", () => {
    expect(theUtil(null)).toBe(fallback);
  });

  test("should handle edge cases", () => {
    expect(theUtil(edgeCase)).toBe(edgeExpected);
  });
});
```

### Jotai 아톰 테스트

```typescript
import { renderHook, act } from "@testing-library/react";
import { useAtom } from "jotai";
import { theAtom } from "./theAtom";

describe("theAtom", () => {
  test("should initialize with default value", () => {
    const { result } = renderHook(() => useAtom(theAtom));
    expect(result.current[0]).toEqual(defaultValue);
  });

  test("should update when setter is called", () => {
    const { result } = renderHook(() => useAtom(theAtom));
    act(() => {
      result.current[1](newValue);
    });
    expect(result.current[0]).toEqual(newValue);
  });
});
```

### API 훅 테스트 (useGetXxx)

```typescript
import { renderHook, waitFor } from "@testing-library/react";
import { createWrapper } from "../testUtils"; // QueryClient wrapper
import { useGetXxx } from "./useGetXxx";

describe("useGetXxx", () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test("should fetch data with correct endpoint and params", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ data: mockData }),
    });

    const { result } = renderHook(() => useGetXxx(params), {
      wrapper: createWrapper(),
    });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("/api/expected-endpoint"),
      expect.any(Object),
    );
    expect(result.current.data).toEqual(expectedData);
  });

  test("should not fetch when required params are missing", () => {
    const { result } = renderHook(() => useGetXxx(null), {
      wrapper: createWrapper(),
    });
    expect(result.current.isFetching).toBe(false);
    expect(global.fetch).not.toHaveBeenCalled();
  });

  test("should handle API error response", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 500,
      json: async () => ({ title: "Internal Server Error" }),
    });

    const { result } = renderHook(() => useGetXxx(params), {
      wrapper: createWrapper(),
    });
    await waitFor(() => expect(result.current.isError).toBe(true));
  });
});
```

### 라우트 로더 테스트

```typescript
import { loader } from "./loader";

describe("loader", () => {
  test("should redirect with correct date format when from/to are invalid", async () => {
    const request = new Request(
      "http://localhost/page?from=invalid&to=invalid",
    );
    const response = await loader({ request, params: {} });
    expect(response).toBeInstanceOf(Response); // redirect
  });

  test("should return null for valid date params", async () => {
    const now = Date.now();
    const request = new Request(
      `http://localhost/page?from=${now - 3600000}&to=${now}`,
    );
    const response = await loader({ request, params: {} });
    expect(response).toBeNull();
  });
});
```

## 4단계: 커버리지 목표

각 파일에 대해 다음을 커버하는 것을 목표로 합니다:

- [ ] 정상 / 해피 패스
- [ ] 빈 / null / undefined 입력
- [ ] 오류 상태 (API 실패, 잘못된 데이터)
- [ ] 경계값 (빈 배열, 최대값)
- [ ] 훅의 `enabled` 조건 (파라미터 없을 때 페칭 안 함)

## 5단계: 배치 및 등록

- 테스트 파일은 소스와 함께 위치: `myHook.ts` → `myHook.test.ts`
- 테스트 통과 확인을 위해 실행: `yarn workspace @pinpoint-fe/ui jest path/to/file.test.ts`

작성된 테스트와 통과/실패 여부를 보고합니다.
