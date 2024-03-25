import './datetime-picker.css';
import '@pinpoint-fe/datetime-picker/dist/rich-datetime-picker.css';
import React from 'react';
import { subDays, format, subYears } from 'date-fns';
import { RxChevronLeft, RxChevronRight, RxPlay, RxTrackNext, RxStop } from 'react-icons/rx';
import { RichDatetimePicker, RichDatetimePickerProps } from '@pinpoint-fe/datetime-picker';
import Marquee from 'react-fast-marquee';

import { DATE_FORMAT } from '@pinpoint-fe/constants';
import { getParsedDateRange, isValidDateRange } from '@pinpoint-fe/utils';
import { Button, Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '../ui';
import { cn } from '../../lib';
import { toast } from '../Toast';
import { useLanguage } from '@pinpoint-fe/hooks';

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

export interface DatetimePickerProps extends Omit<RichDatetimePickerProps, 'onChange'> {
  className?: string;
  from?: Date | string;
  to?: Date | string;
  isRealtime?: boolean;
  enableRealtimeButton?: boolean;
  maxDateRangeDays?: number;
  outOfDateRangeMessage?: string;
  onChange?: DatetimePickerChangeHandler;
}

const genDateState = (from: number | Date, to: number | Date): DateState => {
  const newFrom = new Date(from);
  const newTo = new Date(to);

  return {
    dates: {
      from: newFrom,
      to: newTo,
    },
    formattedDates: {
      from: format(newFrom, DATE_FORMAT),
      to: format(newTo, DATE_FORMAT),
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
    onChange,
    ...props
  }: DatetimePickerProps) => {
    const [language] = useLanguage();
    const [input, setInput] = React.useState('');
    const parsedDate = getParsedDateRange({ from, to }, isValidDateRange(maxDateRangeDays));
    const parsedFromTimestamp = parsedDate.from.getTime();
    const parsedToTimestamp = parsedDate.to.getTime();
    const gap = parsedDate.to.getTime() - parsedDate.from.getTime();

    const handleChange = (dateState: DateState, text = '') => {
      setInput(text);
      onChange?.(dateState);
    };

    const handleClickPrev = () => {
      handleChange?.(genDateState(parsedFromTimestamp - gap, parsedToTimestamp - gap));
    };

    const handleClickNext = () => {
      if (parsedToTimestamp + gap > new Date().getTime()) {
        const now = new Date().getTime();
        handleChange?.(genDateState(now - gap, now));
      } else {
        handleChange?.(genDateState(parsedFromTimestamp + gap, parsedToTimestamp + gap));
      }
    };

    const handleClickLatest = () => {
      const now = new Date().getTime();

      handleChange?.(genDateState(now - gap, now));
    };

    const handleRealtime = (realtime: boolean) => () => {
      handleChange?.({ isRealtime: realtime });
    };

    return (
      <>
        <div className={cn('flex h-8 gap-1 items-center', className)}>
          {isRealtime ? (
            <div className="flex items-center h-full border rounded w-90 border-input">
              <Marquee speed={80} className="text-sm italic opacity-40">
                REAL TIME MONITORING
              </Marquee>
            </div>
          ) : (
            <RichDatetimePicker
              disable={isRealtime}
              className="w-90"
              seamToken="~"
              localeKey={language}
              startDate={parsedDate.from}
              endDate={parsedDate.to}
              minDate={subYears(new Date(), 5)}
              displayedInput={input}
              onChange={(dateRange, text = '') => {
                if (dateRange[0] && dateRange[1]) {
                  handleChange?.(genDateState(dateRange[0], dateRange[1]), text);
                }
              }}
              customTimes={{
                Relative: ['45m', '12hours', '1d', '2days', 'yesterday', 'today'],
              }}
              validateDatePickerRange={([from, to]) => {
                if (from && to) {
                  if (subDays(to, maxDateRangeDays) > from) {
                    toast.warn(outOfDateRangeMessage);
                    return false;
                  } else {
                    return true;
                  }
                }
                return false;
              }}
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
                    <TooltipTrigger>
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
                    <TooltipTrigger>
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
