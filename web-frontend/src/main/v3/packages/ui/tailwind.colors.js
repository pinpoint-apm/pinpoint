import colors from 'tailwindcss/colors';

/**
 * 테마 색 정의. `src/constants/theme.ts`(런타임)가 읽는다.
 *
 * 유틸리티 클래스(`bg-primary` 등)는 `src/theme.css` 의 `--color-*` 로 만들어진다. 두 곳의 값은
 * 같아야 한다 — 색을 바꾸면 둘 다 고치고, `src/lib/tailwindTheme.test.ts` 가 어긋남을 잡는다.
 */
export const themeColors = {
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
};
