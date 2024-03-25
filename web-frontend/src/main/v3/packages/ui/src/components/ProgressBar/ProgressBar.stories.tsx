import { ComponentStory, ComponentMeta } from '@storybook/react';

import { ProgressBar } from './ProgressBar';

export default {
  title: 'PINPOINT/Component/Common/ProgressBar',
  component: ProgressBar,
  argTypes: {
    range: {
      control: 'array',
    },
    progress: {
      control: 'number',
    },
    tickCount: {
      control: 'number',
    },
    reverse: {
      control: 'boolean',
    },
    hideTick: {
      control: 'boolean',
    },
    onChange: { action: console.log },
  },
} as ComponentMeta<typeof ProgressBar>;

const Template: ComponentStory<typeof ProgressBar> = (args) => {
  const [count, setCount] = React.useState(0);

  return (
    <>
      <ProgressBar {...args} progress={count} />
      <div style={{ marginTop: 20 }}>
        <button style={{ padding: 15 }} onClick={() => setCount(count + 10)}>
          ➕
        </button>
        <button style={{ padding: 15 }} onClick={() => setCount(count - 10)}>
          ➖
        </button>
      </div>
    </>
  );
};

export const Defatult = Template.bind({});
Defatult.args = {
  formatTick: (value) => `${Math.round(value)}`,
  hideTick: false,
  reverse: false,
  // onChange: ({ percent }) => console.log(percent),
};

// export const HideTick = Template.bind({});
// HideTick.args = {
//   hideTick: true,
// };
