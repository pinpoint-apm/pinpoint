// .storybook/preview.js
import { IconContext } from 'react-icons';

export const parameters = {
  actions: { argTypesRegex: "^on[A-Z].*" },
  controls: {
    matchers: {
      color: /(background|color)$/i,
      date: /Date$/,
    },
  },
}

export const decorators = [
  (Story) => (
    <>
      <IconContext.Provider value={{ style: { verticalAlign: 'middle' } }}>
        <Story />
      </IconContext.Provider>
    </>
  ),
];
