import uiTailwindConfig from '@pinpoint-fe/ui/tailwind.config.js';
import rdpTailwindConfig from '@pinpoint-fe/datetime-picker/tailwind.config.js';

/** @type {import('tailwindcss').Config} */
export default {
  presets: [rdpTailwindConfig, uiTailwindConfig],
  content: [
    './index.html',
    './src/**/*.{ts,tsx}',
    '../../packages/ui/src/**/*.{ts,tsx}',
    '../../packages/datetime-picker/src/**/*.{ts,tsx}',
  ],
};
