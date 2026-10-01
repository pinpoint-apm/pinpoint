import uiTailwindConfig from '@pinpoint-fe/ui/tailwind.config.js';
import rdpTailwindConfig from '@pinpoint-fe/datetime-picker/tailwind.config.js';

// datetime-picker 는 `rdp` prefix 로 자기 CSS 를 따로 만든다. 그 설정을 preset 으로 그대로 쓰면
// prefix 가 앱 전체에 걸리므로, 앱이 쓰던 테마 토큰만 가져온다.
const { prefix, content, ...rdpThemePreset } = rdpTailwindConfig;

/** @type {import('tailwindcss').Config} */
export default {
  presets: [rdpThemePreset, uiTailwindConfig],
  content: ['./index.html', './src/**/*.{ts,tsx}', '../../packages/ui/src/**/*.{ts,tsx}'],
};
