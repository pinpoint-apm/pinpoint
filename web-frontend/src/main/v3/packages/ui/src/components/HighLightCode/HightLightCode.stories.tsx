import type { Meta, StoryObj } from '@storybook/react';
import { HighLightCode } from './HighLightCode';

// More on how to set up stories at: https://storybook.js.org/docs/react/writing-stories/introduction#default-export
const meta = {
  title: 'PINPOINT/Component/HightLightCode.stories',
  component: HighLightCode,
  parameters: {
    // Optional parameter to center the component in the Canvas. More info: https://storybook.js.org/docs/react/configure/story-layout
    layout: 'centered',
  },
  // This component will have an automatically generated Autodocs entry: https://storybook.js.org/docs/react/writing-docs/autodocs
  tags: ['autodocs'],
  // More on argTypes: https://storybook.js.org/docs/react/api/argtypes
} satisfies Meta<typeof HighLightCode>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    language: 'sql',
    code: ` SELECT role_id, permission_collection AS permissionCollectionJson FROM role_definition WHERE role_id = ?`,
  },
};
