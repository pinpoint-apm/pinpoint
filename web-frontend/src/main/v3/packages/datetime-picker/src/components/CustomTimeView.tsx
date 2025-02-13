import React from 'react';
import { Locale, subMinutes } from 'date-fns';
import classNames from 'classnames';
import { addDays, addHours } from 'date-fns';
import { DateRange } from '..';
import {
  getZonedEndOfDay,
  getZonedStartOfDay,
  getZonedStartOfMonth,
  parseTimeString,
} from '../utils/date';
import { Transition } from '@headlessui/react';
import AppContext from './context/appContext';
import { formatInTimeZone, utcToZonedTime } from 'date-fns-tz';

export interface CustomTimeViewProps {
  show: boolean;
  locale: Locale;
  direction: 'right' | 'left' | 'bottom';
  dateFormat?: string;
  customTimes: {
    [key: string]: string[];
  };
  children?: React.ReactNode;
  onClickTimeString?: (dateRange: DateRange, value: string) => void;
}

export const CustomTimeView = ({
  show,
  locale,
  direction,
  dateFormat,
  customTimes,
  children,
  onClickTimeString,
}: CustomTimeViewProps) => {
  const {
    appContext: { seamToken, timeZone },
  } = React.useContext(AppContext);
  const handlecClickDateString = (dateString: string) => {
    onClickTimeString?.(
      parseTimeString(dateString, locale, { dateFormat, seamToken, timeZone }),
      dateString,
    );
  };

  return (
    <Transition
      as="div"
      show={show}
      className={classNames('rich-datetime-picker__more', {
        'left-0 rounded-l border-r border-r-rgba2': direction === 'left',
        'right-0 rounded-r border-l border-l-rgba2': direction === 'right',
        'left-0 w-full rounded-bl rounded-br border-t border-t-rgba2': direction === 'bottom',
      })}
      enter="transition-all transform duration-200"
      enterFrom="opacity-0 translate-x-0"
      enterTo={classNames('opacity-100', {
        '-translate-x-[100%]': direction === 'left',
        'translate-x-[100%]': direction === 'right',
        'translate-y-[100%]': direction === 'bottom',
      })}
      leave="transition-all transform duration-200"
      leaveFrom={classNames('opacity-100', {
        '-translate-x-[100%]': direction === 'left',
        'translate-x-[100%]': direction === 'right',
        '-translate-y-[100%]': direction === 'bottom',
      })}
      leaveTo="translate-x-0 opacity-0"
    >
      {children ? (
        children
      ) : (
        <div className="flex flex-col gap-4 px-5 py-3">
          <div className="text-sm font-bold">Type custom times like:</div>
          {Object.keys(customTimes).map((key, i) => {
            const times = customTimes?.[key];

            return times?.length > 0 ? (
              <div key={i}>
                <div className="mb-2 text-xs">{key}</div>
                <div className="flex flex-wrap gap-1.5">
                  {times.map((time, i) => {
                    return (
                      <label
                        key={i}
                        className="rich-datetime-picker__more-label"
                        onClick={() => handlecClickDateString(time)}
                      >
                        {time}
                      </label>
                    );
                  })}
                </div>
              </div>
            ) : null;
          })}
        </div>
      )}
    </Transition>
  );
};

export const getDefaultCustomTimes = (
  locale: Locale,
  seamToken: string,
  timeZone: string,
): CustomTimeViewProps['customTimes'] => {
  const now = utcToZonedTime(new Date(), timeZone);
  const startDayOfMonth = getZonedStartOfMonth(now, timeZone);
  const nextDayOfStartOfMonth = getZonedEndOfDay(
    addDays(getZonedStartOfMonth(now, timeZone), 1),
    timeZone,
  );
  const baseHour = addHours(getZonedStartOfDay(now, timeZone), 9);

  return {
    Relative: ['45m', '12hours', '10d', '2 weeks', 'last month', 'yesterday', 'today'],
    Fixed: [
      formatInTimeZone(startDayOfMonth, timeZone, 'MMM d', { locale }),
      `${formatInTimeZone(startDayOfMonth, timeZone, 'MMM d', {
        locale,
      })} ${seamToken} ${formatInTimeZone(nextDayOfStartOfMonth, timeZone, 'MMM d', {
        locale,
      })}`,
      formatInTimeZone(startDayOfMonth, timeZone, 'M/d', { locale }),
      `${formatInTimeZone(startDayOfMonth, timeZone, 'M/d', {
        locale,
      })} ${seamToken} ${formatInTimeZone(nextDayOfStartOfMonth, timeZone, 'M/d', {
        locale,
      })}`,
      `${formatInTimeZone(baseHour, timeZone, 'hh:mm a', {
        locale,
      })} ${seamToken} ${formatInTimeZone(addHours(baseHour, 8), timeZone, 'hh:mm a', {
        locale,
      })}`,
    ],
    'Unix timestamps': [`${subMinutes(now, 5).getTime()} ${seamToken} ${now.getTime()}`],
  };
};
