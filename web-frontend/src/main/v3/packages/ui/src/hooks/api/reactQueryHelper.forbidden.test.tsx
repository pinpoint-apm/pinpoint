// ErrorToast transitively pulls the ECharts (ESM) stack that babel-jest does not
// transform, so stub it out. These mocks must run before reactQueryHelper is imported.
jest.mock('../../components/Error/ErrorToast', () => ({ ErrorToast: () => null }));
jest.mock('react-toastify', () => ({ toast: { error: jest.fn() } }));
jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useLocation: () => ({ pathname: globalThis.location.pathname }),
}));

import React from 'react';
import { act, render } from '@testing-library/react';
import { QueryClientProvider, useSuspenseQuery } from '@tanstack/react-query';
import { getDefaultStore } from 'jotai';
import {
  forbiddenNoticeMountCountAtom,
  forbiddenPathAtom,
  resetForbiddenTargetAtom,
} from '../../atoms/permission';
import { useIsForbiddenPath } from '../utility/useIsForbiddenPath';
import { queryClient } from './reactQueryHelper';

/**
 * 실제 react-query로 403 판정을 확인한다. (이슈 #10744)
 *
 * `reactQueryHelper.test.tsx`는 옵저버 수를 가짜로 끼워 넣어 판정 규칙만 본다. 그 방식으로는
 * **suspense 조회의 첫 요청이 옵저버 없이 나간다**는 사실이 드러나지 않아, 그 요청이 403을 받으면
 * 화면은 덮이지 않고 요청이 끝없이 반복되던 것을 놓쳤다. 여기서는 실제로 렌더해 요청 수를 센다.
 */

const store = getDefaultStore();

const forbidden = () => Object.assign(new Error('Access denied'), { status: 403 });

// 반복이 생겨도 테스트가 멈추지 않도록 일정 횟수 뒤에는 성공시킨다. 기대값은 언제나 1번이다.
const LOOP_GUARD = 5;

const flush = async () => {
  for (let i = 0; i < 20; i++) {
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 10));
    });
  }
};

class TestErrorBoundary extends React.Component<{ children: React.ReactNode }, { error: boolean }> {
  state = { error: false };

  static getDerivedStateFromError() {
    return { error: true };
  }

  render() {
    return this.state.error ? <div>error</div> : this.props.children;
  }
}

const renderSuspenseQuery = ({ withNotice }: { withNotice: boolean }) => {
  let calls = 0;
  const queryKey = [`/api/forbidden-test-${Math.random()}`];
  const queryFn = async () => {
    calls++;
    if (calls >= LOOP_GUARD) {
      return 'ok';
    }
    throw forbidden();
  };

  const Body = () => {
    useSuspenseQuery({ queryKey, queryFn });
    return <div>body</div>;
  };
  const content = (
    <TestErrorBoundary>
      <React.Suspense fallback={<div>loading</div>}>
        <Body />
      </React.Suspense>
    </TestErrorBoundary>
  );
  // 안내를 그리는 페이지를 흉내 낸다(`Inspector` 등이 하는 그대로).
  const NoticePage = () => (useIsForbiddenPath() ? <div>forbidden</div> : content);

  const result = render(
    <QueryClientProvider client={queryClient}>
      {withNotice ? <NoticePage /> : content}
    </QueryClientProvider>,
  );

  return { result, queryKey, getCalls: () => calls };
};

describe('page level 403 with a real suspense query', () => {
  beforeEach(() => {
    store.set(forbiddenPathAtom, undefined);
    store.set(forbiddenNoticeMountCountAtom, 0);
  });

  afterEach(() => {
    queryClient.clear();
  });

  test('covers the page for the first request of a screen that renders the notice', async () => {
    window.history.replaceState({}, '', '/inspector/app-name@TOMCAT');

    const { result, getCalls } = renderSuspenseQuery({ withNotice: true });
    await flush();

    expect(getCalls()).toBe(1);
    expect(store.get(forbiddenPathAtom)).toBe('/inspector/app-name@TOMCAT');
    expect(result.container.textContent).toBe('forbidden');
    result.unmount();
  });

  // 덮지 않는 화면은 에러를 캐시에 남겨 ErrorBoundary가 그리게 한다. 지우면 다시 묻는다.
  test.each(['/serverMap/app-name@TOMCAT', '/config/alarm'])(
    'keeps the error for the first request on %s, which does not cover the page',
    async (pathname) => {
      window.history.replaceState({}, '', pathname);

      const { result, queryKey, getCalls } = renderSuspenseQuery({ withNotice: false });
      await flush();

      expect(getCalls()).toBe(1);
      expect(store.get(forbiddenPathAtom)).toBeUndefined();
      expect(result.container.textContent).toBe('error');
      expect(queryClient.getQueryCache().find({ queryKey })).toBeDefined();
      result.unmount();
    },
  );

  // 요청을 낸 화면을 떠난 뒤 도착한 403은 새 화면을 덮지 않고, 다음 화면을 위해 캐시에서 지운다.
  test('neither covers nor keeps a response that arrived after leaving the screen', async () => {
    store.set(forbiddenNoticeMountCountAtom, 1);
    window.history.replaceState({}, '', '/inspector/app-name@TOMCAT');
    const queryKey = ['/api/forbidden-test-late'];
    let reject: (error: Error) => void = () => {};

    const pending = queryClient
      .fetchQuery({
        queryKey,
        queryFn: () => new Promise((_resolve, rej) => (reject = rej)),
      })
      .catch(() => undefined);

    window.history.replaceState({}, '', '/urlStatistic/app-name@TOMCAT');
    reject(forbidden());
    await pending;

    expect(store.get(forbiddenPathAtom)).toBeUndefined();
    expect(queryClient.getQueryCache().find({ queryKey })).toBeUndefined();
  });

  // 화면 안에서 대상을 고르는 곳(Config > Agent Management)은 대상을 바꿔도 경로가 그대로다.
  // A를 고른 직후 B로 바꾸면, 늦게 도착한 A의 403이 B 화면을 덮으면 안 된다.
  describe('when the target changes without a path change', () => {
    const fetchDenied = (queryKey: string[]) => {
      let reject: (error: Error) => void = () => {};
      const pending = queryClient
        .fetchQuery({ queryKey, queryFn: () => new Promise((_resolve, rej) => (reject = rej)) })
        .catch(() => undefined);

      return { pending, deny: () => reject(forbidden()) };
    };

    beforeEach(() => {
      store.set(forbiddenNoticeMountCountAtom, 1);
      window.history.replaceState({}, '', '/config/agentManagement');
    });

    test('neither covers nor keeps a late 403 for the previous target', async () => {
      const queryKey = ['/api/forbidden-test-previous-target'];
      const previous = fetchDenied(queryKey);

      store.set(resetForbiddenTargetAtom);
      previous.deny();
      await previous.pending;

      expect(store.get(forbiddenPathAtom)).toBeUndefined();
      expect(queryClient.getQueryCache().find({ queryKey })).toBeUndefined();
    });

    test('covers the page for a 403 of the target picked after the change', async () => {
      store.set(resetForbiddenTargetAtom);
      const next = fetchDenied(['/api/forbidden-test-next-target']);

      next.deny();
      await next.pending;

      expect(store.get(forbiddenPathAtom)).toBe('/config/agentManagement');
    });

    test('drops the judgement already made for the previous target', () => {
      store.set(forbiddenPathAtom, '/config/agentManagement');

      store.set(resetForbiddenTargetAtom);

      expect(store.get(forbiddenPathAtom)).toBeUndefined();
    });
  });
});
