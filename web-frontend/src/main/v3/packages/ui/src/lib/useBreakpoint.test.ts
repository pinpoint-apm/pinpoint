import { renderHook } from '@testing-library/react';
import { useBreakpoint } from './useBreakpoint';
import { useMediaQuery } from 'react-responsive';
import { screens } from '@pinpoint-fe/ui/src/constants';

// Mock react-responsive
jest.mock('react-responsive', () => ({
  useMediaQuery: jest.fn(),
}));

describe('Test useBreakpoint hook', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('Return breakpoint value and boolean flags when below breakpoint', () => {
    (useMediaQuery as jest.Mock).mockReturnValue(true); // isBelow = true

    const { result } = renderHook(() => useBreakpoint('sm'));

    expect(result.current.sm).toBe(Number(String(screens.sm).replace(/[^0-9]/g, '')));
    expect(result.current.isBelowSm).toBe(true);
    expect(result.current.isAboveSm).toBe(false);
  });

  test('Return breakpoint value and boolean flags when above breakpoint', () => {
    (useMediaQuery as jest.Mock).mockReturnValue(false); // isBelow = false

    const { result } = renderHook(() => useBreakpoint('md'));

    expect(result.current.md).toBe(Number(String(screens.md).replace(/[^0-9]/g, '')));
    expect(result.current.isBelowMd).toBe(false);
    expect(result.current.isAboveMd).toBe(true);
  });

  test('Handle different breakpoint keys', () => {
    (useMediaQuery as jest.Mock).mockReturnValue(true);

    const { result } = renderHook(() => useBreakpoint('lg'));

    expect(result.current.lg).toBe(Number(String(screens.lg).replace(/[^0-9]/g, '')));
    expect(result.current.isBelowLg).toBe(true);
    expect(result.current.isAboveLg).toBe(false);
  });

  test('Call useMediaQuery with correct query string', () => {
    (useMediaQuery as jest.Mock).mockReturnValue(true);

    renderHook(() => useBreakpoint('xl'));

    expect(useMediaQuery).toHaveBeenCalledWith({
      query: `(max-width: ${screens.xl})`,
    });
  });

  test('Handle breakpoint value extraction from string with units', () => {
    (useMediaQuery as jest.Mock).mockReturnValue(true);

    const { result } = renderHook(() => useBreakpoint('sm'));

    // Extract numeric value from breakpoint (e.g., "640px" -> 640)
    const numericValue = Number(String(screens.sm).replace(/[^0-9]/g, ''));
    expect(result.current.sm).toBe(numericValue);
    expect(typeof result.current.sm).toBe('number');
  });
});
