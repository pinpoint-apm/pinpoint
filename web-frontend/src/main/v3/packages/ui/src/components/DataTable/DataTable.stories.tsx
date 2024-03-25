import type { Meta, StoryObj } from '@storybook/react';
import { DataTable } from './DataTable';
import { paymentData, paymentColumns } from './mockData';

// This type is used to define the shape of our data.
// You can use a Zod schema here if you want.

// More on how to set up stories at: https://storybook.js.org/docs/react/writing-stories/introduction#default-export
const meta = {
  title: 'PINPOINT/Component/DataTable.stories',
  component: DataTable,
  parameters: {
    // Optional parameter to center the component in the Canvas. More info: https://storybook.js.org/docs/react/configure/story-layout
    layout: 'centered',
  },
  // This component will have an automatically generated Autodocs entry: https://storybook.js.org/docs/react/writing-docs/autodocs
  tags: ['autodocs'],
  // More on argTypes: https://storybook.js.org/docs/react/api/argtypes
} satisfies Meta<typeof DataTable>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    data: paymentData,
    columns: paymentColumns,
  },
};
