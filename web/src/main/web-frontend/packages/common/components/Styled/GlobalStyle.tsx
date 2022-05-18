import '../../styles/theme.css';
import { css, Global } from '@emotion/react';

export const GlobalStyle = (
  <Global
    styles={css`
      /* nanum-gothic-regular - korean */
      @font-face {
        font-family: 'Nanum Gothic';
        font-style: normal;
        font-weight: 400;
        src: local('NanumGothic'),
          url('/fonts/nanum/nanum-gothic-v8-korean-regular.woff2') format('woff2'), /* Super Modern Browsers */
          url('/fonts/nanum/nanum-gothic-v8-korean-regular.woff') format('woff'), /* Modern Browsers */
          url('/fonts/nanum/nanum-gothic-v8-korean-regular.ttf') format('truetype') /* Safari, Android, iOS */
      }
      /* nanum-gothic-700 - korean */
      @font-face {
        font-family: 'Nanum Gothic';
        font-style: normal;
        font-weight: 700;
        src: local('NanumGothic Bold'), local('NanumGothic-Bold'),
          url('/fonts/nanum/nanum-gothic-v8-korean-700.woff2') format('woff2'), /* Super Modern Browsers */
          url('/fonts/nanum/nanum-gothic-v8-korean-700.woff') format('woff'), /* Modern Browsers */
          url('/fonts/nanum/nanum-gothic-v8-korean-700.ttf') format('truetype') /* Safari, Android, iOS */
      }
      /* nanum-gothic-800 - korean */
      @font-face {
        font-family: 'Nanum Gothic';
        font-style: normal;
        font-weight: 800;
        src: local('NanumGothic ExtraBold'), local('NanumGothic-ExtraBold'),
          url('/fonts/nanum/nanum-gothic-v8-korean-800.woff2') format('woff2'), /* Super Modern Browsers */
          url('/fonts/nanum/nanum-gothic-v8-korean-800.woff') format('woff'), /* Modern Browsers */
          url('/fonts/nanum/nanum-gothic-v8-korean-800.ttf') format('truetype') /* Safari, Android, iOS */
      }
      /* open-sans-regular - latin */
      @font-face {
        font-family: 'Open Sans';
        font-style: normal;
        font-weight: 400;
        src: local('Open Sans Regular'), local('OpenSans-Regular'),
          url('/fonts/opensans/open-sans-v15-latin-regular.woff2') format('woff2'), /* Super Modern Browsers */
          url('/fonts/opensans/open-sans-v15-latin-regular.woff') format('woff'), /* Modern Browsers */
          url('/fonts/opensans/open-sans-v15-latin-regular.ttf') format('truetype') /* Safari, Android, iOS */
      }
      /* open-sans-600 - latin */
      @font-face {
        font-family: 'Open Sans';
        font-style: normal;
        font-weight: 600;
        src: local('Open Sans SemiBold'), local('OpenSans-SemiBold'),
          url('/fonts/opensans/open-sans-v15-latin-600.woff2') format('woff2'), /* Super Modern Browsers */
          url('/fonts/opensans/open-sans-v15-latin-600.woff') format('woff'), /* Modern Browsers */
          url('/fonts/opensans/open-sans-v15-latin-600.ttf') format('truetype') /* Safari, Android, iOS */
      }
      /* open-sans-700 - latin */
      @font-face {
        font-family: 'Open Sans';
        font-style: normal;
        font-weight: 700;
        src: local('Open Sans Bold'), local('OpenSans-Bold'),
          url('/fonts/opensans/open-sans-v15-latin-700.woff2') format('woff2'), /* Super Modern Browsers */
          url('/fonts/opensans/open-sans-v15-latin-700.woff') format('woff'), /* Modern Browsers */
          url('/fonts/opensans/open-sans-v15-latin-700.ttf') format('truetype') /* Safari, Android, iOS */
      }
      /* open-sans-800 - latin */
      @font-face {
        font-family: 'Open Sans';
        font-style: normal;
        font-weight: 800;
        src: local('Open Sans ExtraBold'), local('OpenSans-ExtraBold'),
          url('/fonts/opensans/open-sans-v15-latin-800.woff2') format('woff2'), /* Super Modern Browsers */
          url('/fonts/opensans/open-sans-v15-latin-800.woff') format('woff'), /* Modern Browsers */
          url('/fonts/opensans/open-sans-v15-latin-800.ttf') format('truetype') /* Safari, Android, iOS */
      }

      * {
        margin: 0;
        padding: 0;
        text-decoration: none;
        box-sizing: border-box;
        border-collapse: collapse;
        background: transparent;
        list-style: none;
        border: 0;
        font-family: inherit;
        color: inherit;
      }

      html {
        height: 100%;
      }

      body {
        color: var(--text-primary);
        font-family: Nanum Gothic, Open Sans, -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Oxygen,
          Ubuntu, Cantarell, Fira Sans, Droid Sans, Helvetica Neue, sans-serif;
      }

      button {
        cursor: pointer;
        background: none;
        outline: none;
      }

      table {
        border-collapse: collapse;
        border-spacing: 0;
        background-color: transparent;
      }

      caption {
        width: 0;
        height: 0;
        overflow: hidden;
        font-size: 0;
      }
    `}
  />
)