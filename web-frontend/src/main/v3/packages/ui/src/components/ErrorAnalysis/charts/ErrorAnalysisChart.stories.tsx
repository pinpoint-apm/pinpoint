import type { Meta, StoryObj } from '@storybook/react';
import { ErrorAnalysisChart } from './ErrorAnalysisChart';

const meta = {
  title: 'PINPOINT UI/Components/ErrorAnalysisChart',
  component: ErrorAnalysisChart,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof ErrorAnalysisChart>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {},
};
