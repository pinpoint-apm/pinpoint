/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  prefix: 'rdp',
  theme: {
    extend: {
      // 강조색은 CSS 변수로 열어 둔다. 기본값은 원본(rich-datetime-picker)과 같고, pinpoint 는
      // packages/ui 의 datetime-picker.css 에서 이 변수들을 테마색(--ui-*)으로 채운다.
      colors: {
        primary: 'var(--rdp-primary, #9338FF)',
        stateRed: 'var(--rdp-danger, #F84302)',
        rgba1: 'rgba(0, 0, 0, 0.1)',
        rgba15: 'rgba(0, 0, 0, 0.15)',
        rgba2: 'rgba(0, 0, 0, 0.2)',
        rgba5: 'rgba(0, 0, 0, 0.5)',
        rgba8: 'rgba(0, 0, 0, 0.8)',
        rgba06: 'rgba(0, 0, 0, 0.06)',
        rgbaPrimary: 'var(--rdp-primary-soft, rgba(147, 56, 255, 0.10))',
      },
      boxShadow: {
        box1: '2px 2px 12px 0px rgba(0, 0, 0, 0.20)',
      },
      spacing: {
        7.5: '1.875rem',
        12.5: '3.125rem',
        13: '3.25rem',
        70: '17.5rem',
        85: '21.25rem',
      },
    },
  },
  plugins: [],
};
