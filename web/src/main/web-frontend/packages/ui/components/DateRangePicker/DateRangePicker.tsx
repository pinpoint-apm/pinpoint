import React, { FC } from 'react';
import styled from '@emotion/styled';
import { format, isValid, parse, subDays } from 'date-fns';

import { DateRange, DateRangeTime, DATE_FORMAT } from '@pinpoint-fe/constants';
import { useSkipFirstEffect, getParsedDateRange } from '@pinpoint-fe/utils';
import { QuickButtons } from './QuickButtons';
import { RangeDropdown } from './RangeDropdown';
import DateRangeContext, { DateRangeContextType, DateStateType } from './DateRangeContext';

type DateRangeStateType = Pick<DateRangeContextType, 'dateState'>;
type onChangeHanlderType = (param: DateRangeStateType & {formattedDate: {from: string, to: string}}) => void;

export interface DateRangePickerProps {
  from?: Date | string,
  to?: Date | string,
  realtime?: boolean;
  defaultRange?: DateRange,
  dateFormat?: string,
  className?: string;
  onChange?: onChangeHanlderType,
}

export const DateRangePicker: FC<DateRangePickerProps> = React.memo(({
  from,
  to,
  realtime,
  defaultRange = DateRange.FIVE_MINUTES,
  dateFormat = DATE_FORMAT,
  className,
  onChange,
}: DateRangePickerProps) => {
  const [ range, setRange ] = React.useState(defaultRange);
  const [ dateState, setDateState ] = React.useState<DateStateType>(getParsedDateRange({from, to}))

  React.useEffect(() => {
    setDateState(getParsedDateRange({from, to}));
  }, [ from, to ])

  useSkipFirstEffect(() => {
    onChange?.({ 
      dateState, 
      formattedDate: {
        from: format(dateState.from, dateFormat),
        to: format(dateState.to, dateFormat),
      }
    });
  }, [ range, dateState ])

  const updateRange = React.useCallback((range: DateRange) => {
    setRange(range);
    if (range !== DateRange.REAL_TIME) {
      updateDateState({
        from: new Date(Date.now() - DateRangeTime[range]),
        to: new Date(),
      })
    }
  }, []);

  const updateDateState = React.useCallback(({ from, to }: { from: Date, to: Date }) => {
    if (from >= to) {
      alert('시작 날짜가 끝 날짜보다 크거나 같을 수 없습니다.');
    } else if (subDays(to, 2) > from ) {
      alert('시작 날짜와 끝 날짜의 간격을 2일 이하로 설정해주세요.');
    } else {
      setDateState({
        from,
        to,
      })
    }
  }, []);

  return (
    <DateRangeContext.Provider value={{ 
      range, 
      updateRange,
      dateState,
      updateDateState,
      dateFormat,
    }}>
      <StyledContainer className={className}>
        <RangeDropdown />
        <QuickButtons />
      </StyledContainer>
    </DateRangeContext.Provider>
  );
});

const StyledContainer = styled.div`
  display: flex;
  gap: 5px;
`

