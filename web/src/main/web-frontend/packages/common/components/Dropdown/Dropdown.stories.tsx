import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import Dropdown from './Dropdown';
import { DropdownTrigger } from './DropdownTrigger';
import { DropdownContent } from './DropdownContent';
import { css } from '@emotion/react';

export default {
  title: 'PINPOINT/Component/BASE/Dropdown',
  component: Dropdown,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof Dropdown>;

const Template: ComponentStory<typeof Dropdown> = (args) => (
  <Dropdown {...args}>
    <Dropdown.Trigger>
      <div css={css`padding: 10px; border: 1px solid black;`}>Toggler</div>
    </Dropdown.Trigger>
    <Dropdown.Content>
      <div css={css`padding: 10px; border: 1px solid black;`}>Content</div>
    </Dropdown.Content>
  </Dropdown>
);

export const Default = Template.bind({});
Default.args = {
};
