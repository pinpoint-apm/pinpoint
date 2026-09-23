import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { forbiddenPathAtom, markCurrentPathForbiddenAtom } from '@pinpoint-fe/ui/src/atoms';
import { useClearForbiddenOnPathChange } from './useClearForbiddenOnPathChange';

jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useLocation: () => ({ pathname: globalThis.location.pathname }),
}));

const store = getDefaultStore();

const renderAt = (pathname: string) => {
  window.history.replaceState({}, '', pathname);
  return renderHook(() => useClearForbiddenOnPathChange());
};

describe('useClearForbiddenOnPathChange', () => {
  beforeEach(() => {
    act(() => store.set(forbiddenPathAtom, undefined));
    window.history.replaceState({}, '', '/inspector/app-name@TOMCAT');
  });

  test('keeps the judgement while the denied path is still open', () => {
    act(() => store.set(markCurrentPathForbiddenAtom));

    renderAt('/inspector/app-name@TOMCAT');

    expect(store.get(forbiddenPathAtom)).toBe('/inspector/app-name@TOMCAT');
  });

  // 판정이 선 화면은 본문을 언마운트해 조회가 멈춘다. 도장을 남겨 두면 같은 URL로 다시
  // 들어왔을 때 재조회 없이 곧바로 막혀, 그 사이 권한이 생겨도 새로고침 전까지 알 수 없다.
  test('drops the judgement once another path is open, so re-entering asks again', () => {
    act(() => store.set(markCurrentPathForbiddenAtom));

    renderAt('/serverMap/app-name@TOMCAT');

    expect(store.get(forbiddenPathAtom)).toBeUndefined();
  });

  test('does nothing when no path has been denied', () => {
    renderAt('/serverMap/app-name@TOMCAT');

    expect(store.get(forbiddenPathAtom)).toBeUndefined();
  });

  // 기간·조회 옵션만 바꾸는 이동은 경로가 그대로다. 권한은 그것으로 달라지지 않으므로
  // 판정도 그대로 둔다(본문이 언마운트된 채라 재조회도 일어나지 않는다).
  test('keeps the judgement when only the query string changes', () => {
    act(() => store.set(markCurrentPathForbiddenAtom));

    window.history.replaceState({}, '', '/inspector/app-name@TOMCAT?from=1&to=2');
    renderHook(() => useClearForbiddenOnPathChange());

    expect(store.get(forbiddenPathAtom)).toBe('/inspector/app-name@TOMCAT');
  });
});
