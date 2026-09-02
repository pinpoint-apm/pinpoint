import defaultColors from 'tailwindcss/colors';
import defaultTheme from 'tailwindcss/defaultTheme';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore -- 설정과 공유하는 색 정의(JS)
import { themeColors } from '../../tailwind.colors.js';
import { oklchToHex } from '../lib/oklch';

/**
 * Tailwind 4 에는 `resolveConfig` 가 없다. JS 쪽에서 실제로 쓰는 것은 색과 브레이크포인트
 * 둘뿐이라, 기본 테마와 우리 색 정의를 직접 합친다.
 *
 * 색 정의만 따로 임포트하는 이유: `tailwind.config.js` 를 그대로 임포트하면 플러그인까지 앱·테스트
 * 모듈 그래프로 끌려 들어온다(그 중 일부는 ESM 전용이라 jest 가 읽지 못한다).
 */

/**
 * 색 중 **JS 에서 읽는** 것들. 색 정의가 JS 라 타입을 얻을 수 없어 여기에 적는다.
 * CSS 변수로 된 색(`border`, `primary` 등)은 유틸리티 클래스로만 쓰이므로 뺀다.
 */
type PinpointColors = Record<
  | 'status-success'
  | 'status-good'
  | 'status-warn'
  | 'status-fail'
  | 'fast'
  | 'normal'
  | 'delay'
  | 'slow'
  | 'error',
  string
>;

/**
 * Tailwind 4 의 기본 팔레트는 색을 `oklch()` 로 내놓는데, echarts(zrender)의 색 파서는 그 표기를
 * 읽지 못해 차트 색이 에러 없이 사라진다. JS 로 넘어오는 값은 hex 로 바꿔 둔다.
 * (CSS 유틸리티는 계속 `oklch()` 를 쓰므로 같은 팔레트를 가리킨다.)
 */
const toHexDeep = (value: unknown): unknown => {
  if (typeof value === 'string') {
    return value.startsWith('oklch(') ? oklchToHex(value) : value;
  }
  if (value && typeof value === 'object') {
    return Object.fromEntries(
      Object.entries(value as Record<string, unknown>).map(([key, v]) => [key, toHexDeep(v)]),
    );
  }
  return value;
};

export const colors = toHexDeep({
  ...defaultColors,
  ...themeColors,
}) as typeof defaultColors & PinpointColors;

/** Tailwind 의 브레이크포인트 원문 값. 미디어 쿼리 문자열에 그대로 쓴다. */
export const screens = defaultTheme.screens;

/**
 * 길이 문자열을 픽셀 숫자로. Tailwind 4 는 브레이크포인트를 `rem` 으로 정의하므로
 * (3 까지는 `px`) 숫자만 떼어내면 640 이 아니라 40 이 나온다.
 */
export const toPixels = (length: string) => {
  const value = Number.parseFloat(length);

  if (Number.isNaN(value)) {
    return 0;
  }
  return length.trim().endsWith('rem') ? value * 16 : value;
};

export const screenPixels = Object.fromEntries(
  Object.entries(screens).map(([key, value]) => [key, toPixels(String(value))]),
) as Record<keyof typeof screens, number>;
