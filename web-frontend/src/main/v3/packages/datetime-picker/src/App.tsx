import React from 'react';
import { RichDatetimePicker } from './components/RichDatetimePicker';
import { subMinutes } from 'date-fns';
import { convertToTimeUnit } from './utils/date';
import { DateRange } from './types';

function App() {
  const now = new Date();
  const [startDate, setStartDate] = React.useState<Date | null>(subMinutes(now, 5));
  const [endDate, setEndDate] = React.useState<Date | null>(now);

  const handleChange = (params: DateRange) => {
    setStartDate(params[0]);
    setEndDate(params[1]);
  };

  return (
    <>
      <div className="w-85">
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          onChange={handleChange}
          defaultOpen
        />
      </div>
      <div className="w-85">
        <RichDatetimePicker
          localeKey="ko"
          startDate={startDate}
          endDate={endDate}
          onChange={handleChange}
          customTimeViewDirection="right"
          getPanelContainer={() => document.querySelector('#panel-container')}
        />
      </div>
      <div className="w-85">
        <RichDatetimePicker
          localeKey="ko"
          startDate={startDate}
          endDate={endDate}
          formatTag={(ms) => convertToTimeUnit(ms).toUpperCase()}
          onChange={handleChange}
        >
          {(props) => {
            return props?.map(({ timeUnitToMilliseconds, formattedTimeUnit }, i) => {
              return (
                <div className="flex gap-2" key={i}>
                  <div className="w-12">{formattedTimeUnit}</div>
                  {timeUnitToMilliseconds} 전
                </div>
              );
            });
          }}
        </RichDatetimePicker>
      </div>
    </>
  );
}

export default App;
