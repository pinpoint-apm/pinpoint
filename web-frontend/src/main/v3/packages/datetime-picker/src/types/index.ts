export type TimeUnit = 's' | 'm' | 'h' | 'd' | 'w' | 'mo' | 'y';
export type TimeUnitFormat = `${number}${TimeUnit}` | 'today';
// 'zh'는 간체(zh-CN) 별칭으로 하위호환 유지. 대만(번체)은 'zh-TW'.
export type LocaleKey = 'en' | 'ko' | 'ja' | 'zh' | 'zh-CN' | 'zh-TW';
export type DateRange = [Date | null, Date | null];
export type TimePatternKeys =
  | 'second'
  | 'minute'
  | 'hour'
  | 'day'
  | 'week'
  | 'month'
  | 'year'
  | 'yesterday'
  | 'today'
  | 'lastMonth'
  | 'lastYear'
  | 'unixTimestampRange';
export type TimePattern = { [key in TimePatternKeys]: RegExp };
export type TimePatterns = { [key in TimePatternKeys]: RegExp[] };

/**
 * 피커가 표면화하는 오류 사유.
 * - `empty` 입력이 비었거나 범위가 선택되지 않음
 * - `unparseable` 어떤 패턴·포맷에도 맞지 않음
 * - `seamToken`이 날짜 내부 구분자와 충돌해 조각이 3개 이상으로 쪼개짐
 * - `incomplete` 범위의 한쪽이 비어 있음
 * - `reversed` 시작이 종료보다 늦음
 * - `out-of-bounds` minDate/maxDate 밖
 * - `range-invalid` consumer의 `validateDatePickerRange`가 거부함
 */
export type PickerErrorCode =
  | 'empty'
  | 'unparseable'
  | 'seam-conflict'
  | 'incomplete'
  | 'reversed'
  | 'out-of-bounds'
  | 'range-invalid';

/** 오류가 텍스트 입력에서 났는지, Apply/퀵레인지의 범위 검증에서 났는지 */
export type PickerErrorSource = 'input' | 'range';

/**
 * 문구가 아직 해소되지 않은 오류 상태. 훅이 들고 있는 형태다.
 * `message`는 consumer가 직접 지정했을 때만 채워지고, 없으면 로케일 기본 문구가 쓰인다.
 */
export interface PickerErrorState {
  code: PickerErrorCode;
  source: PickerErrorSource;
  message?: string;
}

export interface PickerError {
  code: PickerErrorCode;
  source: PickerErrorSource;
  /** 표시할 문구. consumer가 지정하지 않으면 로케일별 기본 문구가 채워진다. */
  message: string;
}

/**
 * `validateDatePickerRange`의 반환값.
 * `boolean`은 기존 시그니처 그대로 동작하고, 문자열을 반환하면 그 문자열이 오류 문구가 된다.
 */
export type ValidateRangeResult = boolean | string | { valid: boolean; message?: string };
