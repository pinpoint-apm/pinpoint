/** @type {import('tailwindcss').Config} */
export default {
  relative: true,
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  // prefix: '',
  theme: {
    extend: {
      colors: {
        stateRed: '#F84302',
        rgba1: 'rgba(0, 0, 0, 0.1)',
        rgba2: 'rgba(0, 0, 0, 0.2)',
        rgba5: 'rgba(0, 0, 0, 0.5)',
        rgba8: 'rgba(0, 0, 0, 0.8)',
        rgba06: 'rgba(0, 0, 0, 0.06)',
        rgbaPrimary: 'rgba(147, 56, 255, 0.10)',
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
