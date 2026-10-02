import React from 'react';
import { Locale } from 'date-fns';
import { DateRange, LocaleKey, ValidateRangeResult } from '../types';
import { DatePanelProps } from './DatePanel';

/** Copy range 버튼이 클립보드에 복사할 문자열을 만드는 포맷터 */
export type CopyRangeFormatter = (range: { from: Date; to: Date }) => string;

export interface RichDatetimePickerProps extends Omit<
  DatePanelProps,
  | 'locale'
  | 'open'
  | 'className'
  | 'onChangeDatePicker'
  | 'onTimeBadgeChange'
  | 'onApply'
  // 내부 배선 전용 — 공개 prop이 되면 안 된다
  | 'error'
  | 'errorId'
> {
  disable?: boolean;
  startDate?: Date | null;
  endDate?: Date | null;
  minDate?: Date;
  maxDate?: Date;
  className?: string;
  inputClassName?: string;
  triggerClassName?: string;
  panelClassName?: string;
  /**
   * 트리거 좌측 아이콘을 커스텀 렌더한다. 인자로 기본 시계 아이콘이 전달돼
   * 감싸거나(패딩·래퍼 등) 교체·제거할 수 있다.
   * - 미지정: 기본 시계 아이콘 노출
   * - 지정: 반환값을 그대로 노출 (`() => null`이면 아이콘 숨김)
   *
   * @example renderIcon={(icon) => <span style={{ paddingRight: 8 }}>{icon}</span>}
   */
  renderIcon?: (defaultIcon: React.ReactNode) => React.ReactNode;
  localeKey?: LocaleKey;
  timeZone?: string;
  seamToken?: string;
  defaultOpen?: boolean;
  displayedInput?: string;
  getPanelContainer?: () => HTMLElement | null;
  /**
   * Apply·퀵레인지 확정 전 범위를 검증한다.
   * `false`를 반환하면 로케일 기본 문구가, 문자열을 반환하면 그 문자열이 패널에 표시된다.
   */
  validateDatePickerRange?: (params: DateRange) => ValidateRangeResult;
}

export interface DatePickerProps extends Pick<
  RichDatetimePickerProps,
  'startDate' | 'endDate' | 'maxDate' | 'minDate' | 'className'
> {
  locale: Locale;
  hideCalendarYearButton?: boolean;
  onUnmount?: () => void;
  onChange?: (dates: DateRange) => void;
}
