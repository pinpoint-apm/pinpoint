import { PickerErrorCode, ValidateRangeResult } from '../types';
import { getZonedEndOfDay, getZonedStartOfDay } from './date';

export interface NormalizedValidateResult {
  valid: boolean;
  /** consumer가 직접 지정한 문구. 없으면 로케일 기본 문구를 쓴다. */
  message?: string;
}

/**
 * `validateDatePickerRange`의 반환값을 정규화한다.
 *
 * 반환 타입을 `boolean`에서 넓혔지만 기존 구현체는 그대로 동작해야 한다:
 * - `boolean` — 기존 시그니처
 * - `string` — 무효 + 그 문자열이 오류 문구 (빈 문자열이면 기본 문구)
 * - `{ valid, message }` — valid일 때 message는 경고 채널이 아니므로 버린다
 * - `undefined`/`null` — 콜백에서 return을 빠뜨린 경우. 기존에도 falsy=무효였으므로 유지한다.
 */
export const normalizeValidateResult = (result: ValidateRangeResult): NormalizedValidateResult => {
  if (typeof result === 'boolean') return { valid: result };
  if (typeof result === 'string') return { valid: false, message: result || undefined };
  if (result && typeof result === 'object') {
    return { valid: Boolean(result.valid), message: result.valid ? undefined : result.message };
  }
  return { valid: false };
};

/**
 * 라이브러리 내장 범위 검증. Apply 경로와 텍스트 입력 경로가 **같은 판정**을 내려야 하므로
 * 여기 한 곳에만 둔다.
 *
 * min/max는 캘린더에 넘어가는 '일 단위' 제약이다. 원시 타임스탬프로 비교하면 minDate가
 * 09:00일 때 캘린더가 허용하는 당일 00:00 선택이 범위 밖으로 잘못 걸린다.
 */
export const getBuiltinRangeError = (
  from: Date,
  to: Date,
  timeZone: string,
  minDate?: Date,
  maxDate?: Date,
): Extract<PickerErrorCode, 'reversed' | 'out-of-bounds'> | null => {
  if (from.getTime() > to.getTime()) return 'reversed';
  if (minDate && from.getTime() < getZonedStartOfDay(minDate, timeZone).getTime()) {
    return 'out-of-bounds';
  }
  if (maxDate && to.getTime() > getZonedEndOfDay(maxDate, timeZone).getTime()) {
    return 'out-of-bounds';
  }
  return null;
};
