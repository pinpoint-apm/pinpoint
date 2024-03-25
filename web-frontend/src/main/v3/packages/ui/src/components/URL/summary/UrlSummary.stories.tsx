import type { Meta, StoryObj } from '@storybook/react';
import { UrlSummary } from './UrlSummary';

const meta = {
  title: 'PINPOINT UI/Components/UrlSummary',
  component: UrlSummary,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof UrlSummary>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {},
};
