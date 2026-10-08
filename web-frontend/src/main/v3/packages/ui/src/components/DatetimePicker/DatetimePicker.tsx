import './datetime-picker.css';
import React from 'react';
import { subDays, subYears } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';
import { RxChevronLeft, RxChevronRight, RxPlay, RxTrackNext, RxStop } from 'react-icons/rx';
import {
  RichDatetimePicker,
  RichDatetimePickerProps,
  TimeUnitFormat,
} from '@pinpoint-fe/datetime-picker';
import Marquee from 'react-fast-marquee';

import { SEARCH_PARAMETER_DATE_FORMAT } from '@pinpoint-fe/ui/src/constants';
import {
  getFormattedDateRange,
  getParsedDateRange,
  isValidDateRange,
} from '@pinpoint-fe/ui/src/utils';
import { Button, Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '../ui';
import { cn } from '../../lib';
import { useReactToastifyToast } from '../Toast';
import {
  useDateFormat,
  useLanguage,
  useSearchParameters,
  useTimezone,
} from '@pinpoint-fe/ui/src/hooks';

export type DateState = {
  dates?: {
    from: Date;
    to: Date;
  };
  formattedDates?: {
    from: string;
    to: string;
  };
  isRealtime?: boolean;
};

export type DatetimePickerChangeHandler = (dateState: DateState) => void;

export interface DatetimePickerProps extends Omit<
  RichDatetimePickerProps,
  'onChange' | 'timeUnits'
> {
  className?: string;
  from?: Date | string;
  to?: Date | string;
  isRealtime?: boolean;
  enableRealtimeButton?: boolean;
  maxDateRangeDays?: number;
  outOfDateRangeMessage?: string;
  onChange?: DatetimePickerChangeHandler;
  timeUnits?: string[];
}

/**
 * 트리거에 띄운 라벨("5분 전" 같은 상대 범위 이름)과 **그 라벨을 붙인 범위**.
 *
 * 라벨은 고른 그 순간의 범위에만 맞는 이름이다. 범위가 picker 밖에서 바뀌면(브라우저 뒤로/앞으로
 * 가기, 링크 이동) 라벨은 더 이상 그 범위를 가리키지 않으므로 버린다. 앞으로 가기로 그 범위에
 * 돌아와도 다시 보여주지 않는다 — 범위는 고른 시각에 고정된 값이라, 그 사이 시간이 흘렀으면 더는
 * "5분 전"이 아니다. 범위를 함께 적어 두는 것은 effect가 라벨을 버리기 전 한 렌더 동안에도 남의
 * 범위에 라벨이 붙어 보이지 않게 하기 위해서다.
 *
 * `pending`은 "방금 골랐고 아직 `from`/`to`가 따라오지 않았다"는 뜻이다. 고른 뒤 URL이 바뀌기까지
 * (라우트 로더를 거치므로) 한동안 이전 범위가 들어오는데, 그동안 라벨을 감추면 이전 라벨 → 날짜 →
 * 새 라벨로 깜빡인다. `from`/`to`가 한 번이라도 바뀌면 기다림은 끝난다.
 *
 * 범위는 `SEARCH_PARAMETER_DATE_FORMAT` 문자열로 비교한다. 호출부들이 `formattedDates`를 그대로
 * URL에 싣고 그 값을 `from`/`to`로 돌려주기 때문이다. 시각(timestamp)으로 비교하면 안 된다 —
 * `getParsedDateRange`는 문자열을 브라우저 시간대로 읽어, 설정 시간대가 다른 사용자에게는 같은
 * 범위가 늘 다르게 보인다.
 */
type RangeLabel = { text: string; from: string; to: string; pending: boolean };

const toRangeKey = (date: Date | string | undefined, timezone: string) =>
  typeof date === 'string'
    ? date
    : date
      ? formatInTimeZone(date, timezone, SEARCH_PARAMETER_DATE_FORMAT)
      : undefined;

const genDateState = (from: number | Date, to: number | Date, timezone: string): DateState => {
  const newFrom = new Date(from);
  const newTo = new Date(to);

  return {
    dates: {
      from: newFrom,
      to: newTo,
    },
    formattedDates: {
      from: formatInTimeZone(newFrom, timezone, SEARCH_PARAMETER_DATE_FORMAT),
      to: formatInTimeZone(newTo, timezone, SEARCH_PARAMETER_DATE_FORMAT),
    },
  };
};

export const DatetimePicker = React.memo(
  ({
    className,
    isRealtime,
    enableRealtimeButton,
    from,
    to,
    maxDateRangeDays = 2,
    outOfDateRangeMessage = 'Out of date range.',
    timeUnits,
    onChange,
    ...props
  }: DatetimePickerProps) => {
    const toast = useReactToastifyToast();
    const { application } = useSearchParameters();
    const [language] = useLanguage();
    const [dateFormat] = useDateFormat();
    const [timezone] = useTimezone();
    const [label, setLabel] = React.useState<RangeLabel>();
    const fromKey = toRangeKey(from, timezone);
    const toKey = toRangeKey(to, timezone);
    const input =
      label && (label.pending || (label.from === fromKey && label.to === toKey)) ? label.text : '';
    const parsedDate = getParsedDateRange({ from, to }, isValidDateRange(maxDateRangeDays));
    const parsedFromTimestamp = parsedDate.from.getTime();
    const parsedToTimestamp = parsedDate.to.getTime();
    const gap = parsedDate.to.getTime() - parsedDate.from.getTime();

    React.useEffect(() => {
      setLabel(undefined);
    }, [application?.applicationName, application?.serviceType]);

    React.useEffect(() => {
      // 고른 범위가 도착한 첫 변경만 기다림을 끝내고, 그 뒤의 변경은 라벨을 버린다.
      setLabel((prev) => (prev?.pending ? { ...prev, pending: false } : undefined));
    }, [fromKey, toKey]);

    const handleChange = (dateState: DateState, text = '') => {
      setLabel(
        text && dateState.formattedDates
          ? { text, ...dateState.formattedDates, pending: true }
          : undefined,
      );
      onChange?.(dateState);
    };

    const handleClickPrev = () => {
      handleChange?.(genDateState(parsedFromTimestamp - gap, parsedToTimestamp - gap, timezone));
    };

    const handleClickNext = () => {
      if (parsedToTimestamp + gap > new Date().getTime()) {
        const now = new Date().getTime();
        handleChange?.(genDateState(now - gap, now, timezone));
      } else {
        handleChange?.(genDateState(parsedFromTimestamp + gap, parsedToTimestamp + gap, timezone));
      }
    };

    const handleClickLatest = () => {
      const now = new Date().getTime();

      handleChange?.(genDateState(now - gap, now, timezone));
    };

    const handleRealtime = (realtime: boolean) => () => {
      handleChange?.({ isRealtime: realtime });
    };

    return (
      <>
        <div className={cn('flex h-8 gap-1 items-center', className)}>
          {isRealtime ? (
            <div className="flex items-center h-full border rounded w-104 border-input">
              <Marquee speed={80} className="text-sm italic opacity-40">
                REAL TIME MONITORING
              </Marquee>
            </div>
          ) : (
            <RichDatetimePicker
              dateFormat={dateFormat}
              disable={isRealtime}
              className="w-104"
              seamToken="~"
              localeKey={language}
              startDate={parsedDate.from}
              endDate={parsedDate.to}
              minDate={subYears(new Date(), 5)}
              displayedInput={input}
              onChange={(dateRange, text = '') => {
                if (dateRange[0] && dateRange[1]) {
                  const isWithinMaxRange = isValidDateRange(maxDateRangeDays)({
                    from: dateRange[0],
                    to: dateRange[1],
                  });
                  if (isWithinMaxRange) {
                    handleChange?.(genDateState(dateRange[0], dateRange[1], timezone), text);
                  } else {
                    toast.warn(outOfDateRangeMessage);
                    const prarsedPrevDate = getParsedDateRange({ from, to }, () => true);
                    const formattedDateRange = getFormattedDateRange({
                      from: prarsedPrevDate.from,
                      to: prarsedPrevDate.to,
                    });
                    // 범위는 그대로이므로 지금 범위에 붙인다.
                    setLabel({
                      text: `${formattedDateRange.from} ~ ${formattedDateRange.to}`,
                      from: fromKey ?? '',
                      to: toKey ?? '',
                      pending: false,
                    });
                  }
                }
              }}
              customTimes={{
                Relative: ['45m', '12hours', '1d', '2days', 'yesterday', 'today'],
              }}
              validateDatePickerRange={([from, to]) => {
                if (from && to) {
                  if (subDays(to, maxDateRangeDays) > from) {
                    // 반환한 문구가 패널 하단 오류 영역에 표시된다.
                    return outOfDateRangeMessage;
                  } else {
                    return true;
                  }
                }
                return false;
              }}
              timeUnits={timeUnits as TimeUnitFormat[]}
              {...props}
            />
          )}

          <div className="flex items-center h-full">
            <TooltipProvider>
              <Tooltip>
                <TooltipTrigger disabled={isRealtime} asChild>
                  <Button
                    className="h-8 px-2 py-1 rounded-e-none"
                    variant="outline"
                    onClick={handleClickPrev}
                  >
                    <RxChevronLeft />
                  </Button>
                </TooltipTrigger>
                <TooltipContent>At the previous range</TooltipContent>
              </Tooltip>
              {enableRealtimeButton &&
                (isRealtime ? (
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <Button
                        variant="outline"
                        className="h-8 px-2 py-1 border-l-0 rounded-none"
                        onClick={handleRealtime(false)}
                      >
                        <RxStop />
                      </Button>
                    </TooltipTrigger>
                    <TooltipContent>Stop Realtime</TooltipContent>
                  </Tooltip>
                ) : (
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <Button
                        className="h-8 px-2 py-1 border-l-0 rounded-none"
                        variant="outline"
                        onClick={handleRealtime(true)}
                      >
                        <RxPlay />
                      </Button>
                    </TooltipTrigger>
                    <TooltipContent>Real-time mode</TooltipContent>
                  </Tooltip>
                ))}
              <Tooltip>
                <TooltipTrigger disabled={isRealtime} asChild>
                  <Button
                    className="h-8 px-2 py-1 border-l-0 rounded-none"
                    variant="outline"
                    onClick={handleClickNext}
                  >
                    <RxChevronRight />
                  </Button>
                </TooltipTrigger>
                <TooltipContent>At the next range</TooltipContent>
              </Tooltip>
              <Tooltip>
                <TooltipTrigger disabled={isRealtime} asChild>
                  <Button
                    className="h-8 px-2 py-1 border-l-0 rounded-s-none"
                    variant="outline"
                    onClick={handleClickLatest}
                  >
                    <RxTrackNext />
                  </Button>
                </TooltipTrigger>
                <TooltipContent>At the latest range</TooltipContent>
              </Tooltip>
            </TooltipProvider>
          </div>
        </div>
      </>
    );
  },
);
