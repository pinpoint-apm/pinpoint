export type DeepNonNullable<T> = {
  [P in keyof T]-?: NonNullable<T[P]>;
};

export type ScatterDataType = {
  x: number;
  y: number;
  type?: string;
  hidden?: boolean;
};

type Shape = 'point' | 'area';

type DataStyle = {
  shape: Shape;
  color: string;
  legend: string;
  radius: number;
};

export type DataStyleMap = {
  [key: string]: DataStyle;
};

export type Coord = {
  x: number;
  y: number;
};

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
  strokeColor?: string;
  tick?: TickOption;
}

export interface TickOption {
  font?: CanvasTextDrawingStyles['font'];
  color?: string;
  strokeColor?: string;
  width?: number;
  count?: number;
  format?: FormatType<number | string>;
  padding?: Padding;
}

export interface DataOption {
  type: string;
  color?: string;
  priority?: number;
  radius?: number;
  opacity?: number;
  shape?: Shape;
}

export interface LegendOption {
  disableCheckbox?: boolean;
  formatLabel?: FormatType<string>;
  formatValue?: FormatType<number>;
  hidden?: boolean;
}

export interface PointOption {
  radius?: number;
  opacity?: number;
}

export interface GuideOption {
  hidden?: boolean;
  color?: string;
  strokeColor?: string;
  backgroundColor?: string;
  drag?: DragOption;
  font?: CanvasTextDrawingStyles['font'];
}

export interface DragOption {
  strokeColor?: string;
  backgroundColor?: string;
}

export interface BackgroundOption {
  color?: string;
}

export interface GridOption {
  hidden?: boolean;
  strokeColor?: string;
}

export interface RenderOption {
  append?: boolean;
  drawOutOfRange?: boolean;
}
