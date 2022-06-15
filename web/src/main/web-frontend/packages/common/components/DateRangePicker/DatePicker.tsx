import 'react-datepicker/dist/react-datepicker.css';
import React, { FC, useState, useContext } from 'react';
import DP from 'react-datepicker';
import styled from '@emotion/styled';
import { addDays, endOfDay } from 'date-fns';

import DateRangeContext from './DateRangeContext';

interface DatePickerProps {
}

export type DatePickerChangeEventHandler = (date: Date) => void;

export const DatePicker: FC<DatePickerProps> = ({
}: DatePickerProps) => {
  const { updateDateState } = useContext(DateRangeContext)
  const [ dateRange, setDateRange ] = useState<[Date | null , Date | null]>([null, null]);
  const [ startDate, endDate ] = dateRange;

  React.useEffect(() => {
    if (startDate && endDate) {
      updateDateState({
        from: startDate,
        to: endDate,
      })
    }
  }, [ startDate, endDate ])

  function handleChange(dates: [Date , Date | null], event: React.SyntheticEvent<any, Event> | undefined) {
    event?.stopPropagation();
    if (dates[1] instanceof Date) {
      dates = [dates[0], endOfDay(dates[1])];
    }
    setDateRange(dates);
  };

  return (
    <StyledWrapper>
      <DP 
       selectsRange={true}
       startDate={startDate}
       endDate={endDate}
       onChange={handleChange}
       inline
       maxDate={addDays(new Date(), 1)}
      />
    </StyledWrapper>
  );
};

const StyledWrapper = styled.div`
  .react-datepicker,
  .react-datepicker__month-container {
    width: 100%;
  }
  .react-datepicker {
    border: none;
  }
  .react-datepicker__header {
    background-color: var(--white-default);
  }
  .react-datepicker__day--keyboard-selected {
    background-color: var(--icon-default);
  }
  .react-datepicker__day--in-range {
    background-color: var(--secondary);
  }
`;