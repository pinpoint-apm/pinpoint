/**
 * `oklch()` 표기를 hex 로 바꾼다.
 *
 * Tailwind 4 의 기본 팔레트가 색을 `oklch()` 로 내놓는데, echarts 의 색 파서(zrender)는 그 표기를
 * 읽지 못해 `undefined` 를 돌려준다 — 차트 색이 에러 없이 사라진다. JS 로 넘어오는 팔레트 값은
 * 여기서 hex 로 바꿔 둔다.
 *
 * 변환 경로는 표준 그대로다: OkLCh → OkLab → LMS → 선형 sRGB → 감마 보정 sRGB.
 * 색역을 벗어나는 값은 채널별로 잘라낸다(Tailwind 팔레트는 sRGB 안에 있어 실제로는 거의 없다).
 */

const clamp = (value: number) => Math.min(1, Math.max(0, value));

const toGamma = (channel: number) =>
  channel <= 0.0031308 ? 12.92 * channel : 1.055 * Math.pow(channel, 1 / 2.4) - 0.055;

const toHexChannel = (channel: number) =>
  Math.round(clamp(channel) * 255)
    .toString(16)
    .padStart(2, '0');

/**
 * `oklch(54.6% 0.245 262.881)` 처럼 공백으로 구분된 세 값을 읽는다. 알파(`/ 0.5`)는 hex 로
 * 표현하지 않으므로 무시한다 — Tailwind 기본 팔레트에는 알파가 붙지 않는다.
 */
const parseOklch = (value: string) => {
  const inner = value
    .trim()
    .replace(/^oklch\(/, '')
    .replace(/\)$/, '')
    .split('/')[0];
  const parts = inner.trim().split(/[\s,]+/);

  if (parts.length < 3) {
    return null;
  }

  // CSS Color 4 는 빠진 성분을 `none` 으로 쓴다(무채색의 채도·색상). 계산에서는 0 과 같다.
  const toNumber = (part: string) => (part === 'none' ? 0 : Number.parseFloat(part));

  const lightness = toNumber(parts[0]);
  const chroma = toNumber(parts[1]);
  const hue = toNumber(parts[2]);

  if ([lightness, chroma, hue].some(Number.isNaN)) {
    return null;
  }

  return {
    // 밝기는 0~1 이지만 퍼센트로도 쓴다.
    lightness: parts[0].includes('%') ? lightness / 100 : lightness,
    chroma,
    hue,
  };
};

export const oklchToHex = (value: string) => {
  const parsed = parseOklch(value);

  if (!parsed) {
    return value;
  }

  const { lightness, chroma, hue } = parsed;
  const hueRadians = (hue * Math.PI) / 180;
  const a = chroma * Math.cos(hueRadians);
  const b = chroma * Math.sin(hueRadians);

  const l = (lightness + 0.3963377774 * a + 0.2158037573 * b) ** 3;
  const m = (lightness - 0.1055613458 * a - 0.0638541728 * b) ** 3;
  const s = (lightness - 0.0894841775 * a - 1.291485548 * b) ** 3;

  const red = toGamma(4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s);
  const green = toGamma(-1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s);
  const blue = toGamma(-0.0041960863 * l - 0.7034186147 * m + 1.707614701 * s);

  return `#${toHexChannel(red)}${toHexChannel(green)}${toHexChannel(blue)}`;
};
