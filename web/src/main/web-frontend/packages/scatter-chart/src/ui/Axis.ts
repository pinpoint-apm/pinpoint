import { AXIS_DEFAULT_FORMAT } from "../constants/options";
import { AXIS_DEFAULT_TICK_COUNT, CONTAINER_PADDING } from "../constants/ui";
import { TickOption, FormatType, Padding, AxisOption } from "../types";
import { Layer, LayerProps } from "./Layer";

export interface AxisProps extends LayerProps {
  axisOption?: AxisOption;
  padding?: Padding;
};

export class Axis extends Layer {
  min;
  max;
  padding;
  tickOption;

  constructor({
    axisOption,
    padding,
    ...props
  }: AxisProps) {
    super(props);
    this.min = axisOption?.min || 0;
    this.max = axisOption?.max || 1;
    this.padding = { ...CONTAINER_PADDING, ...padding };
    this.tickOption = { ...{ count: AXIS_DEFAULT_TICK_COUNT, format: AXIS_DEFAULT_FORMAT }, ...axisOption?.tick }
  }

  setOptions(options: {
    min?: number,
    max?: number,
    padding?: Padding,
  }) {
    const { min, max, padding } = options;
    this.min = min || this.min;
    this.max = max || this.max;
    this.padding = { ...CONTAINER_PADDING, ...padding };
    return this;
  }

  setSize(...args: Parameters<Layer['setSize']>) {
    super.setSize(...args);
    return this;
  }
}