import type { Meta, StoryObj } from '@storybook/react';

import { SqlStatChart } from './SqlStatChart';

const meta = {
  title: 'PINPOINT UI/Components/SqlStatChart',
  component: SqlStatChart,
  tags: ['autodocs'],
} satisfies Meta<typeof SqlStatChart>;

export default meta;
type Story = StoryObj<typeof SqlStatChart>;

export const AvgTime: Story = {
  args: {},
};
