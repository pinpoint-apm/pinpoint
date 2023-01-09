export type ScatterDataType =  {
  x: number;
  y: number;
  type?: string;
  hidden?: boolean;
}

export type Coord = {
  x: number;
  y: number;
}

export type Padding = {
  top?: number;
  bottom?: number;
  left?: number;
  right?: number;
};

export type FormatType<T> = (value: T) => string | number;

export interface AxisOption {
  min: number;
  max: number;
  padding?: number;
  tick?: TickOption;
}

export interface TickOption {
  count?: number;
  format?: FormatType<number>;
}

export interface DataOption {
  type: string;
  color?: string;
  priority?: number;
}

export interface LegendOption {
  disableCheckbox?: boolean;
  formatLabel?: FormatType<string>;
  formatValue?: FormatType<number>;
}

export interface PointOption {
  radius?: number;
}
