import type { Meta, StoryObj } from '@storybook/react';
import { VirtualList } from './VirtualList';

type DataType = { name: string; role: string };
const data = [
  { name: 'Ellary Garnham', role: 'Assassin' },
  { name: 'Doy Illston', role: 'Tank' },
  { name: 'Quinlan Quainton', role: 'Marksman' },
  { name: 'Karel O Sirin', role: 'Mage' },
  { name: 'Theo Radki', role: 'Assassin' },
  { name: 'Retha Ruby', role: 'Assassin' },
  { name: 'Lorrie Harburtson', role: 'Mage' },
  { name: 'Lemuel Etuck', role: 'Marksman' },
  { name: 'Jeannette Champe', role: 'Assassin' },
  { name: 'Pace Cranney', role: 'Tank' },
  { name: 'Jedediah Simak', role: 'Mage' },
  { name: 'Faustine MacNab', role: 'Assassin' },
  { name: 'Gardener Reaper', role: 'Support' },
  { name: 'Arlen Castagneri', role: 'Support' },
  { name: 'Abigail Rennix', role: 'Marksman' },
  { name: 'Caesar O Scully', role: 'Support' },
  { name: 'Jedediah Houldcroft', role: 'Fighter' },
  { name: 'Dion Maling', role: 'Assassin' },
  { name: 'Jake Farnill', role: 'Fighter' },
  { name: 'Conrad Finicj', role: 'Mage' },
];

// More on how to set up stories at: https://storybook.js.org/docs/react/writing-stories/introduction#default-export
const meta = {
  title: 'PINPOINT UI/Components/VirtualList',
  component: VirtualList<DataType>,
  parameters: {
    // Optional parameter to center the component in the Canvas. More info: https://storybook.js.org/docs/react/configure/story-layout
    layout: 'centered',
  },
  // This component will have an automatically generated Autodocs entry: https://storybook.js.org/docs/react/writing-docs/autodocs
  tags: ['autodocs'],
  // More on argTypes: https://storybook.js.org/docs/react/api/argtypes
} satisfies Meta<typeof VirtualList<DataType>>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    list: data,
    className: '!h-96 w-80',
    itemChild: (data) => data.name,
    onClickItem: (item) => {
      console.log(item);
    },
  },
};
