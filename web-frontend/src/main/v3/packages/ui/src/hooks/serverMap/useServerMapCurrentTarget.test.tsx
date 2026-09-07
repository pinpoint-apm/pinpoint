import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import {
  CurrentTarget,
  serverMapCurrentTargetAtom,
  serverMapDataAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { GetServerMap } from '@pinpoint-fe/ui/src/constants';
import {
  useServerMapCurrentTarget,
  useServerMapCurrentTargetData,
} from './useServerMapCurrentTarget';

// 이 훅은 `useLocation()`을 다시 렌더시키는 구독으로만 쓰고, 경로 값 자체는
// `window.location`에서 읽는다(아톰이 도장 찍는 곳과 같은 출처).
jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useLocation: () => ({ pathname: globalThis.location.pathname }),
}));

const store = getDefaultStore();

const node = (applicationName: string) =>
  ({
    key: `${applicationName}^TOMCAT`,
    nodeKey: `${applicationName}^TOMCAT`,
    applicationName,
    serviceType: 'TOMCAT',
  }) as GetServerMap.NodeData;

const goTo = (pathname: string) => {
  window.history.pushState({}, '', pathname);
};

const setTarget = (target?: CurrentTarget) => {
  act(() => {
    store.set(serverMapCurrentTargetAtom, target);
  });
};

describe('useServerMapCurrentTarget', () => {
  beforeEach(() => {
    goTo('/serverMap/app-a@TOMCAT');
    act(() => {
      store.set(serverMapDataAtom, {
        applicationMapData: { nodeDataArray: [node('app-a'), node('app-b')], linkDataArray: [] },
      } as unknown as GetServerMap.Response);
    });
    setTarget(undefined);
  });

  test('gives the selection made on the path being viewed', () => {
    setTarget({ id: 'app-a^TOMCAT', type: 'node', applicationName: 'app-a' });

    expect(renderHook(() => useServerMapCurrentTarget()).result.current?.id).toBe('app-a^TOMCAT');
    expect(
      (renderHook(() => useServerMapCurrentTargetData()).result.current as GetServerMap.NodeData)
        ?.applicationName,
    ).toBe('app-a');
  });

  // 페이지가 경로 변경 effect에서 아톰을 비우기 전 한 렌더 동안 이전 선택이 남아 있다.
  // 그 짝(새 application + 이전 경로의 노드)으로 조회가 한 번 더 나가던 원인. (이슈 #10587)
  test('drops the selection as soon as the path changes, before the atom is cleared', () => {
    setTarget({ id: 'app-a^TOMCAT', type: 'node', applicationName: 'app-a' });
    goTo('/serverMap/app-b@TOMCAT');

    expect(renderHook(() => useServerMapCurrentTarget()).result.current).toBeUndefined();
    expect(renderHook(() => useServerMapCurrentTargetData()).result.current).toBeUndefined();
  });

  // 기간·조회 옵션 변경은 query string만 바꾼다. 고른 노드는 그대로여야 한다.
  test('keeps the selection when only the query string changes', () => {
    setTarget({ id: 'app-a^TOMCAT', type: 'node', applicationName: 'app-a' });
    goTo('/serverMap/app-a@TOMCAT?from=2026-09-07-11-00-00&to=2026-09-07-11-20-00');

    expect(renderHook(() => useServerMapCurrentTarget()).result.current?.id).toBe('app-a^TOMCAT');
  });

  // 새 경로에서 다시 고르면 그 선택은 이 경로의 것이다.
  test('takes the selection again once it is made on the new path', () => {
    setTarget({ id: 'app-a^TOMCAT', type: 'node', applicationName: 'app-a' });
    goTo('/serverMap/app-b@TOMCAT');
    setTarget({ id: 'app-b^TOMCAT', type: 'node', applicationName: 'app-b' });

    expect(renderHook(() => useServerMapCurrentTarget()).result.current?.id).toBe('app-b^TOMCAT');
  });

  test('is undefined when nothing has ever been picked', () => {
    expect(renderHook(() => useServerMapCurrentTarget()).result.current).toBeUndefined();
  });
});
