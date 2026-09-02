import { themeColors } from './tailwind.colors.js';
import tailwindcssAnimate from 'tailwindcss-animate';
import tailwindScrollbarHide from 'tailwind-scrollbar-hide';
//
/** @type {import('tailwindcss').Config} */
export default {
  darkMode: ['class'],
  content: ['./src/**/*.{ts,tsx}'],
  safelist: ['flex-none'],
  theme: {
    container: {
      center: true,
      padding: '2rem',
      screens: {
        '2xl': '1400px',
      },
    },
    extend: {
      blur: {
        xs: '2px',
      },
      fontSize: {
        xxs: '0.625rem',
      },
      maxWidth: {
        '8xl': '90rem',
      },
      spacing: {
        90: '22.5rem',
        160: '40rem',
      },
      colors: themeColors,
      borderWidth: {
        1: '1px',
      },
      borderRadius: {
        lg: 'var(--ui-radius)',
        md: 'calc(var(--ui-radius) - 2px)',
        sm: 'calc(var(--ui-radius) - 4px)',
      },
      keyframes: {
        'accordion-down': {
          from: { height: 0 },
          to: { height: 'var(--radix-accordion-content-height)' },
        },
        'accordion-up': {
          from: { height: 'var(--radix-accordion-content-height)' },
          to: { height: 0 },
        },
        blink: {
          '0%, 100%': { opacity: 1 },
          '50%': { opacity: 0 },
        },
        blinkWithBgColor: {
          '0%, 100%': { opacity: 1 },
          '50%': {
            backgroundColor: 'hsl(var(--ui-primary))',
            color: 'hsl(var(--ui-primary-foreground))',
          },
        },
      },
      animation: {
        'accordion-down': 'accordion-down 0.2s ease-out',
        'accordion-up': 'accordion-up 0.2s ease-out',
        blink: 'blink 1s infinite',
        blinkWithBgColor: 'blinkWithBgColor 1s infinite',
      },
    },
  },
  // @tailwindcss/container-queries 는 v4 에 내장되어 플러그인이 필요 없다.
  plugins: [tailwindcssAnimate, tailwindScrollbarHide],
};
