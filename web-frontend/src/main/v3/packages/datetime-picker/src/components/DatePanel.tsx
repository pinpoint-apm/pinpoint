import React, { HTMLAttributes } from 'react';
import classNames from 'classnames';
import { Locale, formatDistance } from 'date-fns';
import { convertToMilliseconds, getFormattedTimeUnit } from '../utils/date';
import { ReactComponent as CalendarIcon } from '../assets/calendar.svg';
import { ReactComponent as MoreIcon } from '../assets/more.svg';
import { DateRange } from '..';
import { TimeUnitFormat } from '..';
import { CustomTimeView, CustomTimeViewProps, getDefaultCustomTimes } from './CustomTimeView';
import { DatePicker, DatePickerProps } from './DatePicker';
import AppContext from './context/appContext';

export interface DatePanelItemProps {
  timeUnit: string;
  timeUnitToMilliseconds: number;
  formattedTimeUnit: string;
  close: () => void;
}

export interface DatePanelProps extends Pick<HTMLAttributes<HTMLDivElement>, 'style'> {
  locale: Locale;
  dateFormat?: string;
  open?: boolean;
  className?: string;
  children?: ((props: DatePanelItemProps[]) => React.ReactNode) | React.ReactNode;
  datePickerClassName?: string;
  startDate?: Date | null;
  endDate?: Date | null;
  minDate?: Date;
  maxDate?: Date;
  timeUnits?: TimeUnitFormat[];
  customTimeView?: CustomTimeViewProps['children'];
  customTimeViewDirection?: CustomTimeViewProps['direction'];
  customTimes?: CustomTimeViewProps['customTimes'];
  hideCalendarYearButton?: DatePickerProps['hideCalendarYearButton'];
  formatTag?: (ms: number) => string;
  onChange?: (params: DateRange, text: string, timeUnit?: string) => void;
  onChangeDatePicker?: (params: DateRange) => void;
}

export const DatePanel = ({
  locale,
  dateFormat,
  open,
  children,
  className = '',
  datePickerClassName,
  startDate,
  endDate,
  minDate,
  maxDate,
  timeUnits = ['5m', '20m', '1h', '3h', '6h', '12h', '1d', '2d'],
  customTimeView,
  customTimeViewDirection = 'left',
  customTimes = {},
  formatTag,
  onChange,
  onChangeDatePicker,
  hideCalendarYearButton,
  ...props
}: DatePanelProps) => {
  const {
    appContext: { seamToken, timeZone },
  } = React.useContext(AppContext);
  const delayCloseRef = React.useRef<NodeJS.Timeout>();
  const [openMore, setOpenMore] = React.useState(false);
  const [showDatePicker, setShowDatePicker] = React.useState(false);
  const defatulCustomTimes = {
    ...getDefaultCustomTimes(locale, seamToken, timeZone),
    ...customTimes,
  };

  React.useEffect(() => {
    if (!open) {
      setOpenMore(false);
    }
  }, [open]);

  const getListItemText = (ms: number) => {
    if (endDate) {
      return formatDistance(endDate.getTime() - ms, endDate, {
        locale,
        addSuffix: true,
      });
    }
  };

  const handleClickTimeUnit = (ms: number, text: string, timeUnit: string) => {
    const now = Date.now();
    onChange?.([new Date(now - ms), new Date(now)], text, timeUnit);
  };

  return (
    <div
      {...props}
      className={classNames('rich-datetime-picker__panel', className, {
        [`expand-${customTimeViewDirection}`]: openMore,
      })}
    >
      {showDatePicker ? (
        <DatePicker
          className={datePickerClassName}
          locale={locale}
          startDate={startDate}
          endDate={endDate}
          minDate={minDate}
          maxDate={maxDate}
          onChange={onChangeDatePicker}
          onUnmount={() => setShowDatePicker(false)}
          hideCalendarYearButton={hideCalendarYearButton}
        />
      ) : (
        <>
          {!children ? (
            <>
              {timeUnits?.map((timeUnit, i) => {
                const milliseconds = convertToMilliseconds(timeUnit, timeZone);
                const text = timeUnit === 'today' ? timeUnit : getListItemText(milliseconds) || '';

                return (
                  <div
                    key={i}
                    onClick={() => {
                      handleClickTimeUnit(milliseconds, text, timeUnit);
                    }}
                    className="rich-datetime-picker__item"
                  >
                    <div className="rich-datetime-picker__tag">
                      {getFormattedTimeUnit(milliseconds, formatTag)}
                    </div>
                    <div className="rich-datetime-picker__item-text">{text}</div>
                  </div>
                );
              })}
              <div
                className="rich-datetime-picker__item"
                onClick={(e) => {
                  e.preventDefault();
                  setShowDatePicker(true);
                }}
              >
                <div className="rich-datetime-picker__tag">
                  <CalendarIcon />
                </div>
                <div className="rich-datetime-picker__item-text">Select from calendar...</div>
              </div>
              <div
                className="rich-datetime-picker__item"
                onMouseEnter={() => {
                  delayCloseRef.current && clearTimeout(delayCloseRef.current);
                  setOpenMore(true);
                }}
                onMouseLeave={() => {
                  delayCloseRef.current = setTimeout(() => {
                    setOpenMore(false);
                  }, 250);
                }}
              >
                <div className="rich-datetime-picker__tag">
                  <MoreIcon />
                </div>
                <div className="rich-datetime-picker__item-text">More</div>
                <CustomTimeView
                  show={openMore}
                  locale={locale}
                  direction={customTimeViewDirection}
                  dateFormat={dateFormat}
                  customTimes={defatulCustomTimes}
                  onClickTimeString={(dateRange, timeString) => {
                    setOpenMore(false);
                    onChange?.(dateRange, timeString);
                  }}
                >
                  {customTimeView}
                </CustomTimeView>
              </div>
            </>
          ) : typeof children === 'function' ? (
            children(
              timeUnits.map((timeUnit) => {
                const milliseconds = convertToMilliseconds(timeUnit, timeZone);

                return {
                  timeUnit,
                  timeUnitToMilliseconds: milliseconds,
                  formattedTimeUnit: getFormattedTimeUnit(
                    convertToMilliseconds(timeUnit, timeZone),
                  ),
                  onChange,
                  close,
                };
              }),
            )
          ) : (
            children
          )}
        </>
      )}
    </div>
  );
};
