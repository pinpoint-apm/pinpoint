import SL from 'rc-slider';
import type { SliderProps as SLProps } from 'rc-slider';
import 'rc-slider/assets/index.css';

export interface SliderProps extends SLProps {}

export const Slider = ({ ...props }: SliderProps) => {
  return <SL {...props} />;
};
