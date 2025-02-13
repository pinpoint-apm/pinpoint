import React from 'react';
import type { Meta, StoryObj } from '@storybook/react';
import { RichDatetimePicker, RichDatetimePickerProps } from '../components/RichDatetimePicker';
import { format, subMinutes, subDays } from 'date-fns';
import { DateRange } from '../types';

// More on how to set up at: https://storybook.js.org/docs/react/writing/introduction#default-export
const meta = {
  title: 'Rich/RichDatetimePicker/Basic',
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
  const handleChange = (params: DateRange) => {
    setStartDate(params[0]);
    setEndDate(params[1]);
  };
  return (
    <div className="h-[400px] font-sans">
      <RichDatetimePicker
        {...args}
        startDate={startDate}
        endDate={endDate}
        onChange={handleChange}
        className="w-85"
        // dateFormat="MMM dd, HH:mm"
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

export const OpenAtInitialization: Story = {
  args: {
    defaultOpen: true,
  },
  render: TemplateDatetimePicker,
};

export const Korean: Story = {
  args: {
    localeKey: 'ko',
  },
  render: TemplateDatetimePicker,
};

export const CustomizeListItems: Story = {
  args: {
    localeKey: 'ko',
    children: (props) => {
      return props?.map(({ timeUnitToMilliseconds, formattedTimeUnit }, i) => {
        return (
          <div className="flex gap-2" key={i}>
            <div className="w-12">{formattedTimeUnit}</div>
            현재로부터 {timeUnitToMilliseconds}ms 전
          </div>
        );
      });
    },
  },
  render: TemplateDatetimePicker,
};

export const CutomizeToken: Story = {
  args: {
    seamToken: '~',
  },
  render: TemplateDatetimePicker,
};

export const CustomizeRelativeTimes: Story = {
  args: {
    localeKey: 'ko',
    customTimes: {
      연관: ['15m', '30m', 'yesterday'],
      고정: [`${format(new Date(), 'yyyy-MM-dd')}`, `${format(new Date(), 'MM/dd')}`],
    },
  },
  render: TemplateDatetimePicker,
};

export const PanelContainer: Story = {
  args: {
    getPanelContainer: () => document.querySelector('#panel-container'),
  },
  render: TemplateDatetimePicker,
};

export const ValidateDateRange: Story = {
  args: {
    validateDatePickerRange: ([from, to]) => {
      if (from && to) {
        if (subDays(to, 2) > from) {
          alert('Search duration may not be greater than 2days.');
          return false;
        }
      }
      return true;
    },
  },
  render: TemplateDatetimePicker,
};
