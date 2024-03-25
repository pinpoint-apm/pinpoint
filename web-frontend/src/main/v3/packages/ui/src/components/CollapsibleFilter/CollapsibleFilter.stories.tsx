import type { Meta, StoryObj } from '@storybook/react';
import { CollapsibleFilter } from './CollapsibleFilter';

// More on how to set up stories at: https://storybook.js.org/docs/react/writing-stories/introduction#default-export
const meta = {
  title: 'PINPOINT UI/Components/CollapsibleFilter',
  component: CollapsibleFilter,
  parameters: {
    // Optional parameter to center the component in the Canvas. More info: https://storybook.js.org/docs/react/configure/story-layout
    layout: 'centered',
  },
  // This component will have an automatically generated Autodocs entry: https://storybook.js.org/docs/react/writing-docs/autodocs
  tags: ['autodocs'],
  // More on argTypes: https://storybook.js.org/docs/react/api/argtypes
} satisfies Meta<typeof CollapsibleFilter>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    title: 'AgentId',
    filterOptions: [
      { id: 'id A', name: 'agent A' },
      { id: 'id B', name: 'agent B' },
      { id: 'id C', name: 'agent C' },
    ],
  },
};
