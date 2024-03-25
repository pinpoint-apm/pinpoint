import type { Meta, StoryObj } from '@storybook/react';
import { UrlStatChart } from './UrlStatChart';

const meta = {
  title: 'PINPOINT UI/Components/UrlStatChart',
  component: UrlStatChart,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof UrlStatChart>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {},
};
