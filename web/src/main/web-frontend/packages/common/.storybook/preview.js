// .storybook/preview.js
import * as NextImage from 'next/image';
import { GlobalStyle } from '@pinpoint-fe/common/components/Styled/GlobalStyle';

// de optimize next image
const OriginalNextImage = NextImage.default;
Object.defineProperty(NextImage, "default", {
  configurable: true,
  value: (props) => <OriginalNextImage {...props} unoptimized />,
});

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
      <Story />
    </>
  ),
];
