import React from 'react';
import { Locale, format } from 'date-fns';
import ArrowLeft from '../assets/arrow-left.svg?react';
import ArrowRight from '../assets/arrow-right.svg?react';
import ArrowDoubleLeft from '../assets/arrow-double-left.svg?react';
import ArrowDoubleRight from '../assets/arrow-double-right.svg?react';
import { cn } from '../utils/style';

interface DatePickerHeaderProps {
  date: Date;
  locale: Locale;
  decreaseMonth: () => void;
  increaseMonth: () => void;
  prevMonthButtonDisabled: boolean;
  nextMonthButtonDisabled: boolean;
  decreaseYear: () => void;
  increaseYear: () => void;
  prevYearButtonDisabled: boolean;
  nextYearButtonDisabled: boolean;
  changeYear: (year: number) => void;
  changeMonth: (month: number) => void;
  /** 연도 목록의 범위. `DatePicker`가 캘린더에 넘기는 값과 같은 것을 받는다. */
  minDate: Date;
  maxDate: Date;
  hideCalendarYearButton?: boolean;
}

interface YearSelectProps {
  year: number;
  years: number[];
  onSelect: (year: number) => void;
}

/**
 * 연도 선택. 네이티브 `select`를 쓴다 — 키보드·타입어헤드·모바일 피커가 공짜로 따라오고,
 * 무엇보다 팝업이 문서 흐름 밖이라 포털 래퍼의 `overflow-hidden`에 잘리지 않는다.
 * 트리거 겉모양만 `appearance: none`으로 덮고 캐럿은 직접 그린다.
 * 이동할 연도가 없을 때는 호출부가 이 컴포넌트를 렌더하지 않는다 — 비활성 상태로 흐려 두는 것보다
 * 컨트롤 자체를 감추는 쪽이 "누를 게 없다"는 걸 분명히 보여준다.
 */
const YearSelect = ({ year, years, onSelect }: YearSelectProps) => (
  <span className="rich-datetime-picker__year">
    <select
      data-testid="test-calendar-year-select"
      className="rich-datetime-picker__year-select"
      value={year}
      onChange={(e) => onSelect(Number(e.target.value))}
    >
      {years.map((item) => (
        <option key={item} value={item}>
          {item}
        </option>
      ))}
    </select>
    <span aria-hidden="true" className="rich-datetime-picker__year-caret" />
  </span>
);

interface NavButtonProps {
  onClick: () => void;
  disabled: boolean;
  className?: string;
  testId?: string;
  children: React.ReactNode;
}

const NavButton = ({ onClick, disabled, className, testId, children }: NavButtonProps) => (
  <button
    type="button"
    data-testid={testId}
    onClick={(e) => {
      e.preventDefault();
      e.stopPropagation();
      onClick();
    }}
    disabled={disabled}
    className={cn(
      'rdp:h-5 rdp:w-5 rdp:p-0 rdp:disabled:cursor-not-allowed rdp:disabled:opacity-40',
      className,
    )}
  >
    {children}
  </button>
);

export const DatePickerHeader = ({
  date,
  locale,
  decreaseMonth,
  increaseMonth,
  prevMonthButtonDisabled,
  nextMonthButtonDisabled,
  decreaseYear,
  increaseYear,
  prevYearButtonDisabled,
  nextYearButtonDisabled,
  changeYear,
  changeMonth,
  minDate,
  maxDate,
  hideCalendarYearButton,
}: DatePickerHeaderProps) => {
  const year = date.getFullYear();
  const month = date.getMonth();
  // minDate/maxDate는 소비자가 안 주면 DatePicker가 렌더마다 새로 만든다.
  // Date 객체를 deps에 두면 매번 새 참조라 캐시되지 않으므로 연도 숫자로 비교한다.
  const firstYear = minDate.getFullYear();
  const lastYear = maxDate.getFullYear();
  const years = React.useMemo(
    () => Array.from({ length: Math.max(lastYear - firstYear + 1, 1) }, (_, i) => firstYear + i),
    [firstYear, lastYear],
  );

  // 연도만 바꾸면 경계 연도에서 날짜가 전부 비활성인 달이 열린다
  // (min이 2023-06인데 6월 이전 달을 보고 있던 경우). 그래서 월도 범위 안으로 당긴다.
  // changeYear/changeMonth 둘 다 함수형 setState라 연달아 호출해도 뒤엣것이 앞 결과를 본다.
  // changeMonth는 onMonthChange를 발화시키므로 실제로 옮길 때만 부른다.
  const handleSelectYear = (nextYear: number) => {
    let nextMonth = month;
    if (nextYear === firstYear && month < minDate.getMonth()) nextMonth = minDate.getMonth();
    if (nextYear === lastYear && month > maxDate.getMonth()) nextMonth = maxDate.getMonth();
    changeYear(nextYear);
    if (nextMonth !== month) changeMonth(nextMonth);
  };

  // 이동할 연도가 없으면(대개 min/max 기본값) select를 내보내지 않는다.
  const showYearSelect = !hideCalendarYearButton && years.length > 1;

  return (
    <div className="rdp:mb-2 rdp:flex rdp:justify-between rdp:p-2">
      <div className="rdp:flex rdp:items-center">
        {!hideCalendarYearButton && (
          <NavButton
            testId="test-calendar-year-decrease-button"
            onClick={decreaseYear}
            disabled={prevYearButtonDisabled}
          >
            <ArrowDoubleLeft />
          </NavButton>
        )}
        <NavButton
          onClick={decreaseMonth}
          disabled={prevMonthButtonDisabled}
          className={cn({ 'rdp:ml-5': hideCalendarYearButton })}
        >
          <ArrowLeft />
        </NavButton>
      </div>
      <div className="rich-datetime-picker__header-label">
        <span>{format(date, 'MMM', { locale })}</span>
        {showYearSelect ? (
          <YearSelect year={year} years={years} onSelect={handleSelectYear} />
        ) : (
          <span>{format(date, 'yyyy', { locale })}</span>
        )}
      </div>
      <div className="rdp:flex rdp:items-center">
        <NavButton
          onClick={increaseMonth}
          disabled={nextMonthButtonDisabled}
          className={cn({ 'rdp:mr-5': hideCalendarYearButton })}
        >
          <ArrowRight />
        </NavButton>
        {!hideCalendarYearButton && (
          <NavButton
            testId="test-calendar-year-increase-button"
            onClick={increaseYear}
            disabled={nextYearButtonDisabled}
          >
            <ArrowDoubleRight />
          </NavButton>
        )}
      </div>
    </div>
  );
};
