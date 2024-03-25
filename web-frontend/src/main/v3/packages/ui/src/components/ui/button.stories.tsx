import type { Meta, StoryObj } from '@storybook/react';
import { Button } from './button';

// More on how to set up at: https://storybook.js.org/docs/react/writing/introduction#default-export
const meta = {
  title: 'PINPOINT UI/Components/button',
  component: Button,
  parameters: {
    // Optional parameter to center the component in the Canvas. More info: https://storybook.js.org/docs/react/configure/story-layout
    layout: 'centered',
  },
  // This component will have an automatically generated Autodocs entry: https://storybook.js.org/docs/react/writing-docs/autodocs
  tags: ['autodocs'],
  argTypes: {
    // variant: buttonVariants,
  },
  // More on argTypes: https://storybook.js.org/docs/react/api/argtypes
} satisfies Meta<typeof Button>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {},
};
