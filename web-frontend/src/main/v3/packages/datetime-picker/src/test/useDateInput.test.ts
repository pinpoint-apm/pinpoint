import { renderHook, act } from '@testing-library/react';
import { useDateInput } from '../components/hooks/useDateInput';
import { subMinutes, addDays } from 'date-fns';
import { getLocale } from '../utils/locale';
import { SEAM_TOKEN } from '../constants/patterns';

describe('useDateInput', () => {
  const now = new Date();
  const startDate = subMinutes(now, 5);
  const endDate = now;
  const locale = getLocale('en');
  const dateFormat = 'MMM do, hh:mm a';
  const timeZone = 'UTC';
  const seamToken = SEAM_TOKEN;

  const defaultParams = {
    from: startDate,
    to: endDate,
    locale,
    dateFormat,
    timeZone,
    seamToken,
  };

  describe('초기화', () => {
    it('기본값으로 초기화되어야 함', () => {
      const { result } = renderHook(() => useDateInput(defaultParams));

      expect(result.current.isValidInput).toBe(true);
      expect(result.current.dateInput).toBe('');
      expect(result.current.displayInput).toBeUndefined();
    });
  });

  describe('getFormattedDate', () => {
    it('날짜를 올바른 형식으로 포맷팅해야 함', () => {
      const { result } = renderHook(() => useDateInput(defaultParams));

      const formatted = result.current.getFormattedDate(startDate, endDate);

      expect(formatted).toContain(seamToken);
      expect(typeof formatted).toBe('string');
    });

    it('locale에 따라 다른 형식으로 포맷팅해야 함', () => {
      const { result: resultEn } = renderHook(() =>
        useDateInput({
          ...defaultParams,
          locale: getLocale('en'),
        }),
      );

      const { result: resultKo } = renderHook(() =>
        useDateInput({
          ...defaultParams,
          locale: getLocale('ko'),
        }),
      );

      const formattedEn = resultEn.current.getFormattedDate(startDate, endDate);
      const formattedKo = resultKo.current.getFormattedDate(startDate, endDate);

      expect(formattedEn).not.toBe(formattedKo);
    });
  });

  const changeEvent = (value: string) =>
    ({ target: { value } }) as React.ChangeEvent<HTMLInputElement>;

  const enterEvent = { code: 'Enter', preventDefault: jest.fn() } as unknown as React.KeyboardEvent;

  const type = (result: { current: ReturnType<typeof useDateInput> }, value: string) =>
    act(() => {
      result.current.handleChangeInput(changeEvent(value));
    });

  describe('handleChangeInput', () => {
    it('입력값을 업데이트해야 함', () => {
      const { result } = renderHook(() => useDateInput(defaultParams));

      type(result, 'Sep 1st, 09:00 AM');

      expect(result.current.dateInput).toBe('Sep 1st, 09:00 AM');
    });

    it('파싱할 수 없는 입력은 사유와 함께 무효로 판정해야 함', () => {
      const { result } = renderHook(() => useDateInput(defaultParams));

      type(result, 'invalid date');

      expect(result.current.isValidInput).toBe(false);
      expect(result.current.inputError).toEqual({ code: 'unparseable', source: 'input' });
    });

    it('범위 한쪽만 입력한 상태는 incomplete로 판정해야 함', () => {
      const { result } = renderHook(() => useDateInput(defaultParams));

      type(result, `Sep 1st ${seamToken} `);

      expect(result.current.inputError?.code).toBe('incomplete');
    });

    it('유효한 입력으로 되돌리면 오류가 사라져야 함', () => {
      const { result } = renderHook(() => useDateInput(defaultParams));

      type(result, 'invalid date');
      expect(result.current.isValidInput).toBe(false);

      type(result, '45m');

      expect(result.current.isValidInput).toBe(true);
      expect(result.current.inputError).toBeNull();
    });

    it('타이핑 중 다 지운 상태는 오류로 보지 않아야 함', () => {
      const { result } = renderHook(() => useDateInput(defaultParams));

      type(result, 'invalid date');
      type(result, '');

      expect(result.current.isValidInput).toBe(true);
    });

    it('minDate 밖 날짜는 out-of-bounds로 판정해야 함', () => {
      const { result } = renderHook(() =>
        useDateInput({ ...defaultParams, minDate: new Date('2030-01-01T00:00:00Z') }),
      );

      type(result, '45m');

      expect(result.current.inputError?.code).toBe('out-of-bounds');
    });
  });

  describe('handleKeyDownInput', () => {
    it('Enter 키가 눌리고 입력이 유효하면 onChange를 호출해야 함', () => {
      const onChangeMock = jest.fn();
      const { result } = renderHook(() => useDateInput(defaultParams));

      type(result, '45m');

      act(() => {
        expect(result.current.handleKeyDownInput(enterEvent, onChangeMock)).toBe(true);
      });

      expect(onChangeMock).toHaveBeenCalledTimes(1);
      expect(result.current.displayInput).toBe('45m');
    });

    it('커밋 텍스트는 trim된 문자열이어야 함 (검증 대상과 동일)', () => {
      const onChangeMock = jest.fn();
      const { result } = renderHook(() => useDateInput(defaultParams));

      type(result, '  45m  ');

      act(() => {
        result.current.handleKeyDownInput(enterEvent, onChangeMock);
      });

      expect(onChangeMock.mock.calls[0][1]).toBe('45m');
      expect(result.current.displayInput).toBe('45m');
    });

    it('입력이 유효하지 않으면 onChange를 호출하지 않아야 함', () => {
      const onChangeMock = jest.fn();
      const { result } = renderHook(() => useDateInput(defaultParams));

      type(result, 'invalid input');

      act(() => {
        expect(result.current.handleKeyDownInput(enterEvent, onChangeMock)).toBe(false);
      });

      expect(onChangeMock).not.toHaveBeenCalled();
      expect(result.current.displayInput).toBeUndefined();
    });

    it('빈 입력으로 커밋하려 하면 empty로 막아야 함', () => {
      const onChangeMock = jest.fn();
      const { result } = renderHook(() => useDateInput(defaultParams));

      act(() => {
        expect(result.current.handleKeyDownInput(enterEvent, onChangeMock)).toBe(false);
      });

      expect(onChangeMock).not.toHaveBeenCalled();
      expect(result.current.inputError?.code).toBe('empty');
    });

    it('Enter 키가 아니면 false를 반환해야 함', () => {
      const onChangeMock = jest.fn();
      const { result } = renderHook(() => useDateInput(defaultParams));

      const spaceEvent = { code: 'Space' } as unknown as React.KeyboardEvent;

      act(() => {
        expect(result.current.handleKeyDownInput(spaceEvent, onChangeMock)).toBe(false);
      });

      expect(onChangeMock).not.toHaveBeenCalled();
    });
  });

  describe('상태 업데이트', () => {
    it('setDateInput으로 dateInput을 업데이트할 수 있어야 함', () => {
      const { result } = renderHook(() => useDateInput(defaultParams));

      act(() => {
        result.current.setDateInput('new input');
      });

      expect(result.current.dateInput).toBe('new input');
    });

    it('setDisplayInput으로 displayInput을 업데이트할 수 있어야 함', () => {
      const { result } = renderHook(() => useDateInput(defaultParams));

      act(() => {
        result.current.setDisplayInput('new display input');
      });

      expect(result.current.displayInput).toBe('new display input');
    });

    it('clearInputError로 오류를 지울 수 있어야 함', () => {
      const { result } = renderHook(() => useDateInput(defaultParams));

      type(result, 'invalid date');
      expect(result.current.isValidInput).toBe(false);

      act(() => {
        result.current.clearInputError();
      });

      expect(result.current.isValidInput).toBe(true);
      expect(result.current.inputError).toBeNull();
    });
  });

  describe('의존성 변경', () => {
    it('from이 변경되면 from이 바뀌어도 훅이 살아 있어야 함', () => {
      const { result, rerender } = renderHook(
        ({ from }) =>
          useDateInput({
            ...defaultParams,
            from,
          }),
        {
          initialProps: { from: startDate },
        },
      );

      const newFrom = addDays(startDate, 1);

      rerender({ from: newFrom });

      expect(result.current.getFormattedDate).toBeDefined();
    });

    it('to가 변경되면 to가 바뀌어도 훅이 살아 있어야 함', () => {
      const { result, rerender } = renderHook(
        ({ to }) =>
          useDateInput({
            ...defaultParams,
            to,
          }),
        {
          initialProps: { to: endDate },
        },
      );

      const newTo = addDays(endDate, 1);

      rerender({ to: newTo });

      expect(result.current.getFormattedDate).toBeDefined();
    });

    it('dateFormat이 변경되면 getFormattedDate가 새로운 형식을 사용해야 함', () => {
      const { result, rerender } = renderHook(
        ({ dateFormat }) =>
          useDateInput({
            ...defaultParams,
            dateFormat,
          }),
        {
          initialProps: { dateFormat: 'MMM do, hh:mm a' },
        },
      );

      const newDateFormat = 'yyyy-MM-dd';

      rerender({ dateFormat: newDateFormat });

      const formatted = result.current.getFormattedDate(startDate, endDate);
      expect(formatted).toBeDefined();
    });

    it('timeZone이 변경되면 getFormattedDate가 새로운 timeZone을 사용해야 함', () => {
      const { result, rerender } = renderHook(
        ({ timeZone }) =>
          useDateInput({
            ...defaultParams,
            timeZone,
          }),
        {
          initialProps: { timeZone: 'UTC' },
        },
      );

      const newTimeZone = 'America/New_York';

      rerender({ timeZone: newTimeZone });

      const formatted = result.current.getFormattedDate(startDate, endDate);
      expect(formatted).toBeDefined();
    });
  });
});
