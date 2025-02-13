import 'react-datepicker/dist/react-datepicker.css';
import './datepicker.scss';

import React from 'react';
import ReactDatePicker from 'react-datepicker';
import { ReactComponent as ArrowLeft } from '../assets/arrow-left.svg';
import { ReactComponent as ArrowRight } from '../assets/arrow-right.svg';
import { ReactComponent as ArrowDoubleLeft } from '../assets/arrow-double-left.svg';
import { ReactComponent as ArrowDoubleRight } from '../assets/arrow-double-right.svg';
import { RichDatetimePickerProps } from './RichDatetimePicker';
import { Locale, addDays, format, isWithinInterval, subMonths } from 'date-fns';
import { DateRange } from '../types';
import classNames from 'classnames';
import AppContext from './context/appContext';
import { getZonedEndOfDay, getZonedStartOfDay } from '../utils/date';
import { utcToZonedTime, zonedTimeToUtc } from 'date-fns-tz';

export interface DatePickerProps
  extends Pick<
    RichDatetimePickerProps,
    'startDate' | 'endDate' | 'maxDate' | 'minDate' | 'className'
  > {
  locale: Locale;
  hideCalendarYearButton?: boolean;
  onUnmount?: () => void;
  onChange?: (dates: DateRange) => void;
}

const getZonedCalendarDate = (date: Date | null | undefined, timeZone: string) => {
  if (date) {
    return utcToZonedTime(date, timeZone);
  }
  return null;
};

export const DatePicker = ({
  className,
  locale,
  startDate,
  endDate,
  minDate,
  maxDate,
  hideCalendarYearButton,
  onUnmount,
  onChange,
}: DatePickerProps) => {
  const {
    appContext: { timeZone },
  } = React.useContext(AppContext);
  const now = utcToZonedTime(Date.now(), timeZone);
  const min = minDate || subMonths(now, 1);
  const max = maxDate || addDays(now, 1);
  const datePickerRef = React.useCallback((ref: ReactDatePicker) => {
    if (!ref) {
      onUnmount?.();
    }
  }, []);
  const to = getZonedCalendarDate(endDate, timeZone) || null;

  return (
    <ReactDatePicker
      inline
      selectsRange
      showDisabledMonthNavigation
      className={className}
      locale={locale}
      ref={datePickerRef}
      selected={startDate}
      startDate={startDate}
      endDate={to}
      minDate={min}
      maxDate={max}
      weekDayClassName={() => 'rich-datetime-picker__day-name'}
      dayClassName={(date) => {
        let dayClass = 'rich-datetime-picker__day';
        if (startDate && endDate) {
          dayClass = isWithinInterval(zonedTimeToUtc(date, timeZone), {
            start: startDate,
            end: endDate,
          })
            ? `${dayClass} __day--in-range`
            : dayClass;
        }

        return dayClass;
      }}
      formatWeekDay={(nameOfDay) => {
        if (locale.code === 'ko') {
          return nameOfDay.substring(0, 1);
        }
        return nameOfDay.substring(0, 3);
      }}
      calendarClassName={'rich-datetime-picker__date-picker'}
      onChange={(dates: DateRange) => {
        const [start, end] = dates;
        let resultDate: DateRange = [...dates];

        if (start instanceof Date) {
          resultDate = [getZonedStartOfDay(start, timeZone), end];
        }
        if (end instanceof Date) {
          resultDate = [start, getZonedEndOfDay(end, timeZone)];
        }

        onChange?.(resultDate);
      }}
      renderCustomHeader={({
        date,
        decreaseMonth,
        increaseMonth,
        prevMonthButtonDisabled,
        nextMonthButtonDisabled,
        decreaseYear,
        increaseYear,
        prevYearButtonDisabled,
        nextYearButtonDisabled,
      }) => (
        <div className="mb-2 flex justify-between p-2">
          <div className="flex items-center">
            {!hideCalendarYearButton && (
              <button
                data-testid="test-calendar-year-button"
                onClick={decreaseYear}
                disabled={prevYearButtonDisabled}
                className="h-5 w-5 p-0 disabled:cursor-not-allowed disabled:opacity-40"
              >
                <ArrowDoubleLeft />
              </button>
            )}
            <button
              onClick={decreaseMonth}
              disabled={prevMonthButtonDisabled}
              className={classNames('h-5 w-5 p-0 disabled:cursor-not-allowed disabled:opacity-40', {
                'ml-5': hideCalendarYearButton,
              })}
            >
              <ArrowLeft />
            </button>
          </div>
          {format(date, 'MMM yyyy', { locale })}
          <div className="flex items-center">
            <button
              onClick={increaseMonth}
              disabled={nextMonthButtonDisabled}
              className={classNames('h-5 w-5 p-0 disabled:cursor-not-allowed disabled:opacity-40', {
                'mr-5': hideCalendarYearButton,
              })}
            >
              <ArrowRight />
            </button>
            {!hideCalendarYearButton && (
              <button
                data-testid="test-calendar-year-button"
                onClick={increaseYear}
                disabled={nextYearButtonDisabled}
                className="h-5 w-5 p-0 disabled:cursor-not-allowed disabled:opacity-40"
              >
                <ArrowDoubleRight />
              </button>
            )}
          </div>
        </div>
      )}
    />
  );
};
