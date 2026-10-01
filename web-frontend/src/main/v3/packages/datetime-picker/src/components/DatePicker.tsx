import 'react-datepicker/dist/react-datepicker.css';
import './datepicker.scss';

import React from 'react';
import ReactDatePicker from 'react-datepicker';
import {
  addDays,
  isAfter,
  isBefore,
  isSameDay,
  isWithinInterval,
  startOfDay,
  subMonths,
} from 'date-fns';
import { DateRange } from '../types';
import AppContext from './context/appContext';
import { getZonedEndOfDay, getZonedStartOfDay } from '../utils/date';
import { toZonedTime, fromZonedTime } from 'date-fns-tz';
import { DatePickerProps } from './types';
import { DatePickerHeader } from './DatePickerHeader';

const getZonedCalendarDate = (date: Date | null | undefined, timeZone: string) => {
  if (date) {
    return toZonedTime(date, timeZone);
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
  const now = toZonedTime(Date.now(), timeZone);
  const min = minDate || subMonths(now, 1);
  const max = maxDate || addDays(now, 1);
  const datePickerRef = React.useCallback((ref: ReactDatePicker) => {
    if (!ref) {
      onUnmount?.();
    }
  }, []);
  const to = getZonedCalendarDate(endDate, timeZone) || null;

  // react-datepicker는 선택 중 하이라이트(--in-selecting-range / --selecting-range-end)를
  // 이번 달 날짜에만 준다 — isInSelectingRange가 outside-month를 명시적으로 제외한다.
  // 그래서 다음 달 날짜를 가리키면 같은 줄의 다음 달 날짜만 띠가 비어 보인다.
  // 내부 selectingDate와 같은 값을 onDayMouseEnter/onMonthMouseLeave로 따라가서 직접 칠한다.
  const [hoverDate, setHoverDate] = React.useState<Date | null>(null);
  // 비교는 react-datepicker의 isDayInRange와 같은 로컬 일 단위로 한다 — 이번 달 날짜의
  // 하이라이트를 그리는 쪽이 그 규칙이라, 여기서 다르게 재면 달 경계에서 띠가 어긋난다.
  const selectingEnd =
    startDate && !endDate && hoverDate && !isBefore(startOfDay(hoverDate), startOfDay(startDate))
      ? startOfDay(hoverDate)
      : null;
  const isDisabledDay = (day: Date) =>
    isBefore(day, startOfDay(min)) || isAfter(day, startOfDay(max));

  return (
    <ReactDatePicker
      inline
      selectsRange
      showDisabledMonthNavigation
      // react-datepicker의 preSelection(키보드 커서)은 stock 스타일시트에서 옅은 파란
      // 박스(#bad9f1 `--keyboard-selected`)로 칠해지는데, 이 커서는 선택 범위와 따로 논다:
      // inline + shouldCloseOnSelect(기본 true)에서는 날짜를 클릭해도 갱신되지 않고,
      // props의 startDate 변화도 "월/연이 달라질 때만" 재동기화되며, 월 이동만 해도
      // 그 달의 같은 날짜로 옮겨간다. 그래서 범위 밖 아무 날짜에나 파란 박스가 남아
      // "이전 시작일이 아직 선택된 것처럼" 보인다 (NELO-2279).
      // 이 캘린더는 인라인 패널의 마우스 조작용이고 키보드 커서를 노출하는 디자인이
      // 아니므로, 커서 자체를 끈다 — Day/Week/Month/Year 전부에서 keyboard-selected
      // 클래스가 사라진다. 키보드 입력은 트리거의 텍스트 입력이 계속 담당한다.
      disabledKeyboardNavigation
      className={className}
      locale={locale}
      ref={datePickerRef}
      selected={startDate}
      startDate={startDate}
      endDate={to}
      minDate={min}
      maxDate={max}
      weekDayClassName={() => 'rich-datetime-picker__day-name'}
      onDayMouseEnter={setHoverDate}
      onMonthMouseLeave={() => setHoverDate(null)}
      dayClassName={(date) => {
        let dayClass = 'rich-datetime-picker__day';
        if (startDate && endDate) {
          dayClass = isWithinInterval(fromZonedTime(date, timeZone), {
            start: startDate,
            end: endDate,
          })
            ? `${dayClass} __day--in-range`
            : dayClass;
        }
        if (selectingEnd && startDate) {
          const day = startOfDay(date);
          // 비활성 날짜는 react-datepicker도 칠하지 않는다(selectsDisabledDaysInRange 기본 false).
          const inSelecting =
            !isDisabledDay(day) &&
            !isBefore(day, startOfDay(startDate)) &&
            !isAfter(day, selectingEnd);
          if (inSelecting) dayClass += ' __day--in-selecting-range';
          if (inSelecting && isSameDay(day, selectingEnd))
            dayClass += ' __day--selecting-range-end';
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

        onChange?.([
          start instanceof Date ? getZonedStartOfDay(start, timeZone) : start,
          end instanceof Date ? getZonedEndOfDay(end, timeZone) : end,
        ]);
      }}
      renderCustomHeader={(props) => (
        <DatePickerHeader
          {...props}
          locale={locale}
          minDate={min}
          maxDate={max}
          hideCalendarYearButton={hideCalendarYearButton}
        />
      )}
    />
  );
};
