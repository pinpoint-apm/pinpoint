import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { forbiddenPathAtom, markCurrentPathForbiddenAtom } from '@pinpoint-fe/ui/src/atoms';
import { enterForbiddenNotice } from '../api/reactQueryHelper';
import { useIsForbiddenPath } from './useIsForbiddenPath';

const mockLeaveNotice = jest.fn();
jest.mock('../api/reactQueryHelper', () => ({
  enterForbiddenNotice: jest.fn(() => mockLeaveNotice),
}));

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

  // 안내가 떠 있는 동안 아무도 보지 않는 403 조회를 치운다. 그래야 같은 조회를 쓰는 다음 화면이
  // 캐시의 에러를 그대로 받지 않고 다시 묻는다. 안내가 사라지면 그 표시도 거둔다.
  test('enters the notice while the page shows it, and leaves it on unmount', () => {
    jest.mocked(enterForbiddenNotice).mockClear();
    mockLeaveNotice.mockClear();

    renderAt('/inspector/app-name@TOMCAT');
    expect(enterForbiddenNotice).not.toHaveBeenCalled();

    act(() => store.set(markCurrentPathForbiddenAtom));
    window.history.replaceState({}, '', '/inspector/app-name@TOMCAT');
    const { unmount } = renderHook(() => useIsForbiddenPath());
    expect(enterForbiddenNotice).toHaveBeenCalled();

    unmount();
    expect(mockLeaveNotice).toHaveBeenCalled();
  });
});
