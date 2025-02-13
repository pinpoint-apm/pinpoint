import React from 'react';
import type { Meta, StoryObj } from '@storybook/react';
import { RichDatetimePicker, RichDatetimePickerProps } from '../components/RichDatetimePicker';
import { subMinutes } from 'date-fns';
import { DateRange } from '../types';

// More on how to set up at: https://storybook.js.org/docs/react/writing/introduction#default-export
const meta = {
  title: 'Rich/RichDatetimePicker/TimeZone',
  component: RichDatetimePicker,
  parameters: {
    // Optional parameter to center the component in the Canvas. More info: https://storybook.js.org/docs/react/configure/story-layout
    layout: 'centered',
  },
  // This component will have an automatically generated Autodocs entry: https://storybook.js.org/docs/react/writing-docs/autodocs
  tags: ['autodocs'],
  // More on argTypes: https://storybook.js.org/docs/react/api/argtypes
  argTypes: {
    // backgroundColor: { control: 'color' },
  },
} satisfies Meta<typeof RichDatetimePicker>;

export default meta;
type Story = StoryObj<typeof meta>;

const TemplateDatetimePicker = (args: RichDatetimePickerProps) => {
  const now = new Date();
  const [startDate, setStartDate] = React.useState<Date | null>(subMinutes(now, 5));
  const [endDate, setEndDate] = React.useState<Date | null>(now);
  const [timeZone, setTimeZone] = React.useState('Asia/Seoul');
  const handleChange = (params: DateRange) => {
    setStartDate(params[0]);
    setEndDate(params[1]);
  };

  return (
    <div className="flex h-[400px] flex-col gap-4 font-sans">
      <select onChange={(e) => setTimeZone(e.target.value)} value={timeZone}>
        {Intl.supportedValuesOf('timeZone').map((tz, i) => {
          return (
            <option key={i} value={tz}>
              {tz}
            </option>
          );
        })}
      </select>
      <RichDatetimePicker
        {...args}
        startDate={startDate}
        endDate={endDate}
        onChange={handleChange}
        className="w-85"
        timeZone={timeZone}
      />
    </div>
  );
};

export const Default: Story = {
  args: {
    //
  },
  render: TemplateDatetimePicker,
};
