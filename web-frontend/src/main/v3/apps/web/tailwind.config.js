import uiTailwindConfig from '@pinpoint-fe/ui/tailwind.config.js';

/** @type {import('tailwindcss').Config} */
export default {
  ...uiTailwindConfig,
  content: ['./index.html', './src/**/*.{ts,tsx}'],
};
