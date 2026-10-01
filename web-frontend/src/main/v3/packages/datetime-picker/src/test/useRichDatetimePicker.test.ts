import { renderHook, act } from '@testing-library/react';
import { useRichDatetimePicker } from '../components/hooks/useRichDatetimePicker';
import { subMinutes, addDays } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';
import { DateRange } from '../types';
import { SEAM_TOKEN } from '../constants/patterns';
import { getZonedEndOfDay, getZonedStartOfDay, setZonedTimeOfDay } from '../utils/date';

// Mock dependencies
jest.mock('../utils/useCaptureKeydown', () => ({
  useCaptureKeydown: jest.fn(),
}));

jest.mock('usehooks-ts', () => ({
  useOnClickOutside: jest.fn(),
}));

jest.mock('../utils/useUpdateEffect', () => ({
  useUpdateEffect: jest.fn(),
}));

describe('useRichDatetimePicker', () => {
  const TZ = 'Asia/Seoul';
  const readZoned = (date: Date) => formatInTimeZone(date, TZ, 'yyyy-MM-dd HH:mm:ss.SSS');
  const now = new Date();
  const startDate = subMinutes(now, 5);
  const endDate = now;
  const mockGetFormattedDate = jest.fn((from: Date, to: Date) => {
    return `${from.toISOString()} ${SEAM_TOKEN} ${to.toISOString()}`;
  });

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('초기화', () => {
    it('기본값으로 초기화되어야 함', () => {
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      expect(result.current.from).toBeUndefined();
      expect(result.current.to).toBeUndefined();
      expect(result.current.open).toBe(false);
      expect(result.current.locale).toBeDefined();
      expect(result.current.containerRef).toBeDefined();
      expect(result.current.triggerRef).toBeDefined();
      expect(result.current.displayInputRef).toBeDefined();
    });

    it('startDate와 endDate로 초기화되어야 함', () => {
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          startDate,
          endDate,
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      expect(result.current.from).toEqual(startDate);
      expect(result.current.to).toEqual(endDate);
    });

    it('defaultOpen이 true일 때 패널이 열려있어야 함', () => {
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          defaultOpen: true,
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      expect(result.current.open).toBe(true);
    });

    it('localeKey에 따라 올바른 locale을 반환해야 함', () => {
      const { result: resultEn } = renderHook(() =>
        useRichDatetimePicker({
          localeKey: 'en',
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      const { result: resultKo } = renderHook(() =>
        useRichDatetimePicker({
          localeKey: 'ko',
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      expect(resultEn.current.locale.code).toBe('en-US');
      expect(resultKo.current.locale.code).toBe('ko');
    });

    it('timeZone이 제공되지 않으면 시스템 timeZone을 사용해야 함', () => {
      const systemTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      expect(result.current.appContext.timeZone).toBe(systemTimeZone);
    });

    it('커스텀 timeZone을 사용해야 함', () => {
      const customTimeZone = 'America/New_York';
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          timeZone: customTimeZone,
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      expect(result.current.appContext.timeZone).toBe(customTimeZone);
    });
  });

  describe('상태 관리', () => {
    it('setOpen으로 패널을 열고 닫을 수 있어야 함', () => {
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      act(() => {
        result.current.setOpen(true);
      });

      expect(result.current.open).toBe(true);

      act(() => {
        result.current.setOpen(false);
      });

      expect(result.current.open).toBe(false);
    });

    // Note: useUpdateEffect가 mock되어 있으므로 prop 변경 테스트는
    // 통합 테스트(RichDatetimePicker.test.tsx)에서 검증합니다.
  });

  describe('handleDraftDatePickerChange (draft만 갱신)', () => {
    it('draft만 갱신하고 커밋값(from/to)/onChange/open은 건드리지 않아야 함', () => {
      const onChangeMock = jest.fn();
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          defaultOpen: true,
          timeZone: TZ,
          onChange: onChangeMock,
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      // DatePicker가 넘겨주는 형태(start/end-of-day로 스냅된 값)
      const newStartDate = getZonedStartOfDay(new Date('2026-07-13T03:00:00Z'), TZ);
      const newEndDate = getZonedEndOfDay(addDays(newStartDate, 1), TZ);
      const dates: DateRange = [newStartDate, newEndDate];

      act(() => {
        result.current.handleDraftDatePickerChange(dates);
      });

      expect(result.current.draftFrom).toEqual(newStartDate);
      expect(result.current.draftTo).toEqual(newEndDate);
      // 커밋값은 그대로
      expect(result.current.from).toBeUndefined();
      expect(result.current.to).toBeUndefined();
      expect(onChangeMock).not.toHaveBeenCalled();
      expect(result.current.open).toBe(true);
    });

    // 캘린더는 하루 통째를 고르는 컨트롤이다. DatePicker가 스냅해 준 값을 훅이 손대지 않고
    // 그대로 draft에 넣어야 배지가 00:00:00 ~ 23:59:59로 표시된다 (NELO-2284).
    it('DatePicker가 스냅한 하루 통째 값을 가공 없이 draft에 반영해야 함', () => {
      const initialStart = setZonedTimeOfDay(new Date('2026-07-13T00:00:00Z'), TZ, {
        h: 9,
        m: 30,
        s: 15,
      });
      const initialEnd = setZonedTimeOfDay(new Date('2026-07-15T00:00:00Z'), TZ, {
        h: 18,
        m: 45,
        s: 5,
      });

      const { result } = renderHook(() =>
        useRichDatetimePicker({
          startDate: initialStart,
          endDate: initialEnd,
          defaultOpen: true,
          timeZone: TZ,
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      const clicked = getZonedStartOfDay(new Date('2026-07-20T03:00:00Z'), TZ);
      const clickedEnd = getZonedEndOfDay(new Date('2026-07-22T03:00:00Z'), TZ);

      // 1st click: 범위가 리셋되며 end가 잠시 null이 된다
      act(() => {
        result.current.handleDraftDatePickerChange([clicked, null]);
      });
      expect(result.current.draftTo).toBeNull();

      // 2nd click: 범위 완성 — 이전 시각(09:30:15 / 18:45:05)이 되살아나면 안 된다
      act(() => {
        result.current.handleDraftDatePickerChange([clicked, clickedEnd]);
      });

      expect(readZoned(result.current.draftFrom as Date)).toBe('2026-07-20 00:00:00.000');
      expect(readZoned(result.current.draftTo as Date)).toBe('2026-07-22 23:59:59.999');
    });
  });

  describe('handleApply (draft 커밋)', () => {
    it('draft를 커밋하고 onChange 호출 후 닫아야 함', () => {
      const onChangeMock = jest.fn();
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          defaultOpen: true,
          timeZone: TZ,
          onChange: onChangeMock,
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      const newStartDate = getZonedStartOfDay(new Date('2026-07-13T03:00:00Z'), TZ);
      const newEndDate = getZonedEndOfDay(addDays(newStartDate, 1), TZ);

      act(() => {
        result.current.handleDraftDatePickerChange([newStartDate, newEndDate]);
      });
      act(() => {
        result.current.handleApply();
      });

      expect(result.current.from).toEqual(newStartDate);
      expect(result.current.to).toEqual(newEndDate);
      expect(onChangeMock).toHaveBeenCalledWith(
        [newStartDate, newEndDate],
        mockGetFormattedDate(newStartDate, newEndDate),
      );
      expect(result.current.open).toBe(false);
    });

    it('start만 선택된 draft는 end를 그 날의 끝으로 보정해 커밋해야 함', () => {
      const onChangeMock = jest.fn();
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          defaultOpen: true,
          timeZone: TZ,
          onChange: onChangeMock,
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      const newStartDate = getZonedStartOfDay(new Date('2026-07-13T03:00:00Z'), TZ);

      act(() => {
        result.current.handleDraftDatePickerChange([newStartDate, null]);
      });
      act(() => {
        result.current.handleApply();
      });

      expect(result.current.from).toEqual(newStartDate);
      expect(readZoned(result.current.to as Date)).toBe('2026-07-13 23:59:59.999');
      expect(onChangeMock).toHaveBeenCalledTimes(1);
      expect(result.current.open).toBe(false);
    });

    // 이전에 시각이 들어간 범위를 쓰고 있었더라도, 하루만 다시 고르면 그 하루 통째가 되어야 한다
    it('이전 범위의 시각이 단일 날짜 선택 결과에 되살아나면 안 됨', () => {
      const initialStart = setZonedTimeOfDay(new Date('2026-07-13T00:00:00Z'), TZ, {
        h: 9,
        m: 0,
        s: 0,
      });
      const initialEnd = setZonedTimeOfDay(new Date('2026-07-15T00:00:00Z'), TZ, {
        h: 18,
        m: 45,
        s: 5,
      });

      const { result } = renderHook(() =>
        useRichDatetimePicker({
          startDate: initialStart,
          endDate: initialEnd,
          defaultOpen: true,
          timeZone: TZ,
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      const clicked = getZonedStartOfDay(new Date('2026-07-20T03:00:00Z'), TZ);

      act(() => {
        result.current.handleDraftDatePickerChange([clicked, null]);
      });
      act(() => {
        result.current.handleApply();
      });

      expect(readZoned(result.current.from as Date)).toBe('2026-07-20 00:00:00.000');
      expect(readZoned(result.current.to as Date)).toBe('2026-07-20 23:59:59.999');
    });

    it('validateDatePickerRange가 false면 커밋하지 않아야 함', () => {
      const validateDatePickerRangeMock = jest.fn(() => false);
      const onChangeMock = jest.fn();
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          defaultOpen: true,
          timeZone: TZ,
          validateDatePickerRange: validateDatePickerRangeMock,
          onChange: onChangeMock,
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      const newStartDate = getZonedStartOfDay(new Date('2026-07-13T03:00:00Z'), TZ);
      const newEndDate = getZonedEndOfDay(addDays(newStartDate, 1), TZ);

      act(() => {
        result.current.handleDraftDatePickerChange([newStartDate, newEndDate]);
      });
      act(() => {
        result.current.handleApply();
      });

      expect(validateDatePickerRangeMock).toHaveBeenCalledWith([newStartDate, newEndDate]);
      expect(onChangeMock).not.toHaveBeenCalled();
      expect(result.current.from).toBeUndefined();
      expect(result.current.open).toBe(true);
    });
  });

  describe('범위 검증 오류', () => {
    const renderWithDraft = (
      params: Parameters<typeof useRichDatetimePicker>[0],
      draft: DateRange,
    ) => {
      const rendered = renderHook(() => useRichDatetimePicker(params));
      act(() => {
        rendered.result.current.handleDraftDatePickerChange(draft);
      });
      return rendered;
    };

    const base = { defaultOpen: true, timeZone: TZ, getFormattedDate: mockGetFormattedDate };
    const day = (iso: string) => getZonedStartOfDay(new Date(iso), TZ);

    it('드래프트가 없으면 empty 사유를 남기고 패널을 유지해야 함', () => {
      const onChangeMock = jest.fn();
      const { result } = renderHook(() =>
        useRichDatetimePicker({ ...base, onChange: onChangeMock }),
      );

      act(() => {
        expect(result.current.handleApply()).toBe(false);
      });

      expect(result.current.rangeError).toEqual({ code: 'empty', source: 'range' });
      expect(onChangeMock).not.toHaveBeenCalled();
      expect(result.current.open).toBe(true);
    });

    it('시작이 종료보다 늦으면 reversed로 막아야 함', () => {
      const onChangeMock = jest.fn();
      const { result } = renderWithDraft({ ...base, onChange: onChangeMock }, [
        day('2026-07-20T00:00:00Z'),
        getZonedEndOfDay(day('2026-07-10T00:00:00Z'), TZ),
      ]);

      act(() => {
        result.current.handleApply();
      });

      expect(result.current.rangeError?.code).toBe('reversed');
      expect(onChangeMock).not.toHaveBeenCalled();
      expect(result.current.open).toBe(true);
    });

    it('minDate 밖이면 out-of-bounds로 막아야 함', () => {
      const onChangeMock = jest.fn();
      const { result } = renderWithDraft(
        { ...base, minDate: day('2026-07-15T00:00:00Z'), onChange: onChangeMock },
        [day('2026-07-10T00:00:00Z'), getZonedEndOfDay(day('2026-07-20T00:00:00Z'), TZ)],
      );

      act(() => {
        result.current.handleApply();
      });

      expect(result.current.rangeError?.code).toBe('out-of-bounds');
      expect(onChangeMock).not.toHaveBeenCalled();
    });

    it('내장 검증이 consumer 검증보다 먼저 돈다', () => {
      const validate = jest.fn(() => true);
      const { result } = renderWithDraft({ ...base, validateDatePickerRange: validate }, [
        day('2026-07-20T00:00:00Z'),
        getZonedEndOfDay(day('2026-07-10T00:00:00Z'), TZ),
      ]);

      act(() => {
        result.current.handleApply();
      });

      expect(result.current.rangeError?.code).toBe('reversed');
      expect(validate).not.toHaveBeenCalled();
    });

    it('consumer가 문자열을 반환하면 그 문구를 오류에 담아야 함', () => {
      const { result } = renderWithDraft(
        { ...base, validateDatePickerRange: () => '최대 2일까지 선택할 수 있어요' },
        [day('2026-07-10T00:00:00Z'), getZonedEndOfDay(day('2026-07-20T00:00:00Z'), TZ)],
      );

      act(() => {
        result.current.handleApply();
      });

      expect(result.current.rangeError).toEqual({
        code: 'range-invalid',
        source: 'range',
        message: '최대 2일까지 선택할 수 있어요',
      });
    });

    it('consumer가 { valid: false }만 반환하면 문구는 비워 기본 문구로 넘긴다', () => {
      const { result } = renderWithDraft(
        { ...base, validateDatePickerRange: () => ({ valid: false }) },
        [day('2026-07-10T00:00:00Z'), getZonedEndOfDay(day('2026-07-20T00:00:00Z'), TZ)],
      );

      act(() => {
        result.current.handleApply();
      });

      expect(result.current.rangeError?.code).toBe('range-invalid');
      expect(result.current.rangeError?.message).toBeUndefined();
    });

    it.each([
      [
        '캘린더 재선택',
        (r: { current: ReturnType<typeof useRichDatetimePicker> }) =>
          r.current.handleDraftDatePickerChange([day('2026-07-11T00:00:00Z'), null]),
      ],
      [
        '시간배지 편집',
        (r: { current: ReturnType<typeof useRichDatetimePicker> }) =>
          r.current.handleTimeBadgeChange('from', day('2026-07-11T00:00:00Z')),
      ],
      [
        '취소',
        (r: { current: ReturnType<typeof useRichDatetimePicker> }) => r.current.handleCancel(),
      ],
    ])('%s 시 오류를 지워야 함', (_label, action) => {
      const { result } = renderWithDraft({ ...base, validateDatePickerRange: () => false }, [
        day('2026-07-10T00:00:00Z'),
        getZonedEndOfDay(day('2026-07-20T00:00:00Z'), TZ),
      ]);

      act(() => {
        result.current.handleApply();
      });
      expect(result.current.rangeError).not.toBeNull();

      act(() => {
        action(result);
      });

      expect(result.current.rangeError).toBeNull();
    });

    it('퀵레인지도 consumer 검증에 걸리면 커밋하지 않아야 함', () => {
      const onChangeMock = jest.fn();
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          ...base,
          validateDatePickerRange: () => '허용되지 않는 범위',
          onChange: onChangeMock,
        }),
      );

      act(() => {
        expect(result.current.handlePanelChange([day('2026-07-10T00:00:00Z'), now], '7d')).toBe(
          false,
        );
      });

      expect(result.current.rangeError?.message).toBe('허용되지 않는 범위');
      expect(onChangeMock).not.toHaveBeenCalled();
      expect(result.current.from).toBeUndefined();
      expect(result.current.open).toBe(true);
    });

    it('퀵레인지에는 내장 min/max 검증을 걸지 않는다', () => {
      const onChangeMock = jest.fn();
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          ...base,
          minDate: day('2030-01-01T00:00:00Z'),
          onChange: onChangeMock,
        }),
      );

      act(() => {
        expect(result.current.handlePanelChange([day('2026-07-10T00:00:00Z'), now], '7d')).toBe(
          true,
        );
      });

      expect(result.current.rangeError).toBeNull();
      expect(onChangeMock).toHaveBeenCalledTimes(1);
    });
  });

  describe('handleCancel (draft 원복)', () => {
    it('draft를 커밋값으로 원복하고 닫아야 함', () => {
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          startDate,
          endDate,
          defaultOpen: true,
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      act(() => {
        result.current.handleDraftDatePickerChange([addDays(startDate, 3), addDays(endDate, 3)]);
      });
      act(() => {
        result.current.handleCancel();
      });

      expect(result.current.draftFrom).toEqual(startDate);
      expect(result.current.draftTo).toEqual(endDate);
      expect(result.current.open).toBe(false);
    });
  });

  describe('handlePanelChange', () => {
    it('패널을 닫고 onChange를 호출해야 함', () => {
      const onChangeMock = jest.fn();
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          onChange: onChangeMock,
          defaultOpen: true,
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      const dates: DateRange = [startDate, endDate];
      const text = 'test text';
      const timeUnit = '5m';

      act(() => {
        result.current.handlePanelChange(dates, text, timeUnit);
      });

      expect(result.current.open).toBe(false);
      expect(onChangeMock).toHaveBeenCalledWith(dates, text, timeUnit);
    });

    // NELO-2280: 퀵레인지가 draft를 갱신하지 않으면 다음에 패널을 열 때 캘린더가 이전 범위로
    // mount되고, react-datepicker의 preSelection이 그 날짜에 고정되어 하이라이트 잔상이 남는다.
    it('커밋값과 draft를 모두 새 범위로 갱신해야 함', () => {
      const previousStart = new Date('2026-07-28T00:00:00Z');
      const previousEnd = new Date('2026-07-30T00:00:00Z');
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          startDate: previousStart,
          endDate: previousEnd,
          defaultOpen: true,
          timeZone: TZ,
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      const quickStart = new Date('2026-07-31T05:55:00Z');
      const quickEnd = new Date('2026-07-31T06:00:00Z');

      act(() => {
        result.current.handlePanelChange([quickStart, quickEnd], 'last 5 minutes', '5m');
      });

      expect(result.current.from).toEqual(quickStart);
      expect(result.current.to).toEqual(quickEnd);
      // 이전 캘린더 범위가 draft에 남아있으면 안 된다
      expect(result.current.draftFrom).toEqual(quickStart);
      expect(result.current.draftTo).toEqual(quickEnd);
    });
  });

  describe('openPanel (동기 draft 스냅샷)', () => {
    // NELO-2280: 기존 useUpdateEffect 스냅샷은 캘린더가 stale props로 mount된 뒤에 실행된다.
    it('setOpen과 같은 배치에서 draft를 커밋값으로 스냅샷해야 함', () => {
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          startDate,
          endDate,
          timeZone: TZ,
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      // 편집 중이던 draft를 커밋값과 다르게 만든다
      act(() => {
        result.current.handleTimeBadgeChange('from', new Date('2026-01-01T00:00:00Z'));
      });
      expect(result.current.draftFrom).not.toEqual(startDate);

      act(() => {
        result.current.openPanel();
      });

      expect(result.current.open).toBe(true);
      expect(result.current.draftFrom).toEqual(startDate);
      expect(result.current.draftTo).toEqual(endDate);
    });
  });

  describe('appContext 관리', () => {
    it('seamToken을 설정해야 함', () => {
      const customSeamToken = '~';
      const { result } = renderHook(() =>
        useRichDatetimePicker({
          seamToken: customSeamToken,
          getFormattedDate: mockGetFormattedDate,
        }),
      );

      expect(result.current.appContext.seamToken).toBe(customSeamToken);
    });

    it('seamToken/timeZone prop 변경이 appContext에 즉시 반영되어야 함', () => {
      const { result, rerender } = renderHook(
        (props: { seamToken: string; timeZone: string }) =>
          useRichDatetimePicker({
            seamToken: props.seamToken,
            timeZone: props.timeZone,
            getFormattedDate: mockGetFormattedDate,
          }),
        { initialProps: { seamToken: '-', timeZone: 'UTC' } },
      );

      expect(result.current.appContext).toEqual({ seamToken: '-', timeZone: 'UTC' });

      rerender({ seamToken: '~', timeZone: 'Asia/Seoul' });

      expect(result.current.appContext).toEqual({ seamToken: '~', timeZone: 'Asia/Seoul' });
    });
  });
});
