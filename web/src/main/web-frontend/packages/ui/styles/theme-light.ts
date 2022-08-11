const white = {
  default: '#FFFFFF',
};

const black = {
  default: '#000000',
};

const yellow = {
  default: '#ffff00',
};

const purple = {
  default: '#660099',
};

const blue = {
  '50': '#e5f2fb',
  '100': '#bfdff7',
  '200': '#98ccf2',
  '300': '#74b8eb',
  '400': '#5aa8e8',
  '500': '#469ae4',
  '600': '#3f8cd7',
  '700': '#367bc4',
  '800': '#306ab2',
  '900': '#244d92',
};

const green = {
  default: '#41c464',
  '50': '#ddf2ed',
  '100': '#acdfd0',
  '200': '#74cbb1',
  '300': '#3dcfa8',
  '400': '#00a67e',
  '500': '#00956b',
  '600': '#00885f',
  '700': '#007850',
  '800': '#006843',
  '900': '#004c29',
};

const red = {
  '50': '#ffeaed',
  '100': '#fecacf',
  '200': '#ed9494',
  '300': '#e26b6b',
  '400': '#eb4747',
  '500': '#ef322b',
  '600': '#e0272b',
  '700': '#ce1b25',
  '800': '#c2101e',
  '900': '#b20011',
}

const orange = {
  '50': '#fff3e0',
  '100': '#ffe0b2',
  '200': '#ffcc80',
  '300': '#ffb84d',
  '400': '#ffa726',
  '500': '#ff9800',
  '600': '#ff8c00',
  '700': '#f97c01',
  '800': '#f36c01',
  '900': '#ea5001',
}

const grey = {
  '50': '#f7f7f7',
  '100': '#eeeeee',
  '200': '#e3e3e3',
  '300': '#d1d1d1',
  '400': '#acacac',
  '500': '#8b8b8b',
  '600': '#646464',
  '700': '#515151',
  '800': '#333333',
  '900': '#131313',
};

const blueGrey = {
  '50': '#f8fcff',
  '100': '#f3f7ff',
  '200': '#eef2fc',
  '300': '#e7ebf5',
  '400': '#c6cad3',
  '500': '#a8acb5',
  '600': '#7e828a',
  '700': '#696d75',
  '800': '#4a4d55',
  '900': '#282b32',
};

const navy = {
  '50': '#e7edff',
  '100': '#c7d5ed',
  '200': '#aab8d4',
  '300': '#8b9cbc',
  '400': '#7587a9',
  '500': '#5e7297',
  '600': '#506486',
  '700': '#3f516f',
  '800': '#303f59',
  '900': '#1d2b41',
};

const theme = {
  primary: blue[500],
  secondary: green[300],
  secondaryLighter: green[100],
  background: {
    default: white.default,
    primary: grey[50],
    primaryDarker: grey[100],
    primaryDarkest: grey[200],
    danger: red[50],
    rowDanger: red[50],
    hoverPrimary: blue[50],
    hoverPrimaryDarker: blue[100],
    focusPrimary: blue[50],
    hoverSecondary: green[50],
    hoverDefault: grey[100],
    knockout: black.default,
    disable: grey[300],
    layer: white.default,
    blind: 'rgba(226, 226, 226, 0.8)',
    blindGradient: 'linear-gradient(135deg, rgba(226,226,226,0.7) 0%, rgba(219,219,219,0.7) 43%, rgba(209,209,209,0.7) 51%, rgba(254,254,254,0.7) 100%)',
  },
  border: {
    default: black.default,
    knockout: white.default,
    primary: grey[200],
    primaryLighter: grey[100],
    primaryDarker: grey[300],
    primaryDarkest: grey[500],
  },
  color: {
    default: black.default,
    primary: grey[800],
    primaryLighter: grey[700],
    primaryLightest: grey[600],
    secondary: grey[500],
    secondaryLighter: grey[400],
    secondaryLightest: grey[300],
    knockout: white.default,
    shadow: grey[100],
    disable: grey[300],
  }
}
export default theme; 