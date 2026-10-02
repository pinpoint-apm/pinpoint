import { Locale, isValid } from 'date-fns';
import { DateRange, PickerErrorCode } from '../types';
import { getDateFromPatternedString, parseTimeString } from './date';
import { getBuiltinRangeError } from './validateRange';

export interface DiagnoseInputParams {
  input: string;
  locale: Locale;
  timeZone: string;
  seamToken: string;
  dateFormat?: string;
  referenceDate?: Date | Date[];
  minDate?: Date;
  maxDate?: Date;
  /** 타이핑 중에는 true — 빈 입력을 오류로 보지 않는다. Enter 커밋 시에는 false. */
  allowEmpty?: boolean;
}

export interface DiagnoseInputResult {
  /** 오류 사유. 유효하면 null. */
  code: PickerErrorCode | null;
  /** 진단에 쓴 파싱 결과. 커밋 경로가 그대로 재사용한다(재파싱 금지). */
  dates: DateRange;
  /** 진단과 커밋이 공유하는 정규화(trim)된 문자열. */
  text: string;
}

/**
 * 자유 텍스트 입력의 오류 사유를 판정한다.
 *
 * `parseTimeString`은 실패를 `Invalid Date`로만 표현해 사유가 소실되므로, 파싱 자체는
 * 건드리지 않고 바깥에서 같은 분기(패턴 문자열 → seamToken 분리 → 포맷 파싱)를 재현해
 * 사유를 되살린다. 파싱 결과와 정규화된 문자열을 함께 돌려주므로 호출부는 한 번만 파싱한다.
 */
export const diagnoseDateInput = ({
  input,
  locale,
  timeZone,
  seamToken,
  dateFormat,
  referenceDate,
  minDate,
  maxDate,
  allowEmpty = false,
}: DiagnoseInputParams): DiagnoseInputResult => {
  const text = input.trim();

  // 빈 입력은 파싱 전에 잡아야 한다. date-fns의 parse는 문자열과 포맷이 둘 다 비면
  // referenceDate를 그대로 돌려주므로, 여기서 거르지 않으면 영원히 유효로 통과한다.
  if (!text) {
    return { code: allowEmpty ? null : 'empty', dates: [null, null], text };
  }

  const dates = parseTimeString(text, locale, { dateFormat, seamToken, timeZone, referenceDate });
  const [from, to] = dates;

  const isPatterned = !!getDateFromPatternedString(text, timeZone);
  const hasSeam = !isPatterned && text.includes(seamToken);
  const parts = hasSeam ? text.split(seamToken) : [];
  const hasBlankPart = parts.some((part) => part.trim() === '');
  // seamToken이 날짜 내부 구분자와 같으면(기본 '-' vs '2024-01-01') 조각이 3개 이상으로 쪼개진다.
  // 진단만 하고 parseTimeString의 split 규칙은 바꾸지 않는다.
  const hasSeamConflict = hasSeam && parts.length >= 3;

  // 조각이 비어도 파싱은 '성공'할 수 있다('Sep 1st - '의 빈 조각이 오늘로 되살아난다).
  // 그래서 파싱 실패 여부와 무관하게 형태 검사를 먼저 돌린다.
  if (hasSeamConflict) return { code: 'seam-conflict', dates, text };
  if (hasBlankPart) return { code: 'incomplete', dates, text };

  if (!isValid(from) || !isValid(to)) {
    return { code: 'unparseable', dates, text };
  }

  // 역순·min/max 판정은 Apply 경로와 공유한다(둘이 갈라지면 입력과 Apply가 다른 답을 낸다).
  const rangeError = getBuiltinRangeError(from as Date, to as Date, timeZone, minDate, maxDate);
  if (rangeError) return { code: rangeError, dates, text };

  return { code: null, dates, text };
};
