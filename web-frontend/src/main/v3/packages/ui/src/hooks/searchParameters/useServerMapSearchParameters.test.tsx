import React from 'react';
import { renderHook, render, act, fireEvent, screen } from '@testing-library/react';
import { MemoryRouter, useNavigate } from 'react-router-dom';
import { useServerMapSearchParameters } from './useServerMapSearchParameters';

const renderAt = (path: string) =>
  renderHook(() => useServerMapSearchParameters(), {
    wrapper: ({ children }: { children: React.ReactNode }) => (
      <MemoryRouter initialEntries={[path]}>{children}</MemoryRouter>
    ),
  });

/**
 * 렌더마다의 dateRange를 모은다.
 *
 * `renderHook`은 effect까지 끝난 값만 보여주기 때문에, 첫 렌더에만 잘못된 창이 잡히는 문제는
 * 그것으로 잡히지 않는다. 조회는 렌더 시점의 값으로 나가므로 렌더마다 봐야 한다.
 */
const collectRendersAt = (path: string) => {
  const rendered: { from: Date; to: Date }[] = [];

  const Probe = () => {
    const { dateRange } = useServerMapSearchParameters();
    rendered.push({ from: dateRange.from, to: dateRange.to });
    return null;
  };

  render(
    <MemoryRouter initialEntries={[path]}>
      <Probe />
    </MemoryRouter>,
  );

  return rendered;
};

const minutesBetween = ({ from, to }: { from: Date; to: Date }) =>
  (to.getTime() - from.getTime()) / 60_000;

describe('useServerMapSearchParameters', () => {
  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    // 가짜 타이머를 걷기 전에 스파이를 먼저 되돌린다. 순서가 뒤집히면 스파이가 덮어쓴
    // clearInterval이 전역에서 사라져 뒤의 테스트가 깨진다.
    jest.restoreAllMocks();
    jest.useRealTimers();
  });

  describe('realtime paths', () => {
    const REALTIME_PATHS = [
      '/serverMap/realtime/App@TOMCAT',
      '/serviceMap/realtime/DEFAULT/App@TOMCAT',
      '/serviceMap/realtime/blogService',
      '/scatterFullScreenMode/realtime/App@TOMCAT',
      '/heatmapFullScreenMode/realtime/App@TOMCAT',
    ];

    test.each(REALTIME_PATHS)('marks %s as realtime', (path) => {
      expect(renderAt(path).result.current.dateRange.isRealtime).toBe(true);
    });

    // 실시간 경로에는 from/to가 없다. URL이 아니라 화면이 창을 만든다.
    test.each(REALTIME_PATHS)('starts with a 5 minute window on %s', (path) => {
      expect(minutesBetween(renderAt(path).result.current.dateRange)).toBe(5);
    });

    // 조회는 렌더 시점의 dateRange로 나간다. 첫 렌더가 기본 기간(20분)으로 잡히면 그 폭으로 한 번
    // 조회가 나가고, 스캐터는 그 값으로 x축을 잡는다.
    test.each(REALTIME_PATHS)('never renders a wider window on %s', (path) => {
      const widths = collectRendersAt(path).map(minutesBetween);

      expect(widths.length).toBeGreaterThan(0);
      expect(widths.filter((width) => width !== 5)).toEqual([]);
    });

    // 폭이 5분이어도 from/to가 렌더마다 몇 ms씩 달라지면 조회 조건이 바뀌어 같은 화면을 두 번
    // 조회한다.
    test.each(REALTIME_PATHS)('settles on a single window on %s', (path) => {
      const windows = collectRendersAt(path).map(
        ({ from, to }) => `${from.getTime()}~${to.getTime()}`,
      );

      expect(new Set(windows).size).toBe(1);
    });

    test('slides the window by 2 seconds per tick, keeping its width', () => {
      const { result } = renderAt('/serviceMap/realtime/DEFAULT/App@TOMCAT');
      const before = result.current.dateRange;

      act(() => {
        jest.advanceTimersByTime(4000);
      });

      const after = result.current.dateRange;
      expect(after.from.getTime() - before.from.getTime()).toBe(4000);
      expect(after.to.getTime() - before.to.getTime()).toBe(4000);
      expect(minutesBetween(after)).toBe(5);
    });

    // 정리하지 않으면 화면을 떠난 뒤에도 타이머가 남아 사라진 컴포넌트의 state를 갱신한다.
    test('clears its timer on unmount', () => {
      const clearIntervalSpy = jest.spyOn(global, 'clearInterval');
      const { unmount } = renderAt('/serverMap/realtime/App@TOMCAT');

      expect(jest.getTimerCount()).toBe(1);
      unmount();

      expect(clearIntervalSpy).toHaveBeenCalled();
      expect(jest.getTimerCount()).toBe(0);
    });

    // 실시간이 도중에 켜지는 경우. 마운트한 채로 경로만 바뀌면 초기값은 URL에서 읽은 기간이라,
    // 그대로 두면 실시간 화면이 옛날 기간을 보여준다.
    test('rebuilds the window when realtime turns on without a remount', () => {
      const Probe = () => {
        const navigate = useNavigate();
        const { dateRange } = useServerMapSearchParameters();

        return (
          <button onClick={() => navigate('/serverMap/realtime/App@TOMCAT')}>
            {minutesBetween(dateRange)}
          </button>
        );
      };

      render(
        <MemoryRouter
          initialEntries={['/serverMap/App@TOMCAT?from=2023-11-10-14-30-00&to=2023-11-10-15-00-00']}
        >
          <Probe />
        </MemoryRouter>,
      );
      expect(screen.getByRole('button').textContent).toBe('30');

      act(() => {
        fireEvent.click(screen.getByRole('button'));
      });

      expect(screen.getByRole('button').textContent).toBe('5');
    });
  });

  describe('non-realtime paths', () => {
    test.each([
      ['/serverMap/App@TOMCAT'],
      ['/serviceMap/DEFAULT/App@TOMCAT'],
      // 'realtime'이 다른 페이지의 세그먼트로 들어와도 실시간이 아니다.
      ['/inspector/realtime@TOMCAT'],
    ])('does not mark %s as realtime', (path) => {
      expect(renderAt(path).result.current.dateRange.isRealtime).toBe(false);
    });

    test('starts no timer', () => {
      renderAt('/serverMap/App@TOMCAT');

      expect(jest.getTimerCount()).toBe(0);
    });

    test('reads the range from the query string', () => {
      const { result } = renderAt(
        '/serverMap/App@TOMCAT?from=2023-11-10-14-30-00&to=2023-11-10-15-00-00',
      );

      expect(minutesBetween(result.current.dateRange)).toBe(30);
    });
  });

  test('parses the application and query options from the path', () => {
    const { result } = renderAt('/serverMap/App@TOMCAT?inbound=2&outbound=3&wasOnly=true');

    expect(result.current.application).toEqual({
      applicationName: 'App',
      serviceType: 'TOMCAT',
    });
    expect(result.current.queryOption).toEqual({
      inbound: 2,
      outbound: 3,
      wasOnly: true,
      bidirectional: false,
    });
  });
});
