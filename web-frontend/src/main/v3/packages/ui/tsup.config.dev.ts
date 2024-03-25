import { defineConfig } from 'tsup';

export default defineConfig({
  entry: {
    'pinpoint-fe-common-ui': 'src/index.ts',
  },
  splitting: true,
  dts: true,
  format: ['cjs', 'esm'],
  external: [
    '@pinpoint-fe/atoms',
    '@pinpoint-fe/hooks',
    'react',
    'react-dom',
    'react-router-dom',
    'jotai',
    'i18next',
    'react-i18next',
    'tailwindcss',
    'tailwindcss-animate',
    'tailwind-scrollbar-hide',
  ],
  outExtension: ({ format }) => {
    if (format === 'cjs') {
      return {
        js: `.umd.cjs`,
      };
    } else {
      return {
        js: `.es.js`,
      };
    }
  },
});
