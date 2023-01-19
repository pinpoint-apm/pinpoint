import { AXIS_DEFAULT_TICK_COUNT } from "../constants/ui";
import { AxisOption } from "../types/types";

export const getDevicePicelRatio = () => {
  const dpr = window?.devicePixelRatio || 2;

  return dpr;
}

export const getTickTexts = ({ min, max, tick }: AxisOption) => {
  const tickCount = tick?.count || AXIS_DEFAULT_TICK_COUNT;
  const gap = (max - min) / (tickCount - 1);
  return [...Array(tickCount)].map((_, i) => {
    const value = min + gap * i;
    const result = tick?.format?.(value) || value;

    return `${result}`;
  });
}

export const getLongestText = (texts: string[], measurer: (t: string) => number) => {
  const text = texts.reduce((prev: string, curr: string) => {
    const prevWidth = measurer(prev);
    const currWidth = measurer(curr);

    return prevWidth > currWidth ? prev : curr;
  }, '0');

  return measurer(text);
}
