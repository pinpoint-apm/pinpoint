import type { Meta, StoryObj } from '@storybook/react';
import { SystemMetricChartList } from './SystemMetricChartList';

const meta = {
  title: 'PINPOINT UI/Components/SystemMetricChartList',
  component: SystemMetricChartList,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof SystemMetricChartList>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {},
};
