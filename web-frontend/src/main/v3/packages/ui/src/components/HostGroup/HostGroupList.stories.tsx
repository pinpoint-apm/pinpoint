import type { Meta, StoryObj } from '@storybook/react';
import { HostGroupList } from './HostGroupList';

const meta = {
  title: 'PINPOINT UI/Components/HostGroupList',
  component: HostGroupList,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof HostGroupList>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {},
};
