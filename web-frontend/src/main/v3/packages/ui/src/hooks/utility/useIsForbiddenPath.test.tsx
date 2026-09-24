import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import {
  forbiddenNoticeMountCountAtom,
  forbiddenPathAtom,
  markCurrentPathForbiddenAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { useIsForbiddenPath } from './useIsForbiddenPath';

jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  // 경로 변경을 구독하는 용도로만 쓰이므로 판정에는 관여하지 않는다(판정은 getCurrentRouterPath).
  useLocation: () => ({ pathname: globalThis.location.pathname }),
}));

const store = getDefaultStore();

const renderAt = (pathname: string) => {
  window.history.replaceState({}, '', pathname);
  return renderHook(() => useIsForbiddenPath()).result.current;
};

describe('useIsForbiddenPath', () => {
  beforeEach(() => {
    act(() => store.set(forbiddenPathAtom, undefined));
    window.history.replaceState({}, '', '/inspector/app-name@TOMCAT');
  });

  test('is false while nothing has been denied', () => {
    expect(renderAt('/inspector/app-name@TOMCAT')).toBe(false);
  });

  test('is true on the path that was denied', () => {
    act(() => store.set(markCurrentPathForbiddenAtom));

    expect(renderAt('/inspector/app-name@TOMCAT')).toBe(true);
  });

  // 아톰은 화면이 바뀌어도 지워지지 않는다. 경로를 비교하지 않으면 다음 화면까지 권한 없음으로
  // 그려진다 — effect로 지우면 그 한 렌더를 막지 못한다.
  test('is false on another path even though the judgement is still stored', () => {
    act(() => store.set(markCurrentPathForbiddenAtom));

    expect(renderAt('/urlStatistic/app-name@TOMCAT')).toBe(false);
    expect(store.get(forbiddenPathAtom)).toBe('/inspector/app-name@TOMCAT');
  });

  // 이 훅은 도장만 비교하므로 값이 남아 있으면 다시 true다. 실제로는 화면을 떠나는 순간
  // `useClearForbiddenOnPathChange`가 도장을 버려서, 같은 URL로 다시 들어오면 조회부터 한다.
  test('compares the stamp only, and does not itself expire the judgement', () => {
    act(() => store.set(markCurrentPathForbiddenAtom));
    renderAt('/urlStatistic/app-name@TOMCAT');

    expect(renderAt('/inspector/app-name@TOMCAT')).toBe(true);
  });

  test('is false once the judgement is cleared', () => {
    act(() => store.set(markCurrentPathForbiddenAtom));
    act(() => store.set(forbiddenPathAtom, undefined));

    expect(renderAt('/inspector/app-name@TOMCAT')).toBe(false);
  });

  // 이 훅을 부르는 화면이 안내를 그린다. 전역 에러 핸들러는 이 값으로 화면을 덮을지 정한다.
  test('counts itself as a mounted notice while mounted', () => {
    act(() => store.set(forbiddenNoticeMountCountAtom, 0));

    const first = renderHook(() => useIsForbiddenPath());
    const second = renderHook(() => useIsForbiddenPath());
    expect(store.get(forbiddenNoticeMountCountAtom)).toBe(2);

    first.unmount();
    expect(store.get(forbiddenNoticeMountCountAtom)).toBe(1);

    second.unmount();
    expect(store.get(forbiddenNoticeMountCountAtom)).toBe(0);
  });
});
