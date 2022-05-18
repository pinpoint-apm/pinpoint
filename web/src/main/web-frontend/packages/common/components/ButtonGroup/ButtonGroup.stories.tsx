import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import ButtonGroup from './ButtonGroup';
import { css } from '@emotion/react';

export default {
  title: 'PINPOINT/Component/BASE/ButtonGroup',
  component: ButtonGroup.Container,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof ButtonGroup.Container>;

const Template: ComponentStory<typeof ButtonGroup.Container> = (args) => (
  <ButtonGroup.Container {...args} />  
)

export const Default = Template.bind({});
Default.args = {
  customStyle: css`
    .active {
      background-color: red;
    }
    button {
      padding: 30px;
    }
  `,
  children: [
    <ButtonGroup.Button
      onClick={() => console.log('A clicked')}
    >A</ButtonGroup.Button>,
    <ButtonGroup.Button
      onClick={() => console.log('B clicked')}
    >B</ButtonGroup.Button>,
    <ButtonGroup.Button
      onClick={() => console.log('C clicked')}
    >C</ButtonGroup.Button>,
  ]
};

export const InitActiveIndex = Template.bind({});
InitActiveIndex.args = {
  initActiveIndex: 1,
  customStyle: css`
    .active {
      background-color: blue;
    }
    button {
      padding: 30px;
    }
  `,
  children: [
    <ButtonGroup.Button
      onClick={() => console.log('A clicked')}
    >A</ButtonGroup.Button>,
    <ButtonGroup.Button
      onClick={() => console.log('B clicked')}
    >B</ButtonGroup.Button>,
    <ButtonGroup.Button
      onClick={() => console.log('C clicked')}
    >C</ButtonGroup.Button>,
  ]
};

export const DisableActiveButtonA = Template.bind({});
DisableActiveButtonA.args = {
  initActiveIndex: 1,
  customStyle: css`
    .active {
      background-color: blue;
    }
    button {
      padding: 30px;
    }
  `,
  children: [
    <ButtonGroup.Button
      onClick={() => console.log('A clicked')}
      disableActive={true}
    >A</ButtonGroup.Button>,
    <ButtonGroup.Button
      onClick={() => console.log('B clicked')}
    >B</ButtonGroup.Button>,
    <ButtonGroup.Button
      onClick={() => console.log('C clicked')}
    >C</ButtonGroup.Button>,
  ]
};