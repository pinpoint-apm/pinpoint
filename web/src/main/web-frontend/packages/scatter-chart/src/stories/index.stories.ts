import { createScatterChart, createScatterChartResizable } from './ScatterChartStory';

// More on default export: https://storybook.js.org/docs/html/writing-stories/introduction#default-export
export default {
  title: 'ScatterChart',
  // More on argTypes: https://storybook.js.org/docs/html/api/argtypes
  argTypes: {
    // backgroundColor: { control: 'color' },
    // label: { control: 'text' },
    // onClick: { action: 'onClick' },
    // primary: { control: 'boolean' },
    // size: {
    //   control: { type: 'select' },
    //   options: ['small', 'medium', 'large'],
    // },
  },
};

// More on component templates: https://storybook.js.org/docs/html/writing-stories/introduction#using-args
const TemplateDefault = () => createScatterChart();
export const Defatult = TemplateDefault.bind({});

const TemplateResize = () => createScatterChartResizable();
export const Resize = TemplateResize.bind({});