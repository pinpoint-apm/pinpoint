import { ComponentStory, ComponentMeta } from '@storybook/react';

import { ProgressBarWithControls } from './ProgressBarWithControls';

export default {
  title: 'PINPOINT/Component/Common/ProgressBarWithControls',
  component: ProgressBarWithControls,
  argTypes: {
    complete: {
      control: 'boolean',
    },
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof ProgressBarWithControls>;

const Template: ComponentStory<typeof ProgressBarWithControls> = (args) => (
  <ProgressBarWithControls {...args} />
);

export const Default = Template.bind({});
Default.args = {};
