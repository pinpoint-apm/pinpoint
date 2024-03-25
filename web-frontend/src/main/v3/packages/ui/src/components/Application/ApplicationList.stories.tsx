import type { Meta, StoryObj } from '@storybook/react';
import { ApplicationList } from './ApplicationList';
import { mockData } from './mockData';

// More on how to set up stories at: https://storybook.js.org/docs/react/writing-stories/introduction#default-export
const meta = {
  title: 'PINPOINT UI/Components/ApplicationList',
  component: ApplicationList,
  parameters: {
    // Optional parameter to center the component in the Canvas. More info: https://storybook.js.org/docs/react/configure/story-layout
    layout: 'centered',
  },
  // This component will have an automatically generated Autodocs entry: https://storybook.js.org/docs/react/writing-docs/autodocs
  tags: ['autodocs'],
  // More on argTypes: https://storybook.js.org/docs/react/api/argtypes
} satisfies Meta<typeof ApplicationList>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    list: mockData,
    className: '!h-96 w-80',
  },
};
