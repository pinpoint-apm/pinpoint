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
  children: ['A', 'B', 'C'].map((b, i) => (
    <ButtonGroup.Button
      key={i}
      onClick={() => console.log(`${b} clicked`)}
    >{b}</ButtonGroup.Button>
  ))
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
  children: ['A', 'B', 'C'].map((b, i) => (
    <ButtonGroup.Button
      key={i}
      onClick={() => console.log(`${b} clicked`)}
    >{b}</ButtonGroup.Button>
  ))
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
  children: ['A', 'B', 'C'].map((b, i) => {
    if (i === 0) {
      return (
        <ButtonGroup.Button
          key={i}
          onClick={() => console.log(`${b} clicked`)}
          disableActive={true}
        >{b}</ButtonGroup.Button>
      )
    }
    return (
      <ButtonGroup.Button
        key={i}
        onClick={() => console.log(`${b} clicked`)}
      >{b}</ButtonGroup.Button>
    )
  })
};