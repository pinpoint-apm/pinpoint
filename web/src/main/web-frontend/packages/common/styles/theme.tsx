import React from 'react';
// import { CreateStyled } from '@emotion/styled';
// import _styled from '@emotion/styled';
import { ThemeProvider } from '@emotion/react';

type Theme = {
  primary: string,
  secondary: string,
  secondaryLighter: string,
  background: {
    default: string,
    primary: string,
    primaryDarker: string,
    primaryDarkest: string,
    danger: string,
    rowDanger: string,
    hoverPrimary: string,
    hoverPrimaryDarker: string,
    focusPrimary: string,
    hoverSecondary: string,
    hoverDefault: string,
    knockout: string,
    disable: string,
    layer: string,
    blind: string,
    blindGradient: string,
  },
  border: {
    default: string,
    knockout: string,
    primary: string,
    primaryLighter: string,
    primaryDarker: string,
    primaryDarkest: string,
  },
  color: {
    default: string,
    primary: string,
    primaryLighter: string,
    primaryLightest: string,
    secondary: string,
    secondaryLighter: string,
    secondaryLightest: string,
    knockout: string,
    shadow: string,
    disable: string,
  }
};

export interface RootThemeProviderProps {
  theme: Theme;
  children: React.ReactNode;
};

export const RootThemeProvider: React.FC<RootThemeProviderProps> = ({
  theme,
  children
}) => {
  return (
    <ThemeProvider theme={theme}>
      {children}
    </ThemeProvider>
  )
};
