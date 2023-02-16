import { AXIS_DEFAULT_FORMAT } from "../constants/options";
import { AXIS_DEFAULT_TICK_COUNT, AXIS_INNER_PADDING, COLOR_STROKE, CONTAINER_PADDING } from "../constants/ui";
import { Padding, AxisOption, DeepNonNullable } from "../types/types";
import { Layer, LayerProps } from "./Layer";

export interface AxisProps extends LayerProps {
  option?: AxisOption;
  padding?: DeepNonNullable<Padding>;
};

export class Axis extends Layer {
  min: AxisOption['min'];
  max: AxisOption['max'];
  innerPadding: NonNullable<AxisOption['padding']>;
  tick: AxisOption['tick'];
  strokeColor: AxisOption['strokeColor'];
  padding: DeepNonNullable<Padding>;

  constructor({
    option,
    padding,
    ...props
  }: AxisProps) {
    super(props);
    this.min = option?.min ?? 0;
    this.max = option?.max ?? 1;
    this.innerPadding = option?.padding ?? AXIS_INNER_PADDING;
    this.tick = { ...{ count: AXIS_DEFAULT_TICK_COUNT, format: AXIS_DEFAULT_FORMAT }, ...option?.tick };
    this.padding = { ...CONTAINER_PADDING, ...padding };
    this.strokeColor = option?.strokeColor || COLOR_STROKE;
    option?.tick?.font && (this.context.font = option?.tick?.font);
  }

  public setOption(option: AxisOption) {
    this.min = option?.min ?? this.min;
    this.max = option?.max ?? this.max;
    this.innerPadding = option?.padding ?? this.innerPadding;
    this.strokeColor = option?.strokeColor || this.strokeColor;
    this.tick = { ...this.tick, ...option?.tick };
    const font = option?.tick?.font || this.tick.font
    font && (this.context.font = font);
    return this;
  }

  public setPadding(padding: Padding) {
    this.padding = { ...this.padding, ...padding };
    return this;
  }

  public setSize(...args: Parameters<Layer['setSize']>) {
    super.setSize(...args);
    return this;
  }
}