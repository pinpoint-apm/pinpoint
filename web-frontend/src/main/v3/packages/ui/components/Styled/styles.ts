import { css } from '@emotion/react';

export const StyleFlexVCentered = css`
  display: flex;
  align-items: center;
`

export const StyleFlexVHCentered = css`
  display: flex;
  align-items: center;
  justify-content: center;
`

export const StyleSpin = css`
  animation: spin 1s infinite linear;

  @keyframes spin {
    0% {
      -webkit-transform: rotate(0deg);
              transform: rotate(0deg);
    }
    100% {
      -webkit-transform: rotate(359deg);
              transform: rotate(359deg);
    }
  }

  @keyframes icon-spin {
    0% {
      -webkit-transform: rotate(0deg);
              transform: rotate(0deg);
    }
    100% {
      -webkit-transform: rotate(359deg);
              transform: rotate(359deg);
    }
  }
`