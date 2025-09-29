import React from 'react';
import type { Meta, StoryObj } from '@storybook/react';
import { ProgressBar } from './ProgressBar';

const meta: Meta<typeof ProgressBar> = {
  title: 'PINPOINT/Component/Common/ProgressBar',
  component: ProgressBar,
  argTypes: {
    range: {
      control: { type: 'object' },
    },
    progress: {
      control: { type: 'number' },
    },
    tickCount: {
      control: { type: 'number' },
    },
    reverse: {
      control: { type: 'boolean' },
    },
    hideTick: {
      control: { type: 'boolean' },
    },
    onChange: { action: 'changed' },
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const Template = (args: any) => {
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

export const Default: Story = {
  render: Template,
  args: {
    formatTick: (value) => `${Math.round(value)}`,
    hideTick: false,
    reverse: false,
  },
};

// export const HideTick: Story = {
//   render: Template,
//   args: {
//     hideTick: true,
//   },
// };
