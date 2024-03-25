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
} from '@/utils/date';
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
        'rdp-left-0 rdp-rounded-l rdp-border-r rdp-border-r-rgba2': direction === 'left',
        'rdp-right-0 rdp-rounded-r rdp-border-l rdp-border-l-rgba2': direction === 'right',
        'rdp-left-0 rdp-w-full rdp-rounded-bl rdp-rounded-br rdp-border-t rdp-border-t-rgba2':
          direction === 'bottom',
      })}
      enter="rdp-transition-all rdp-transform rdp-duration-200"
      enterFrom="rdp-opacity-0 rdp-translate-x-0"
      enterTo={classNames('rdp-opacity-100', {
        '-rdp-translate-x-[100%]': direction === 'left',
        'rdp-translate-x-[100%]': direction === 'right',
        'rdp-translate-y-[100%]': direction === 'bottom',
      })}
      leave="rdp-transition-all rdp-transform rdp-duration-200"
      leaveFrom={classNames('rdp-opacity-100', {
        '-rdp-translate-x-[100%]': direction === 'left',
        'rdp-translate-x-[100%]': direction === 'right',
        '-rdp-translate-y-[100%]': direction === 'bottom',
      })}
      leaveTo="rdp-translate-x-0 rdp-opacity-0"
    >
      {children ? (
        children
      ) : (
        <div className="rdp-flex rdp-flex-col rdp-gap-4 rdp-px-5 rdp-py-3">
          <div className="rdp-text-sm rdp-font-bold">Type custom times like:</div>
          {Object.keys(customTimes).map((key, i) => {
            const times = customTimes?.[key];

            return times?.length > 0 ? (
              <div key={i}>
                <div className="rdp-mb-2 rdp-text-xs">{key}</div>
                <div className="rdp-flex rdp-flex-wrap rdp-gap-1.5">
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
