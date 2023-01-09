import { createAppendDatas } from './templates/createAppendDatas';
import { createCaptureIamge } from './templates/createCaptureImage';
import { createDefault } from './templates/createDefault';
import { createResizable } from './templates/createResizable';
import { createSetAxis } from './templates/createSetAxis';

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
const TemplateDefault = () => createDefault();
export const Defatult = TemplateDefault.bind({});

const TemplateAppendDatas = () => createAppendDatas();
export const AppendDatas = TemplateAppendDatas.bind({});

const TemplateResize = () => createResizable();
export const Resize = TemplateResize.bind({});

const TemplateSetAxis = () => createSetAxis();
export const SetAxis = TemplateSetAxis.bind({});

const TemplateCaptureImage = () => createCaptureIamge();
export const CaptureImage = TemplateCaptureImage.bind({});
