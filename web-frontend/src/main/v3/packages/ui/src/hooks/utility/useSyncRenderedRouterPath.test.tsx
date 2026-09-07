import { renderHook } from '@testing-library/react';

const goTo = (pathname: string) => window.history.pushState({}, '', pathname);

/**
 * 모듈 레벨 값(`renderedRouterPath`)을 다루므로 테스트마다 모듈을 새로 읽는다.
 * "아직 한 번도 동기화되지 않은" 상태를 만들 방법이 그것뿐이다.
 */
const load = () => {
  jest.resetModules();
  const route = require('@pinpoint-fe/ui/src/utils/helper/route');
  const sync = require('./useSyncRenderedRouterPath');
  return { getCurrentRouterPath: route.getCurrentRouterPath, ...sync };
};

jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useLocation: () => ({ pathname: globalThis.location.pathname }),
}));

describe('useSyncRenderedRouterPath', () => {
  // 부트스트랩과 라우트 로더는 렌더 전에 돌아간다. 그때는 라우터도 같은 주소를 보고 있다.
  test('falls back to window.location before anything has rendered', () => {
    goTo('/serviceMap/aService');
    const { getCurrentRouterPath } = load();

    expect(getCurrentRouterPath()).toBe('/serviceMap/aService');
  });

  test('reports the path the router rendered', () => {
    goTo('/serviceMap/aService');
    const { getCurrentRouterPath, useSyncRenderedRouterPath } = load();
    renderHook(() => useSyncRenderedRouterPath());

    expect(getCurrentRouterPath()).toBe('/serviceMap/aService');
  });

  // popstate에서는 브라우저가 주소를 먼저 바꾸고 라우터의 location은 다음 렌더에 반영된다.
  // 그 사이 service만 새 경로의 것이 되면 (이전 파라미터, 새 service) 짝의 캐시 키가 만들어져
  // 조회가 한 번 더 나간다. 그래서 렌더될 때까지 이전 경로를 유지해야 한다. (이슈 #10587)
  test('keeps the previous path until the router renders the new one', () => {
    goTo('/serviceMap/aService');
    const { getCurrentRouterPath, useSyncRenderedRouterPath } = load();
    const { rerender } = renderHook(() => useSyncRenderedRouterPath());

    // 브라우저 주소만 먼저 바뀐 상태 (라우터는 아직 렌더하지 않았다)
    goTo('/serviceMap/bService');
    expect(getCurrentRouterPath()).toBe('/serviceMap/aService');

    // 라우터가 새 경로로 렌더하면 그때 따라간다
    rerender();
    expect(getCurrentRouterPath()).toBe('/serviceMap/bService');
  });
});
