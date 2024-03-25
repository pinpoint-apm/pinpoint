import { AXIS_DEFAULT_TICK_COUNT } from '../constants/ui';
import { AxisOption } from '../types/types';

export const getDevicePicelRatio = () => {
  const dpr = window?.devicePixelRatio || 2;

  return dpr;
};

export const getTickTexts = ({ min, max, tick }: AxisOption) => {
  const tickCount = tick?.count || AXIS_DEFAULT_TICK_COUNT;
  const gap = (max - min) / (tickCount - 1);
  return [...Array(tickCount)].map((_, i) => {
    const value = min + gap * i;
    const result = tick?.format?.(value) ?? value;

    return `${result}`;
  });
};

export const getLongestText = (texts: string[], measurer: (t: string) => number) => {
  const text = texts.reduce((prev: string, curr: string) => {
    const prevWidth = measurer(prev);
    const currWidth = measurer(curr);

    return prevWidth > currWidth ? prev : curr;
  }, '0');

  return measurer(text);
};

export const getSafeDrawImageArgs = (
  canvas: HTMLCanvasElement,
  sx: number,
  sy: number,
  sw: number,
  sh: number,
  dx: number,
  dy: number,
  dw: number,
  dh: number,
): [HTMLCanvasElement, number, number, number, number, number, number, number, number] => {
  const { width, height } = canvas;

  if (sw < 0) {
    sx += sw;
    sw = Math.abs(sw);
  }
  if (sh < 0) {
    sy += sh;
    sh = Math.abs(sh);
  }
  if (dw < 0) {
    dx += dw;
    dw = Math.abs(dw);
  }
  if (dh < 0) {
    dy += dh;
    dh = Math.abs(dh);
  }
  const x1 = Math.max(sx, 0);
  const x2 = Math.min(sx + sw, width);
  const y1 = Math.max(sy, 0);
  const y2 = Math.min(sy + sh, height);
  const w_ratio = dw / sw;
  const h_ratio = dh / sh;

  return [
    canvas,
    x1,
    y1,
    x2 - x1,
    y2 - y1,
    sx < 0 ? dx - sx * w_ratio : dx,
    sy < 0 ? dy - sy * h_ratio : dy,
    (x2 - x1) * w_ratio,
    (y2 - y1) * h_ratio,
  ];
};
