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
import { forbiddenPathAtom } from '../../atoms/permission';
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

const renderSuspenseQuery = ({
  withNotice,
  suspenseOutsidePage = false,
}: {
  withNotice: boolean;
  /**
   * 로딩 경계가 페이지 **바깥**에 있는 구조(라우트 단위 Suspense). 페이지 안의 조회가 suspend하면
   * 페이지 전체가 commit되지 않은 채 첫 응답을 받는다.
   */
  suspenseOutsidePage?: boolean;
}) => {
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
  const OutsideNoticePage = () =>
    useIsForbiddenPath() ? (
      <div>forbidden</div>
    ) : (
      <TestErrorBoundary>
        <Body />
      </TestErrorBoundary>
    );

  const result = render(
    <QueryClientProvider client={queryClient}>
      {suspenseOutsidePage ? (
        <React.Suspense fallback={<div>loading</div>}>
          <OutsideNoticePage />
        </React.Suspense>
      ) : withNotice ? (
        <NoticePage />
      ) : (
        content
      )}
    </QueryClientProvider>,
  );

  return { result, queryKey, getCalls: () => calls };
};

describe('page level 403 with a real suspense query', () => {
  beforeEach(() => {
    store.set(forbiddenPathAtom, undefined);
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

  // 실제 화면 구조다(Inspector: 라우트 단위 Suspense 안에서 페이지째 suspend). 첫 403이 올 때
  // 페이지는 아직 commit 전이라, "안내를 그리는 화면이 떠 있는가"로 판정하면 덮지 못했다.
  test('covers a page that is still suspended as a whole when the first 403 arrives', async () => {
    window.history.replaceState({}, '', '/inspector/app-name@TOMCAT');

    const { result, queryKey, getCalls } = renderSuspenseQuery({
      withNotice: true,
      suspenseOutsidePage: true,
    });
    await flush();

    expect(getCalls()).toBe(1);
    expect(result.container.textContent).toBe('forbidden');
    // 안내로 바뀐 뒤에는 아무도 보지 않으므로 지운다. 같은 조회를 쓰는 다음 화면이 다시 묻는다.
    expect(queryClient.getQueryCache().find({ queryKey })).toBeUndefined();
    result.unmount();
  });

  // 안내를 그리지 않는 화면은 도장을 읽지 않으므로 그대로 두고, 에러를 캐시에 남겨 ErrorBoundary가
  // 그리게 한다. 지우면 다시 묻는다.
  test.each(['/serverMap/app-name@TOMCAT', '/config/alarm'])(
    'keeps the error for the first request on %s, which does not render the notice',
    async (pathname) => {
      window.history.replaceState({}, '', pathname);

      const { result, queryKey, getCalls } = renderSuspenseQuery({ withNotice: false });
      await flush();

      expect(getCalls()).toBe(1);
      expect(result.container.textContent).toBe('error');
      expect(queryClient.getQueryCache().find({ queryKey })).toBeDefined();
      result.unmount();
    },
  );

  // 요청을 낸 화면을 떠난 뒤 도착한 403은 새 화면을 덮지 않고, 다음 화면을 위해 캐시에서 지운다.
  test('neither covers nor keeps a response that arrived after leaving the screen', async () => {
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
});
