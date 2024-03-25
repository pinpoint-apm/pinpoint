import type { Meta, StoryObj } from '@storybook/react';
import { ErrorAnalysisErrorDetail } from './ErrorAnalysisErrorDetail';

const meta = {
  title: 'PINPOINT UI/Components/ErrorAnalysisErrorDetail',
  component: ErrorAnalysisErrorDetail,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof ErrorAnalysisErrorDetail>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    errorInfo: {
      applicationName: '',
      agentId: '',
      spanId: 0,
      transactionId: '',
      exceptionId: 0,
    },
  },
};
