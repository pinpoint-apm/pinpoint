// .storybook/preview.js
import { IconContext } from "react-icons";
import { GlobalStyle } from '@pinpoint-fe/common/components/Styled/GlobalStyle';

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
      {GlobalStyle}
      <IconContext.Provider value={{ style: { verticalAlign: 'middle' } }}>
        <Story />
      </IconContext.Provider>
    </>
  ),
];
