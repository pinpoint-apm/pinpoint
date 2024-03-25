import * as React from 'react';
import type { Meta, StoryObj } from '@storybook/react';
import { ButtonGroup, ButtonGroupItem } from './button-group';

// More on how to set up stories at: https://storybook.js.org/docs/react/writing-stories/introduction#default-export
const meta = {
  title: 'PINPOINT UI/Components/button-group',
  component: ButtonGroup,
  parameters: {
    // Optional parameter to center the component in the Canvas. More info: https://storybook.js.org/docs/react/configure/story-layout
    layout: 'centered',
  },
  // This component will have an automatically generated Autodocs entry: https://storybook.js.org/docs/react/writing-docs/autodocs
  tags: ['autodocs'],
  // More on argTypes: https://storybook.js.org/docs/react/api/argtypes
} satisfies Meta<typeof ButtonGroup>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    defaultValue: 'medium',
    children: (
      <>
        <ButtonGroupItem value="low">Low</ButtonGroupItem>
        <ButtonGroupItem value="medium">Medium</ButtonGroupItem>
        <ButtonGroupItem value="high">High</ButtonGroupItem>
      </>
    ),
  },
};
