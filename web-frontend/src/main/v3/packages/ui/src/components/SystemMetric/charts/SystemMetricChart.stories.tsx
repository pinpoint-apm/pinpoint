import type { Meta, StoryObj } from '@storybook/react';
import { SystemMetricChart } from './SystemMetricChart';

const meta = {
  title: 'PINPOINT UI/Components/SystemMetricChart',
  component: SystemMetricChart,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof SystemMetricChart>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    chartInfo: { metricDefinitionId: 'cpu', tagGroup: false },
  },
};
