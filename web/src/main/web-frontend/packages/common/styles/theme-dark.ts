const white = {
  default: '#000000',
};

const black = {
  default: '#FFFFFF',
};

const yellow = {
  default: '#ffff00',
};

const purple = {
  default: '#660099',
};

const blue = {
  '50': '#244d92',
  '100': '#306ab2',
  '200': '#367bc4',
  '300': '#3f8cd7',
  '400': '#469ae4',
  '500': '#5aa8e8',
  '600': '#74b8eb',
  '700': '#98ccf2',
  '800': '#bfdff7',
  '900': '#e5f2fb',
};

const green = {
  default: '#41c464',
  '50': '#004c29',
  '100': '#006843',
  '200': '#007850',
  '300': '#00885f',
  '400': '#00956b',
  '500': '#00a67e',
  '600': '#3dcfa8',
  '700': '#74cbb1',
  '800': '#acdfd0',
  '900': '#ddf2ed',
};

const red = {
  '50': '#b20011',
  '100': '#c2101e',
  '200': '#ce1b25',
  '300': '#e0272b',
  '400': '#ef322b',
  '500': '#eb4747',
  '600': '#e26b6b',
  '700': '#ed9494',
  '800': '#fecacf',
  '900': '#ffeaed',
}

const orange = {
  '50': '#ea5001',
  '100': '#f36c01',
  '200': '#f97c01',
  '300': '#ff8c00',
  '400': '#ff9800',
  '500': '#ffa726',
  '600': '#ffb84d',
  '700': '#ffcc80',
  '800': '#ffe0b2',
  '900': '#fff3e0',
}

const grey = {
  '50': '#131313',
  '100': '#1f1f1f',
  '200': '#2c2c2c',
  '300': '#393939',
  '400': '#8b8b8b',
  '500': '#acacac',
  '600': '#d1d1d1',
  '700': '#e3e3e3',
  '800': '#eeeeee',
  '900': '#f7f7f7',
};

const blueGrey = {
  '50': '#282b32',
  '100': '#4a4d55',
  '200': '#696d75',
  '300': '#7e828a',
  '400': '#a8acb5',
  '500': '#c6cad3',
  '600': '#e7ebf5',
  '700': '#eef2fc',
  '800': '#f3f7ff',
  '900': '#f8fcff',
};

const navy = {
  '50': '#1d2b41',
  '100': '#303f59',
  '200': '#3f516f',
  '300': '#506486',
  '400': '#5e7297',
  '500': '#7587a9',
  '600': '#8b9cbc',
  '700': '#aab8d4',
  '800': '#c7d5ed',
  '900': '#e7edff',
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