import React, { HTMLAttributes } from 'react';
import { Locale } from 'date-fns';
import { convertToMilliseconds, getFormattedTimeUnit, getQuickRangeLabel } from '../utils/date';
import { getLocaleKey } from '../utils/locale';
import { getUiText } from '../utils/uiText';
import HelpIcon from '../assets/help.svg?react';
import { DateRange, PickerError, TimeUnitFormat } from '../types';
import {
  CustomTimeView,
  CustomTimeViewProps,
  getDefaultCustomTimes,
  mergeCustomTimes,
} from './CustomTimeView';
import { DatePicker } from './DatePicker';
import { CopyRangeFormatter, DatePickerProps } from './types';
import { TimeBadge } from './TimeBadge';
import { Tooltip } from './Tooltip';
import AppContext from './context/appContext';
import { cn } from '../utils/style';

export interface DatePanelItemProps {
  timeUnit: string;
  timeUnitToMilliseconds: number;
  formattedTimeUnit: string;
  /** 이 퀵레인지를 즉시 커밋하고 패널을 닫는다 */
  select: () => void;
  /** 커밋 없이 패널을 닫는다 */
  close: () => void;
}

export interface DatePanelProps extends Pick<HTMLAttributes<HTMLDivElement>, 'style'> {
  locale: Locale;
  dateFormat?: string;
  open?: boolean;
  className?: string;
  children?: ((props: DatePanelItemProps[]) => React.ReactNode) | React.ReactNode;
  /**
   * Apply 버튼 위 슬롯. 정적 노드면 기본 오류 문구와 함께 표시되고,
   * 함수면 오류 표현을 통째로 넘겨받는다(기본 문구는 렌더하지 않는다).
   */
  panelFooter?: React.ReactNode | ((state: { error: PickerError | null }) => React.ReactNode);
  /** 내부 배선 — 현재 표시할 오류 */
  error?: PickerError | null;
  /** 내부 배선 — 입력의 aria-describedby가 가리킬 메시지 노드 id */
  errorId?: string;
  datePickerClassName?: string;
  /** 캘린더/시간배지에 표시되는 pending(draft) 값 */
  startDate?: Date | null;
  endDate?: Date | null;
  minDate?: Date;
  maxDate?: Date;
  timeUnits?: TimeUnitFormat[];
  customTimeView?: CustomTimeViewProps['children'];
  customTimes?: CustomTimeViewProps['customTimes'];
  hideCalendarYearButton?: DatePickerProps['hideCalendarYearButton'];
  /** 캘린더 상단 HH:mm:ss 시간 배지 행을 숨긴다. 일(day) 단위 범위만 쓰는 화면용 (NELO-2305) */
  hideTimeBadges?: boolean;
  /**
   * @deprecated 슬라이드아웃 UI가 `?` 툴팁으로 대체되어 더 이상 동작하지 않습니다.
   * 하위 호환을 위해 타입만 유지되며 값은 무시됩니다.
   */
  customTimeViewSlideDirection?: CustomTimeViewProps['direction'];
  /**
   * @deprecated duration 태그가 제거되어 더 이상 동작하지 않습니다.
   * 하위 호환을 위해 타입만 유지되며 값은 무시됩니다.
   */
  formatTag?: (ms: number) => string;
  /** 퀵레인지/커스텀타임 → 즉시 커밋 */
  onChange?: (params: DateRange, text: string, timeUnit?: string) => void;
  /** 캘린더 선택 → draft 갱신 */
  onChangeDatePicker?: (params: DateRange) => void;
  /** 시간 배지 편집 → draft 갱신 */
  onTimeBadgeChange?: (which: 'from' | 'to', next: Date) => void;
  /** Apply 버튼 */
  onApply?: () => void;
  /** 커밋 없이 패널 닫기 (렌더프롭 아이템의 close에 사용) */
  onClose?: () => void;
  /** Copy range 버튼이 복사할 문자열 포맷터. 기본은 {"from":ISO,"to":ISO} JSON */
  formatCopyRange?: CopyRangeFormatter;
}

const defaultFormatCopyRange: CopyRangeFormatter = ({ from, to }) =>
  JSON.stringify({ from: from.toISOString(), to: to.toISOString() });

export const DatePanel = ({
  locale,
  dateFormat,
  style,
  children,
  panelFooter,
  error,
  errorId,
  className = '',
  datePickerClassName,
  startDate,
  endDate,
  minDate,
  maxDate,
  timeUnits = ['10m', '30m', '60m', '3h', '24h', '7d', '14d', '28d'],
  customTimeView,
  customTimes = {},
  onChange,
  onChangeDatePicker,
  onTimeBadgeChange,
  onApply,
  onClose,
  formatCopyRange,
  hideCalendarYearButton,
  hideTimeBadges,
}: DatePanelProps) => {
  const {
    appContext: { seamToken, timeZone },
  } = React.useContext(AppContext);
  const defaultCustomTimes = mergeCustomTimes(
    getDefaultCustomTimes(locale, seamToken, timeZone),
    customTimes,
  );

  const localeKey = getLocaleKey(locale);
  const t = getUiText(localeKey);

  // JSX 안 IIFE 금지 — 분기는 렌더 전에 계산한다.
  const isFooterRenderProp = typeof panelFooter === 'function';
  const footerNode = isFooterRenderProp ? panelFooter({ error: error ?? null }) : panelFooter;

  const handleClickTimeUnit = (ms: number, text: string, timeUnit: string) => {
    const now = Date.now();
    onChange?.([new Date(now - ms), new Date(now)], text, timeUnit);
  };

  const [copied, setCopied] = React.useState(false);
  const copiedTimer = React.useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
  React.useEffect(() => () => clearTimeout(copiedTimer.current), []);

  const handleCopy = () => {
    if (startDate && endDate) {
      const format = formatCopyRange ?? defaultFormatCopyRange;
      navigator.clipboard?.writeText(format({ from: startDate, to: endDate }));
      setCopied(true);
      clearTimeout(copiedTimer.current);
      copiedTimer.current = setTimeout(() => setCopied(false), 1000);
    }
  };

  const quickItems = timeUnits.map((timeUnit) => {
    const milliseconds = convertToMilliseconds(timeUnit, timeZone);
    const text = getQuickRangeLabel(timeUnit, localeKey);
    return { timeUnit, milliseconds, text };
  });

  return (
    <div style={style} className={cn('rich-datetime-picker__panel', className)}>
      {typeof children === 'function' ? (
        children(
          quickItems.map(({ timeUnit, milliseconds, text }) => ({
            timeUnit,
            timeUnitToMilliseconds: milliseconds,
            formattedTimeUnit: getFormattedTimeUnit(milliseconds),
            select: () => handleClickTimeUnit(milliseconds, text, timeUnit),
            close: () => onClose?.(),
          })),
        )
      ) : children ? (
        children
      ) : (
        <div className="rich-datetime-picker__layout">
          <div className="rich-datetime-picker__quick-col">
            <Tooltip
              placement="right-start"
              ariaLabel="custom time input"
              className="rdp:p-0"
              content={
                <CustomTimeView
                  show
                  locale={locale}
                  direction="none"
                  dateFormat={dateFormat}
                  customTimes={defaultCustomTimes}
                  onClickTimeString={(dateRange, timeString) => {
                    onChange?.(dateRange, timeString);
                  }}
                >
                  {customTimeView}
                </CustomTimeView>
              }
            >
              <button type="button" className="rich-datetime-picker__custom-trigger">
                <HelpIcon className="rich-datetime-picker__custom-icon" />
                {t.customTimesTrigger}
              </button>
            </Tooltip>

            <ul className="rich-datetime-picker__quick-list">
              {quickItems.map(({ timeUnit, milliseconds, text }) => (
                <li key={timeUnit}>
                  <button
                    type="button"
                    className="rich-datetime-picker__quick-item"
                    onClick={() => handleClickTimeUnit(milliseconds, text, timeUnit)}
                  >
                    {text}
                  </button>
                </li>
              ))}
            </ul>
          </div>

          <div className="rich-datetime-picker__main">
            {!hideTimeBadges && (
              <div className="rich-datetime-picker__badges">
                <TimeBadge
                  label="from"
                  value={startDate}
                  timeZone={timeZone}
                  onChange={(next) => onTimeBadgeChange?.('from', next)}
                />
                <TimeBadge
                  label="to"
                  value={endDate}
                  timeZone={timeZone}
                  onChange={(next) => onTimeBadgeChange?.('to', next)}
                />
              </div>
            )}

            <DatePicker
              className={datePickerClassName}
              locale={locale}
              startDate={startDate}
              endDate={endDate}
              minDate={minDate}
              maxDate={maxDate}
              onChange={onChangeDatePicker}
              hideCalendarYearButton={hideCalendarYearButton}
            />

            <div className="rich-datetime-picker__footer">
              {/* 문구 유무와 무관하게 항상 마운트한다 — 자리를 예약해 Apply가 밀리지 않고,
                  라이브 리전이 나중에 삽입될 때 일부 스크린리더가 첫 변경을 놓치는 것도 막는다. */}
              <div
                id={errorId}
                className="rich-datetime-picker__error"
                role="status"
                aria-live="polite"
                aria-atomic="true"
                title={error?.message}
              >
                {isFooterRenderProp ? footerNode : error?.message}
              </div>
              {!isFooterRenderProp && footerNode ? (
                <div className="rich-datetime-picker__footer-slot">{footerNode}</div>
              ) : null}
            </div>

            <div className="rich-datetime-picker__actions">
              <button
                type="button"
                className={cn('rich-datetime-picker__copy', {
                  'rich-datetime-picker__copy--copied': copied,
                })}
                onClick={handleCopy}
              >
                {copied ? t.copied : t.copyRange}
              </button>
              <button type="button" className="rich-datetime-picker__apply" onClick={onApply}>
                {t.apply}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
