import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import Dropdown from './Dropdown';
import { css } from '@emotion/react';
import styled from '@emotion/styled';

export default {
  title: 'PINPOINT/Component/BASE/Dropdown',
  component: Dropdown,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof Dropdown>;

const DefaultTemplate: ComponentStory<typeof Dropdown> = (args) => (
  <Dropdown {...args}>
    <Dropdown.Trigger>
      <div css={css`padding: 10px; border: 1px solid black;`}>Toggler</div>
    </Dropdown.Trigger>
    <Dropdown.Content>
      <div css={css`padding: 10px; border: 1px solid black;`}>Content</div>
    </Dropdown.Content>
  </Dropdown>
);

export const Default = DefaultTemplate.bind({});
Default.args = {
};

const StyledDropdown = styled(Dropdown)`
  width: 200px;
`

const StyledTrigger = styled(Dropdown.Trigger)`
  /* width: 200px; */
`

const StyledContent = styled(Dropdown.Content)`
  top: 0px;
  left: 100%;
`

const HoverTemplate: ComponentStory<typeof Dropdown> = (args) => (
  <StyledDropdown {...args} hoverable>
    <StyledTrigger>
      <div css={css`padding: 10px; border: 1px solid black;`}>Toggler</div>
    </StyledTrigger>
    <StyledContent>
      <div css={css`padding: 10px; border: 1px solid black;`}>Content</div>
    </StyledContent>
  </StyledDropdown>
);

export const Hover = HoverTemplate.bind({});
Default.args = {

};
