import { useState, useRef, useMemo, useCallback, type RefObject } from 'react';
import { useOnClickOutside } from 'usehooks-ts';
import { useUpdateEffect } from '../../utils/useUpdateEffect';
import { useCaptureKeydown } from '../../utils/useCaptureKeydown';
import { getLocale } from '../../utils/locale';
import { getZonedEndOfDay } from '../../utils/date';
import { getBuiltinRangeError, normalizeValidateResult } from '../../utils/validateRange';
import { LocaleKey, DateRange, PickerErrorState, ValidateRangeResult } from '../../types';
import { SEAM_TOKEN } from '../../constants/patterns';

interface UseRichDatetimePickerParams {
  startDate?: Date | null;
  endDate?: Date | null;
  localeKey?: LocaleKey;
  timeZone?: string;
  seamToken?: string;
  defaultOpen?: boolean;
  getPanelContainer?: () => HTMLElement | null;
  minDate?: Date;
  maxDate?: Date;
  validateDatePickerRange?: (params: DateRange) => ValidateRangeResult;
  onChange?: (dates: DateRange, text: string, timeUnit?: string) => void;
  getFormattedDate: (from: Date, to: Date) => string;
}

export const useRichDatetimePicker = ({
  startDate,
  endDate,
  localeKey = 'en',
  timeZone,
  seamToken = SEAM_TOKEN,
  defaultOpen,
  getPanelContainer,
  minDate,
  maxDate,
  validateDatePickerRange = () => true,
  onChange,
  getFormattedDate,
}: UseRichDatetimePickerParams) => {
  const tz = timeZone || Intl.DateTimeFormat().resolvedOptions().timeZone;
  const hasPanelContainer = getPanelContainer && getPanelContainer?.();
  const containerRef = useRef<HTMLDivElement>(null);
  const triggerRef = useRef<HTMLDivElement>(null);
  const displayInputRef = useRef<HTMLInputElement>(null);

  const locale = useMemo(() => getLocale(localeKey), [localeKey]);
  // 커밋된 값 (Apply 전까지 draft 편집에 영향받지 않는다)
  const [from, setFrom] = useState<Date | null | undefined>(startDate);
  const [to, setTo] = useState<Date | null | undefined>(endDate);
  // 편집 중인 pending 값 (캘린더 / 시간 배지)
  const [draftFrom, setDraftFrom] = useState<Date | null | undefined>(startDate);
  const [draftTo, setDraftTo] = useState<Date | null | undefined>(endDate);
  const [open, setOpen] = useState(defaultOpen ?? false);
  const [rangeError, setRangeError] = useState<PickerErrorState | null>(null);
  // props에서 100% 파생 — state로 미러링하지 않는다 (seamToken/timeZone 변경이 즉시 반영됨)
  const appContext = useMemo(() => ({ seamToken, timeZone: tz }), [seamToken, tz]);

  // 취소: draft를 커밋값으로 원복하고 닫는다. rangeError만 지우고 inputError·입력 텍스트는 그대로 둔다 —
  // 텍스트 입력은 패널 draft와 독립이라 원복 대상이 아니고, 입력값이 남는데 무효 표시만 사라지면 앞뒤가 안 맞는다.
  const handleCancel = useCallback(() => {
    setDraftFrom(from);
    setDraftTo(to);
    setRangeError(null);
    setOpen(false);
  }, [from, to]);

  useOnClickOutside(containerRef as RefObject<HTMLDivElement>, () => {
    !hasPanelContainer && handleCancel();
  });

  useCaptureKeydown((event) => {
    if (event.code === 'Escape') {
      open && handleCancel();
    }
  });

  // 외부에서 값이 바뀌면 직전 검증 실패 문구는 무효다.
  // 참조가 아니라 시각으로 비교한다. 소비자가 렌더마다 같은 시각의 새 Date를 넘기면(문자열을 매번
  // parse하는 경우) 참조 비교로는 매 렌더가 "값 변경"이 되어 오류 문구와 입력 중인 텍스트가 지워진다.
  const startTime = startDate?.getTime();
  const endTime = endDate?.getTime();

  useUpdateEffect(() => {
    setFrom(startDate);
    setRangeError(null);
  }, [startTime]);

  useUpdateEffect(() => {
    setTo(endDate);
    setRangeError(null);
  }, [endTime]);

  // draft 스냅샷의 **주 경로는 `openPanel`**이다(캘린더가 stale props로 mount되지 않도록 동기 수행).
  // 이 effect는 `setOpen`이 훅 반환값으로 노출돼 있어 `openPanel`을 우회해 열리는 경우를 위한 방어막이다.
  // 평상시엔 openPanel이 방금 쓴 값을 다시 쓰는 no-op이다.
  // deps는 open만 — prop 동기화가 편집 중 draft를 덮지 않도록.
  useUpdateEffect(() => {
    if (open) {
      setDraftFrom(from);
      setDraftTo(to);
    }
  }, [open]);

  // 패널이 닫힐 때 입력 포커스 해제
  useUpdateEffect(() => {
    if (!open) {
      displayInputRef.current?.blur();
    }
  }, [open]);

  // 캘린더 선택 → draft만 갱신 (commit/close 없음).
  // 캘린더는 날짜 단위 컨트롤이므로 `DatePicker`가 스냅해 준 하루 통째(00:00:00.000 ~ 23:59:59.999)를
  // 그대로 받는다. 시각 조정은 시간 배지가 담당한다.
  // 여기에 기존 벽시계 시각을 다시 입히지 말 것 — start만 옛 시각이 남아 end와 비대칭이 된다(NELO-2284).
  const handleDraftDatePickerChange = useCallback((dates: DateRange) => {
    setDraftFrom(dates[0]);
    setDraftTo(dates[1]);
    setRangeError(null);
  }, []);

  // 시간 배지 편집 → 해당 draft만 갱신
  const handleTimeBadgeChange = useCallback((which: 'from' | 'to', next: Date) => {
    if (which === 'from') setDraftFrom(next);
    else setDraftTo(next);
    setRangeError(null);
  }, []);

  // consumer 검증만 돌린다. 퀵레인지처럼 라이브러리가 만든 값에도 공통으로 쓰인다.
  const runConsumerValidation = useCallback(
    (range: DateRange): PickerErrorState | null => {
      const { valid, message } = normalizeValidateResult(validateDatePickerRange(range));
      return valid ? null : { code: 'range-invalid', source: 'range', message };
    },
    [validateDatePickerRange],
  );

  // Apply 경로의 전체 검증 체인. 내장 검증을 consumer보다 먼저 둔다 —
  // consumer 콜백은 "말이 되는 범위"가 들어온다고 전제하고 쓰여 있다.
  const runRangeValidation = useCallback(
    (f: Date, t: Date): PickerErrorState | null => {
      // 내장 판정은 텍스트 입력 경로(diagnoseDateInput)와 같은 함수를 쓴다.
      const builtin = getBuiltinRangeError(f, t, tz, minDate, maxDate);
      if (builtin) return { code: builtin, source: 'range' };
      return runConsumerValidation([f, t]);
    },
    [minDate, maxDate, tz, runConsumerValidation],
  );

  // Apply → draft를 커밋하고 onChange + 닫기. 실패하면 패널을 열어둔 채 사유를 남긴다.
  // 반환값은 커밋 여부 — 실패했는데 트리거 라벨을 지우면 안 되기 때문이다.
  const handleApply = useCallback(() => {
    const f = draftFrom;
    // start만 선택된 경우 end를 그 날의 끝으로 보정.
    // 보정 전에 reversed를 보면 "시작일만 선택"이 항상 역순으로 걸린다.
    const t = draftTo || (f instanceof Date ? getZonedEndOfDay(f, tz) : draftTo);
    if (!(f instanceof Date) || !(t instanceof Date)) {
      setRangeError({ code: 'empty', source: 'range' });
      return false;
    }

    const error = runRangeValidation(f, t);
    if (error) {
      setRangeError(error);
      return false;
    }

    setRangeError(null);
    setFrom(f);
    setTo(t);
    onChange?.([f, t], getFormattedDate(f, t));
    setOpen(false);
    return true;
  }, [draftFrom, draftTo, tz, runRangeValidation, onChange, getFormattedDate]);

  // 퀵레인지 / 커스텀타임 → Apply를 거치지 않고 즉시 커밋.
  // draft까지 함께 맞춰야 다음에 패널을 열 때 캘린더가 이전 범위로 mount되지 않는다 (NELO-2280).
  // 값은 라이브러리가 만든 것이라 내장 검증(역순·min/max)은 걸지 않는다.
  // 다만 consumer 정책("최대 7일")까지 우회하면 검증 구멍이 되므로 그것만 돌린다.
  // 실패 시 커밋하지 않고 패널을 열어둔 채 사유를 남긴다(반환값이 커밋 여부).
  const handlePanelChange = useCallback(
    (dates: DateRange, text: string, timeUnit?: string) => {
      const error = runConsumerValidation(dates);
      if (error) {
        setRangeError(error);
        return false;
      }

      setRangeError(null);
      setFrom(dates[0]);
      setTo(dates[1]);
      setDraftFrom(dates[0]);
      setDraftTo(dates[1]);
      setOpen(false);
      onChange?.(dates, text, timeUnit);
      return true;
    },
    [onChange, runConsumerValidation],
  );

  // 패널 열기. draft 스냅샷을 setOpen과 같은 배치에서 동기적으로 수행한다.
  // open 변화를 보는 useUpdateEffect 스냅샷만으로는 캘린더가 이미 stale props로 mount된 뒤에
  // 실행되고, react-datepicker는 preSelection을 월/연 단위로만 재동기화하므로
  // 같은 달 안의 stale 하이라이트가 남는다 (NELO-2280).
  // onOpen은 트리거 클릭과 입력 focus 양쪽에서, 패널이 이미 열려 있어도 발화한다.
  // 그때 스냅샷을 다시 찍으면 편집 중이던 draft가 커밋값으로 되돌아간다.
  const openPanel = useCallback(() => {
    if (open) return;
    setDraftFrom(from);
    setDraftTo(to);
    setRangeError(null);
    setOpen(true);
  }, [open, from, to]);

  return {
    locale,
    from,
    to,
    draftFrom,
    draftTo,
    open,
    setOpen,
    openPanel,
    rangeError,
    setRangeError,
    appContext,
    containerRef,
    triggerRef,
    displayInputRef,
    handleDraftDatePickerChange,
    handleTimeBadgeChange,
    handleApply,
    handleCancel,
    handlePanelChange,
    runConsumerValidation,
  };
};
