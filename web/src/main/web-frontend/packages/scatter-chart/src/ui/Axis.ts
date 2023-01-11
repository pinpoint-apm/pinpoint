import merge from "lodash.merge";
import { AXIS_DEFAULT_FORMAT } from "../constants/options";
import { AXIS_DEFAULT_TICK_COUNT, AXIS_INNER_PADDING, CONTAINER_PADDING } from "../constants/ui";
import { Padding, AxisOption } from "../types/types";
import { Layer, LayerProps } from "./Layer";

export interface AxisProps extends LayerProps {
  axisOption?: AxisOption;
  padding?: Padding;
};

export class Axis extends Layer {
  min: AxisOption['min'];
  max: AxisOption['max'];
  innerPadding: NonNullable<AxisOption['padding']>;
  tick: AxisOption['tick'];
  padding: DeepNonNullable<Padding>;

  constructor({
    axisOption,
    padding,
    ...props
  }: AxisProps) {
    super(props);
    this.min = axisOption?.min || 0;
    this.max = axisOption?.max || 1;
    this.innerPadding = axisOption?.padding || AXIS_INNER_PADDING;
    this.tick = { ...{ count: AXIS_DEFAULT_TICK_COUNT, format: AXIS_DEFAULT_FORMAT }, ...axisOption?.tick };
    this.padding = { ...CONTAINER_PADDING, ...padding };
  }

  setAxisOption(option: AxisOption) {
    this.min = option?.min || this.min;
    this.max = option?.max || this.max;
    this.innerPadding = option?.padding || this.innerPadding;
    this.tick = { ...this.tick, ...option?.tick };
    return this;
  }

  setPadding(padding: Padding) {
    this.padding = { ...CONTAINER_PADDING, ...padding };
    return this;
  }

  setSize(...args: Parameters<Layer['setSize']>) {
    super.setSize(...args);
    return this;
  }
}