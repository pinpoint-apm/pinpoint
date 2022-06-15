import React, { FC, memo, useState, useMemo, useEffect, useCallback } from 'react';
import styled from '@emotion/styled';
import { subDays } from 'date-fns';

import { useSkipFirstEffect } from '@/hooks/render';
import { QuickButtons } from './QuickButtons';
import { RangeDropdown } from './RangeDropdown';
import DateRangeContext, { DateRange, DateRangeContextType, DateRangeTime, DateStateType } from './DateRangeContext';

type DateRangeStateType = Pick<DateRangeContextType, 'range' | 'dateState'>;

export interface DateRangePickerProps {
  initRange?: DateRange,
  initStartDate?: Date,
  initEndDate?: Date, 
  onChange?: ({ range, dateState }: DateRangeStateType) => void,
}

const m = 1000 * 60;

export const DateRangePicker: FC<DateRangePickerProps> = memo(({
  initRange = DateRange.FIVE_MINUTES,
  initStartDate = new Date(Date.now() - m * 5),
  initEndDate = new Date(),
  onChange,
}: DateRangePickerProps) => {
  const [ range, setRange ] = useState(initRange);
  const [ dateState, setDateState ] = useState<DateStateType>({
    from: initStartDate,
    to: initEndDate,
  })

  useSkipFirstEffect(() => {
    onChange?.({ range, dateState });
  }, [ range, dateState ])

  const updateRange = useCallback((range: DateRange) => {
    setRange(range);
    if (range !== DateRange.REAL_TIME) {
      updateDateState({
        from: new Date(Date.now() - DateRangeTime[range]),
        to: new Date(),
      })
    }
  }, []);

  const updateDateState = useCallback(({ from, to }: { from: Date, to: Date }) => {
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
    }}>
      <StyledContainer>
        <RangeDropdown />
        <QuickButtons />
      </StyledContainer>
    </DateRangeContext.Provider>
  );
});

const StyledContainer = styled.div`
  display: flex;
  gap: 7px;
`




