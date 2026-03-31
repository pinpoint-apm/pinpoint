import { renderHook, act } from '@testing-library/react';
import { useTabFocus } from './useTabFocus';

describe('useTabFocus', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    Object.defineProperty(document, 'hidden', { get: () => false, configurable: true });
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  test('initializes as focused (true) when document is visible', () => {
    const { result } = renderHook(() => useTabFocus());
    expect(result.current).toBe(true);
  });

  test('remains true before delay elapses when tab becomes hidden', () => {
    const { result } = renderHook(() => useTabFocus(1000));

    act(() => {
      Object.defineProperty(document, 'hidden', { get: () => true, configurable: true });
      document.dispatchEvent(new Event('visibilitychange'));
    });

    expect(result.current).toBe(true);
  });

  test('becomes false after delay when tab is hidden', () => {
    const { result } = renderHook(() => useTabFocus(1000));

    act(() => {
      Object.defineProperty(document, 'hidden', { get: () => true, configurable: true });
      document.dispatchEvent(new Event('visibilitychange'));
      jest.advanceTimersByTime(1001);
    });

    expect(result.current).toBe(false);
  });

  test('becomes true immediately when tab regains focus', () => {
    const { result } = renderHook(() => useTabFocus(1000));

    act(() => {
      Object.defineProperty(document, 'hidden', { get: () => true, configurable: true });
      document.dispatchEvent(new Event('visibilitychange'));
      jest.advanceTimersByTime(1001);
    });

    expect(result.current).toBe(false);

    act(() => {
      Object.defineProperty(document, 'hidden', { get: () => false, configurable: true });
      document.dispatchEvent(new Event('visibilitychange'));
    });

    expect(result.current).toBe(true);
  });

  test('cancels the pending timer when tab regains focus before delay', () => {
    const { result } = renderHook(() => useTabFocus(2000));

    act(() => {
      Object.defineProperty(document, 'hidden', { get: () => true, configurable: true });
      document.dispatchEvent(new Event('visibilitychange'));
    });

    // Tab regains focus before the 2s delay
    act(() => {
      Object.defineProperty(document, 'hidden', { get: () => false, configurable: true });
      document.dispatchEvent(new Event('visibilitychange'));
      jest.advanceTimersByTime(2001);
    });

    // Should still be true because the timer was cancelled
    expect(result.current).toBe(true);
  });

  test('removes visibilitychange listener on unmount', () => {
    const removeSpy = jest.spyOn(document, 'removeEventListener');
    const { unmount } = renderHook(() => useTabFocus());

    unmount();

    expect(removeSpy).toHaveBeenCalledWith('visibilitychange', expect.any(Function));
    removeSpy.mockRestore();
  });
});
