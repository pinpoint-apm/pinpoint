import { StrictMode } from 'react';
import { renderHook } from '@testing-library/react';
import { useUpdateEffect } from './useUpdateEffect';

// packages/ui 의 같은 이름 훅과 동작이 어긋나면 안 된다 (의존 방향 때문에 복사해 둔 것이라
// 한쪽만 고쳐지는 일이 생길 수 있다). 그쪽 테스트와 같은 케이스를 둔다.
describe('useUpdateEffect', () => {
  test('does not run on mount', () => {
    const effect = jest.fn();
    renderHook(({ dep }) => useUpdateEffect(effect, [dep]), { initialProps: { dep: 1 } });

    expect(effect).not.toHaveBeenCalled();
  });

  test('runs when a dependency changes', () => {
    const effect = jest.fn();
    const { rerender } = renderHook(({ dep }) => useUpdateEffect(effect, [dep]), {
      initialProps: { dep: 1 },
    });

    rerender({ dep: 2 });
    expect(effect).toHaveBeenCalledTimes(1);
  });

  test('does not run when the dependencies are unchanged', () => {
    const effect = jest.fn();
    const { rerender } = renderHook(({ dep }) => useUpdateEffect(effect, [dep]), {
      initialProps: { dep: 1 },
    });

    rerender({ dep: 1 });
    expect(effect).not.toHaveBeenCalled();
  });

  test('runs the cleanup returned by the effect before the next run', () => {
    const cleanup = jest.fn();
    const effect = jest.fn(() => cleanup);
    const { rerender, unmount } = renderHook(({ dep }) => useUpdateEffect(effect, [dep]), {
      initialProps: { dep: 1 },
    });

    rerender({ dep: 2 });
    expect(cleanup).not.toHaveBeenCalled();

    rerender({ dep: 3 });
    expect(cleanup).toHaveBeenCalledTimes(1);

    unmount();
    expect(cleanup).toHaveBeenCalledTimes(2);
  });

  // 이 패키지의 개발용 엔트리가 StrictMode 로 감싸고 있다.
  test('still skips the mount run under StrictMode', () => {
    const effect = jest.fn();
    const { rerender } = renderHook(({ dep }) => useUpdateEffect(effect, [dep]), {
      initialProps: { dep: 1 },
      wrapper: StrictMode,
    });

    expect(effect).not.toHaveBeenCalled();

    rerender({ dep: 2 });
    expect(effect).toHaveBeenCalledTimes(1);
  });
});
