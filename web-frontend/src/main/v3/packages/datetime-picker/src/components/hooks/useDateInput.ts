import { useState, useCallback } from 'react';
import { Locale } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';
import { diagnoseDateInput } from '../../utils/diagnoseInput';
import { DateRange, PickerErrorState } from '../../types';

interface UseDateInputParams {
  from: Date | null | undefined;
  to: Date | null | undefined;
  locale: Locale;
  dateFormat: string;
  timeZone: string;
  seamToken: string;
  minDate?: Date;
  maxDate?: Date;
  /**
   * 커밋 직전 consumer 정책을 확인한다. 텍스트 입력도 Apply·퀵레인지와 같은 검증을 거쳐야
   * "타이핑으로는 정책을 우회할 수 있는" 구멍이 생기지 않는다.
   */
  validateRange?: (dates: DateRange) => PickerErrorState | null;
}

export const useDateInput = ({
  from,
  to,
  locale,
  dateFormat,
  timeZone,
  seamToken,
  minDate,
  maxDate,
  validateRange,
}: UseDateInputParams) => {
  const [inputError, setInputError] = useState<PickerErrorState | null>(null);
  const [dateInput, setDateInput] = useState('');
  const [displayInput, setDisplayInput] = useState<string | undefined>();

  // 파생 상태 — 별도 state로 두지 않는다.
  const isValidInput = inputError === null;

  const getFormattedDate = useCallback(
    (fromDate: Date, toDate: Date) => {
      const zonedFrom = formatInTimeZone(fromDate, timeZone, dateFormat, { locale });
      const zonedTo = formatInTimeZone(toDate, timeZone, dateFormat, { locale });
      return `${zonedFrom} ${seamToken} ${zonedTo}`;
    },
    [locale, dateFormat, timeZone, seamToken],
  );

  const clearInputError = useCallback(() => setInputError(null), []);

  const diagnose = useCallback(
    (input: string, allowEmpty: boolean) =>
      diagnoseDateInput({
        input,
        locale,
        timeZone,
        seamToken,
        dateFormat,
        minDate,
        maxDate,
        allowEmpty,
        referenceDate: [from as Date, to as Date],
      }),
    [locale, dateFormat, seamToken, timeZone, from, to, minDate, maxDate],
  );

  const handleChangeInput = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const newInput = e.target.value;
      // 타이핑 중에는 빈 입력을 오류로 보지 않는다 — 다 지운 상태가 빨갛게 되면 안 된다.
      const { code } = diagnose(newInput, true);
      setInputError(code ? { code, source: 'input' } : null);
      setDateInput(newInput);
    },
    [diagnose],
  );

  const handleKeyDownInput = useCallback(
    (e: React.KeyboardEvent, onChange?: (dates: DateRange, text: string) => void) => {
      if (e.code !== 'Enter') return false;

      // 커밋 시점에 한 번 더 진단한다. 그 결과의 dates/text를 그대로 쓰므로
      // 검증과 커밋이 서로 다른 문자열을 파싱하던 불일치가 없다.
      const { code, dates, text } = diagnose(dateInput, false);
      if (code) {
        setInputError({ code, source: 'input' });
        return false;
      }

      // 형태가 맞아도 consumer 정책에 걸릴 수 있다. 사유는 입력에서 났으므로
      // source는 'input'으로 남겨 트리거 테두리도 함께 빨개지게 한다.
      const rangeError = validateRange?.(dates);
      if (rangeError) {
        setInputError({ ...rangeError, source: 'input' });
        return false;
      }

      setInputError(null);
      onChange?.(dates, text);
      setDisplayInput(text);
      return true; // 패널 닫기 신호
    },
    [diagnose, dateInput, validateRange],
  );

  return {
    isValidInput,
    inputError,
    dateInput,
    displayInput,
    clearInputError,
    setDateInput,
    setDisplayInput,
    getFormattedDate,
    handleChangeInput,
    handleKeyDownInput,
  };
};
