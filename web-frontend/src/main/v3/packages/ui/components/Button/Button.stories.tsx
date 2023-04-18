import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';
import { Button, ButtonType } from './Button';
import { FaPlus } from 'react-icons/fa';

export default {
  title: 'PINPOINT/Component/Base/Button',
  component: Button,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof Button>;

const Template: ComponentStory<typeof Button> = (args) => <Button {...args} />;
const TemplateIcon: ComponentStory<any> = () => (
  <>
    <Button 
      styleType={ButtonType.Primary}
      label={<FaPlus />}
    />
    <br />
    <br />
    <Button  
      styleType={ButtonType.Disable}
      label={<FaPlus />}
    />
  </>
)

export const Primary = Template.bind({});
Primary.args = {
  label: 'Primary',
  styleType: ButtonType.Primary
};

export const Disable = Template.bind({});
Disable.args = {
  label: 'Disable',
  styleType: ButtonType.Disable
};

export const Default = Template.bind({});
Default.args = {
  label: 'Default',
};

export const Icons = TemplateIcon.bind({});
Icons.args = {
};
