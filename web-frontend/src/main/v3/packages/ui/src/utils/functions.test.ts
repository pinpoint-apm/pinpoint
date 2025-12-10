import { debounce } from './functions';

describe('Test functions utils', () => {
  describe('Test "debounce"', () => {
    beforeEach(() => {
      jest.useFakeTimers();
    });

    afterEach(() => {
      jest.useRealTimers();
    });

    test('Execute function after default delay (250ms)', () => {
      const mockFn = jest.fn();
      const debouncedFn = debounce(mockFn);

      debouncedFn();
      expect(mockFn).not.toHaveBeenCalled();

      jest.advanceTimersByTime(250);
      expect(mockFn).toHaveBeenCalledTimes(1);
    });

    test('Execute function after custom delay', () => {
      const mockFn = jest.fn();
      const debouncedFn = debounce(mockFn, 500);

      debouncedFn();
      expect(mockFn).not.toHaveBeenCalled();

      jest.advanceTimersByTime(250);
      expect(mockFn).not.toHaveBeenCalled();

      jest.advanceTimersByTime(250);
      expect(mockFn).toHaveBeenCalledTimes(1);
    });

    test('Cancel previous call when called multiple times', () => {
      const mockFn = jest.fn();
      const debouncedFn = debounce(mockFn, 250);

      debouncedFn();
      jest.advanceTimersByTime(100);

      debouncedFn();
      jest.advanceTimersByTime(100);

      debouncedFn();
      jest.advanceTimersByTime(250);

      expect(mockFn).toHaveBeenCalledTimes(1);
    });

    test('Handle rapid successive calls', () => {
      const mockFn = jest.fn();
      const debouncedFn = debounce(mockFn, 100);

      debouncedFn();
      debouncedFn();
      debouncedFn();
      debouncedFn();

      jest.advanceTimersByTime(100);
      expect(mockFn).toHaveBeenCalledTimes(1);
    });
  });
});
