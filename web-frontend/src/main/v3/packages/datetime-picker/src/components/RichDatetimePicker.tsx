import './datetimePicker.scss';
import React from 'react';
import classNames from 'classnames';
import { isValid } from 'date-fns';
import { useOnClickOutside, useUpdateEffect } from 'usehooks-ts';
import { getLocale } from '../utils/locale';
import { DateRange, LocaleKey } from '../types';
import { SEAM_TOKEN } from '../constants/patterns';
import { useCaptureKeydown } from '../utils/useCaptureKeydown';
import { getFormattedTimeUnit, getZonedEndOfDay, parseTimeString } from '../utils/date';
import { DatePanel, DatePanelProps } from './DatePanel';
import { withPortalPanelContainer } from './hoc/withPortalPanelContainer';
import AppContext from './context/appContext';
import { formatInTimeZone } from 'date-fns-tz';

export interface RichDatetimePickerProps
  extends Omit<DatePanelProps, 'locale' | 'open' | 'className' | 'onChangeDatePicker'> {
  disable?: boolean;
  startDate?: Date | null;
  endDate?: Date | null;
  minDate?: Date;
  maxDate?: Date;
  className?: string;
  inputClassName?: string;
  triggerClassName?: string;
  panelClassName?: string;
  localeKey?: LocaleKey;
  timeZone?: string;
  seamToken?: string;
  defaultOpen?: boolean;
  displayedInput?: string;
  getPanelContainer?: () => HTMLElement | null;
  validateDatePickerRange?: (params: DateRange) => boolean;
}

export interface RichDatetimePickerListItemProps {
  timeUnit: string;
  timeUnitToMilliseconds: number;
  formattedTimeUnit: string;
  close: () => void;
}

export interface RichDatetiemPickerMoreViewProps {
  open: boolean;
}

const DatePanelWithPortalContainer = withPortalPanelContainer(DatePanel);

export const RichDatetimePicker = ({
  className = '',
  disable,
  inputClassName = '',
  triggerClassName = '',
  panelClassName = '',
  datePickerClassName = '',
  startDate,
  endDate,
  seamToken = SEAM_TOKEN,
  localeKey = 'en',
  timeZone,
  dateFormat = 'MMM do, hh:mm a',
  defaultOpen,
  onChange,
  getPanelContainer,
  validateDatePickerRange = () => true,
  formatTag,
  displayedInput,
  ...props
}: RichDatetimePickerProps) => {
  const tz = timeZone || Intl.DateTimeFormat().resolvedOptions().timeZone;
  const hasPanelContainer = getPanelContainer && getPanelContainer?.();
  const containerRef = React.useRef<HTMLDivElement>(null);
  const triggerRef = React.useRef<HTMLDivElement>(null);
  const locale = React.useMemo(() => getLocale(localeKey), [localeKey]);
  const [from, setFrom] = React.useState<Date | null | undefined>(startDate);
  const [to, setTo] = React.useState<Date | null | undefined>(endDate);
  const [open, setOpen] = React.useState(defaultOpen);
  const [isValidInput, setValidInput] = React.useState(true);
  const [dateInput, setDateInput] = React.useState('');
  const [displayInput, setDisplayInput] = React.useState(displayedInput);
  const [appContext, setAppContext] = React.useState({ seamToken, timeZone: tz });

  useOnClickOutside(containerRef, () => {
    !hasPanelContainer && setOpen(false);
  });

  useCaptureKeydown((event) => {
    if (event.code === 'Escape') {
      open && setOpen(false);
    }
  });

  useUpdateEffect(() => {
    setFrom(startDate);
  }, [startDate]);

  useUpdateEffect(() => {
    setTo(endDate);
  }, [endDate]);

  useUpdateEffect(() => {
    setDisplayInput(displayedInput);
  }, [displayedInput]);

  useUpdateEffect(() => {
    // datepicker에서 startDate만 선택하고 패널을 닫는 경우
    if (!open && !to && from instanceof Date) {
      onChange?.(
        [from, getZonedEndOfDay(from, tz)],
        getFormattedDate(from, getZonedEndOfDay(from, tz)),
      );
    }
  }, [open]);

  useUpdateEffect(() => {
    setAppContext((prev) => ({ ...prev, timeZone: tz }));
  }, [tz]);

  React.useEffect(() => {
    if (from && to) {
      setDateInput(getFormattedDate(from, to));
    }
  }, [from, to, locale, dateFormat, tz]);

  const getFormattedDate = (from: Date, to: Date) => {
    const zonedFrom = formatInTimeZone(from, tz, dateFormat, { locale });
    const zonedTo = formatInTimeZone(to, tz, dateFormat, { locale });

    return `${zonedFrom} ${appContext.seamToken} ${zonedTo}`;
  };

  const handleChangeInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newInput = e.target.value;

    if (
      parseTimeString(newInput.trim(), locale, {
        dateFormat,
        seamToken: appContext.seamToken,
        timeZone: tz,
      }).every((date) => isValid(date))
    ) {
      setValidInput(true);
    } else {
      setValidInput(false);
    }

    setDateInput(newInput);
  };

  const handleKeyDownInput = (e: React.KeyboardEvent) => {
    if (e.code === 'Enter' && isValidInput) {
      onChange?.(
        parseTimeString(dateInput, locale, {
          dateFormat,
          seamToken: appContext.seamToken,
          timeZone: tz,
        }),
        dateInput,
      );
      setOpen(false);
    }
  };

  return (
    <AppContext.Provider value={{ appContext, setAppContext }}>
      <div className={classNames('rich-datetime-picker relative', className)} ref={containerRef}>
        <div
          ref={triggerRef}
          className={classNames(
            'rich-datetime-picker__trigger',
            {
              'border-primary': open,
              'border-stateRed': !isValidInput,
            },
            {
              'div-disable': disable,
            },
            triggerClassName,
          )}
          onClick={() => setOpen(true)}
        >
          <div className="rich-datetime-picker__tag absolute left-1.5 top-1.5">
            {from && to ? getFormattedTimeUnit(to?.getTime() - from?.getTime(), formatTag) : '-'}
          </div>
          {open ? (
            <input
              type="text"
              value={dateInput}
              className={classNames('rich-datetime-picker__input', inputClassName)}
              onClick={(e) => open && e.stopPropagation()}
              onChange={handleChangeInput}
              onKeyDown={handleKeyDownInput}
            />
          ) : (
            <input
              type="text"
              value={displayInput || dateInput}
              className={classNames('rich-datetime-picker__input', inputClassName)}
              onClick={(e) => open && e.stopPropagation()}
              readOnly
            />
          )}
        </div>
        {open && (
          <DatePanelWithPortalContainer
            open={open}
            className={panelClassName}
            datePickerClassName={datePickerClassName}
            startDate={from}
            endDate={to}
            locale={locale}
            onChange={(dates, text, timeUnit) => {
              setValidInput(true);
              setDisplayInput(text);
              setOpen(false);
              onChange?.(dates, text, timeUnit);
            }}
            onChangeDatePicker={(dates) => {
              if (dates[0] && dates[1] === null) {
                setFrom(dates[0]);
                setTo(dates[1]);
              } else if (dates[0] && dates[1]) {
                if (validateDatePickerRange(dates)) {
                  setFrom(dates[0]);
                  setTo(dates[1]);
                  onChange?.(dates, getFormattedDate(dates[0], dates[1]));
                  setDisplayInput('');
                  setOpen(false);
                }
              }
            }}
            getPanelContainer={getPanelContainer}
            onClickOutside={() => setOpen(false)}
            triggerRef={triggerRef}
            {...props}
          />
        )}
      </div>
    </AppContext.Provider>
  );
};
