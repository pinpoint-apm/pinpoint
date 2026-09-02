import colors from 'tailwindcss/colors';

/**
 * 테마 색 정의. `tailwind.config.js`(빌드)와 `src/constants/theme.ts`(런타임)가 함께 쓴다.
 *
 * 설정 파일에서 떼어낸 이유: 런타임 코드가 설정 파일을 임포트하면 플러그인(`tailwindcss-animate`
 * 등)까지 앱·테스트 모듈 그래프로 끌려 들어온다. 색만 필요하므로 색만 나눠 둔다.
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
