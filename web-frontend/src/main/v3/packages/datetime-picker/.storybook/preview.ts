import React from 'react';
import type { Preview } from '@storybook/react';

import { withThemeByClassName } from '@storybook/addon-styling';

/* TODO: update import to your tailwind styles file. If you're using Angular, inject this through your angular.json config instead */
// import
// import '../src/index.css';
import '../src/components/datetimePicker.scss';

const preview: Preview = {
  parameters: {
    actions: { argTypesRegex: '^on[A-Z].*' },
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/,
      },
    },
  },

  decorators: [
    // Adds theme switching support.
    // NOTE: requires setting "darkMode" to "class" in your tailwind config
    withThemeByClassName({
      themes: {
        light: 'light',
        dark: 'dark',
      },
      defaultTheme: 'light',
    }),
    // (Story) => (
    //   <>
    //     <Story />
    //     <div id="root-sibling"></div>
    //   </>
    // ),
  ],
};

export default preview;
