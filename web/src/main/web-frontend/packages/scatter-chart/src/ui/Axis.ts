import { AXIS_DEFAULT_FORMAT } from "../constants/options";
import { AXIS_DEFAULT_TICK_COUNT } from "../constants/ui";
import { TickOption, FormatType } from "../types";
import { Layer, LayerProps } from "./Layer";

export interface AxisProps extends LayerProps {
  min?: number;
  max?: number;
  padding?: {
    top?: number;
    bottom?: number;
    left?: number;
    right?: number;
  };
  innerPadding?: number;
  tickCount?: number;
  format?: FormatType<number>
  tickOption?: TickOption;
};

export class Axis extends Layer {
  min;
  max;
  padding;
  tickOption;

  constructor({
    min = 0,
    max = 1,
    padding,
    tickCount,
    format,
    tickOption,
    ...props
  }: AxisProps = {}) {
    super(props);

    this.min = min;
    this.max = max;
    this.padding = { top: 0, bottom:0, left: 0, right: 0, ...padding };
    this.tickOption = { ...{ count: AXIS_DEFAULT_TICK_COUNT, format: AXIS_DEFAULT_FORMAT }, ...tickOption }
  }

  setMinMax(min: number, max: number) {
    this.min = min;
    this.max = max;
    return this;
  }

  setSize(...args: Parameters<Layer['setSize']>) {
    super.setSize(...args);
    return this;
  }

  setTickCount(count: number) {
    this.tickOption = { ...this.tickOption, count };
    return this;
  }
}