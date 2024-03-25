import type { Meta, StoryObj } from '@storybook/react';
import { ClipboardCopyButton } from './ClipboardCopyButton';

const meta = {
  title: 'PINPOINT/Component/Button.stories',
  component: ClipboardCopyButton,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof ClipboardCopyButton>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    copyValue: 'someText',
  },
};
