import { renderHook } from '@testing-library/react';
import { useBreakpoint } from './useBreakpoint';
import { useMediaQuery } from 'react-responsive';
import { screens } from '@pinpoint-fe/ui/src/constants';

// Mock react-responsive
jest.mock('react-responsive', () => ({
  useMediaQuery: jest.fn(),
}));

// 실제 픽셀 값을 고정한다. Tailwind 의 값 표기에서 숫자만 떼어내는 식으로 기대값을 쓰면 표기가
// 바뀔 때(4 에서 `640px` → `40rem`) 테스트와 구현이 같이 틀려서 아무것도 잡지 못한다.
const BREAKPOINT_PIXELS = { sm: 640, md: 768, lg: 1024, xl: 1280 } as const;

describe('Test useBreakpoint hook', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('Return breakpoint value and boolean flags when below breakpoint', () => {
    (useMediaQuery as jest.Mock).mockReturnValue(true); // isBelow = true

    const { result } = renderHook(() => useBreakpoint('sm'));

    expect(result.current.sm).toBe(BREAKPOINT_PIXELS.sm);
    expect(result.current.isBelowSm).toBe(true);
    expect(result.current.isAboveSm).toBe(false);
  });

  test('Return breakpoint value and boolean flags when above breakpoint', () => {
    (useMediaQuery as jest.Mock).mockReturnValue(false); // isBelow = false

    const { result } = renderHook(() => useBreakpoint('md'));

    expect(result.current.md).toBe(BREAKPOINT_PIXELS.md);
    expect(result.current.isBelowMd).toBe(false);
    expect(result.current.isAboveMd).toBe(true);
  });

  test('Handle different breakpoint keys', () => {
    (useMediaQuery as jest.Mock).mockReturnValue(true);

    const { result } = renderHook(() => useBreakpoint('lg'));

    expect(result.current.lg).toBe(BREAKPOINT_PIXELS.lg);
    expect(result.current.isBelowLg).toBe(true);
    expect(result.current.isAboveLg).toBe(false);
  });

  test('Call useMediaQuery with correct query string', () => {
    (useMediaQuery as jest.Mock).mockReturnValue(true);

    renderHook(() => useBreakpoint('xl'));

    // 미디어 쿼리에는 Tailwind 의 원문 값을 그대로 싣는다(rem 이든 px 이든 CSS 가 해석한다).
    expect(useMediaQuery).toHaveBeenCalledWith({
      query: `(max-width: ${screens.xl})`,
    });
  });

  test('Return the breakpoint in pixels even though Tailwind defines it in rem', () => {
    (useMediaQuery as jest.Mock).mockReturnValue(true);

    const { result } = renderHook(() => useBreakpoint('sm'));

    expect(String(screens.sm)).toBe('40rem');
    expect(result.current.sm).toBe(640);
    expect(typeof result.current.sm).toBe('number');
  });
});
