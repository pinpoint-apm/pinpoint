import React, { createContext } from 'react';

import { DateRange, DateRangeTime, DATE_FORMAT } from '@pinpoint-fe/constants';

export type DateStateType = { from: Date, to: Date };

export type DateRangeContextType = {
  // range
  range: DateRange,
  updateRange: (range: DateRange) => void,
  dateState: DateStateType,
  updateDateState: (dateState: DateStateType) => void,
  isFormatError?: boolean;
  dateFormat: string;
}

const DateRangeContext = createContext<DateRangeContextType>({
  range: DateRange.FIVE_MINUTES,
  updateRange: () => {},
  dateState: { from: new Date(Date.now() - DateRangeTime['5m']), to: new Date()},
  updateDateState: () => {},
  dateFormat: DATE_FORMAT,
})

export default DateRangeContext;
