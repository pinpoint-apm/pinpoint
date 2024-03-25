import type { Meta, StoryObj } from '@storybook/react';
import { ServerMapQueryOption } from './ServerMapQueryOption';

// More on how to set up at: https://storybook.js.org/docs/react/writing/introduction#default-export
const meta = {
  title: 'PINPOINT UI/Components/ServerMapQueryOption',
  component: ServerMapQueryOption,
  parameters: {
    // Optional parameter to center the component in the Canvas. More info: https://storybook.js.org/docs/react/configure/story-layout
    layout: 'centered',
  },
  // This component will have an automatically generated Autodocs entry: https://storybook.js.org/docs/react/writing-docs/autodocs
  tags: ['autodocs'],
  // More on argTypes: https://storybook.js.org/docs/react/api/argtypes
} satisfies Meta<typeof ServerMapQueryOption>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {},
};
