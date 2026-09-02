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
import { formatInTimeZone, toZonedTime } from 'date-fns-tz';

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
      // 열린 뒤의 위치는 `right-full`/`left-full`/`top-full` 로 정한다. 전환 클래스에 맡기면 안
      // 된다 — @headlessui/react 1 은 `enterTo` 를 전환이 끝난 뒤에도 남겨 뒀지만 2 는 지운다.
      // 그래서 예전 코드(`left-0` + `enterTo` 의 `-translate-x-full`)는 2 로 올린 뒤 패널이
      // 제자리로 돌아와 부모 뒤(`-z-10`)에 가려졌다.
      className={classNames('rich-datetime-picker__more', {
        'border-r-rgba2 top-0 right-full rounded-l border-r': direction === 'left',
        'border-l-rgba2 top-0 left-full rounded-r border-l': direction === 'right',
        'border-t-rgba2 top-full left-0 w-full rounded-br rounded-bl border-t':
          direction === 'bottom',
      })}
      enter="transition-all transform duration-200"
      // 부모와 겹친 자리에서 시작해 제자리로 미끄러져 나온다. 끝 상태는 변형이 없는 기본값이라
      // 전환 클래스가 사라져도 위치가 유지된다.
      enterFrom={classNames('opacity-0', {
        'translate-x-full': direction === 'left',
        '-translate-x-full': direction === 'right',
        '-translate-y-full': direction === 'bottom',
      })}
      enterTo="opacity-100 translate-x-0 translate-y-0"
      leave="transition-all transform duration-200"
      leaveFrom="opacity-100 translate-x-0 translate-y-0"
      leaveTo={classNames('opacity-0', {
        'translate-x-full': direction === 'left',
        '-translate-x-full': direction === 'right',
        '-translate-y-full': direction === 'bottom',
      })}
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
  const now = toZonedTime(new Date(), timeZone);
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
