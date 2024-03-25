import { TooltipProvider } from '../src/components/ui/tooltip';
import '../src/globals.css';
import type { Preview } from '@storybook/react';

export const preview: Preview = {
  parameters: {
    actions: { argTypesRegex: '^on[A-Z].*' },
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/,
      },
    },
  },
};

export const decorators = [
  (Story) => (
    <>
      <TooltipProvider>
        {/* {GlobalStyle}
      <IconContext.Provider value={{ style: { verticalAlign: 'middle' } }}> */}
        <Story />
        {/* <div id={POPPER_ROOT} />
      </IconContext.Provider> */}
      </TooltipProvider>
    </>
  ),
];

// export default preview;
