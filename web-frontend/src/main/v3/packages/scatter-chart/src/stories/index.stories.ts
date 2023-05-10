import { createAppendDatas } from './templates/createAppendDatas';
import { createCaptureIamge } from './templates/createCaptureImage';
import { createCustomizeTheme } from './templates/createCustomizeTheme';
import { createDefault } from './templates/createDefault';
import { createDestroy } from './templates/createDestroy';
import { createInteractions } from './templates/createInteractions';
import { createPinpointTheme } from './templates/createPinpointTheme';
import { createRealtime } from './templates/createRealtime';
import { createResizable } from './templates/createResizable';
import { createSetOption } from './templates/createSetOption';
import { createMultipleCharts } from './templates/createMultipleCharts';
import { createHideLegend } from './templates/createHideLegend';

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

const TemplateHideLegend = () => createHideLegend();
export const HideLegend = TemplateHideLegend.bind({});

const TemplateInteractions = () => createInteractions();
export const Interactions = TemplateInteractions.bind({});

const TemplateAppendDatas = () => createAppendDatas();
export const AppendDatas = TemplateAppendDatas.bind({});

const TemplateResize = () => createResizable();
export const Resize = TemplateResize.bind({});

const TemplateSetOption = () => createSetOption();
export const SetOption = TemplateSetOption.bind({});

const TemplateCaptureImage = () => createCaptureIamge();
export const CaptureImage = TemplateCaptureImage.bind({});

const TemplateRealtime = () => createRealtime();
export const Realtime = TemplateRealtime.bind({});

const TemplateCustomizeTheme = () => createCustomizeTheme();
export const CustomizeTheme = TemplateCustomizeTheme.bind({});

const TemplatePinpointTheme = () => createPinpointTheme();
export const PinpointTheme = TemplatePinpointTheme.bind({});

const TemplateDestroy = () => createDestroy();
export const Destroy = TemplateDestroy.bind({});

const TemplateMultipleCharts = () => createMultipleCharts();
export const MultipleCharts = TemplateMultipleCharts.bind({});
