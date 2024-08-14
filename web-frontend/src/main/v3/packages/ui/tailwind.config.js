import colors from 'tailwindcss/colors';
import tailwindcssAnimate from 'tailwindcss-animate';
import tailwindScrollbarHide from 'tailwind-scrollbar-hide';
import tailwindContainerQueries from '@tailwindcss/container-queries';
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
      colors: {
        border: 'hsl(var(--ui-border))',
        input: 'hsl(var(--ui-input))',
        ring: 'hsl(var(--ui-ring))',
        background: 'hsl(var(--ui-background))',
        foreground: 'hsl(var(--ui-foreground))',
        primary: {
          DEFAULT: 'hsl(var(--ui-primary))',
          foreground: 'hsl(var(--ui-primary-foreground))',
        },
        secondary: {
          DEFAULT: 'hsl(var(--ui-secondary))',
          foreground: 'hsl(var(--ui-secondary-foreground))',
        },
        destructive: {
          DEFAULT: 'hsl(var(--ui-destructive))',
          foreground: 'hsl(var(--ui-destructive-foreground))',
        },
        muted: {
          DEFAULT: 'hsl(var(--ui-muted))',
          foreground: 'hsl(var(--ui-muted-foreground))',
        },
        accent: {
          DEFAULT: 'hsl(var(--ui-accent))',
          foreground: 'hsl(var(--ui-accent-foreground))',
        },
        popover: {
          DEFAULT: 'hsl(var(--ui-popover))',
          foreground: 'hsl(var(--ui-popover-foreground))',
        },
        card: {
          DEFAULT: 'hsl(var(--ui-card))',
          foreground: 'hsl(var(--ui-card-foreground))',
        },
        // pinpoint
        'status-success': colors.emerald[400],
        'status-good': 'hsl(var(--ui-primary))',
        'status-warn': colors.orange[500],
        'status-fail': colors.red[500],
        fast: colors.emerald[300],
        normal: colors.blue[300],
        delay: colors.orange[300],
        slow: colors.orange[500],
        error: colors.red[500],
      },
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
      },
      animation: {
        'accordion-down': 'accordion-down 0.2s ease-out',
        'accordion-up': 'accordion-up 0.2s ease-out',
      },
    },
  },
  plugins: [tailwindcssAnimate, tailwindScrollbarHide, tailwindContainerQueries],
};
