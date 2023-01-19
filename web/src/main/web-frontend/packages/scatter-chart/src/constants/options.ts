import { AxisOption, BackgroundOption, DataOption, GridOption, GuideOption, LegendOption, PointOption } from "../types/types";
import { AXIS_DEFAULT_TICK_COUNT, AXIS_TICK_WIDTH, POINT_RADIUS } from "./ui";

export const AXIS_DEFAULT_FORMAT = ((value: number | string) => value);

export const defaultOption = {};

export const defaultDataOption: DataOption[] = []

export const defaultLegendOption: LegendOption = {
  formatLabel: (label) => label,
  formatValue: (value) => value,
}

export const defaultAxisOption: {
  x: AxisOption,
  y: AxisOption,
} = {
  x: {
    min: 0,
    max: 1,
    tick: {
      color: 'black',
      strokeColor: 'black',
      width: AXIS_TICK_WIDTH,
      count: AXIS_DEFAULT_TICK_COUNT,
      format: AXIS_DEFAULT_FORMAT,
    }
  },
  y: {
    min: 0,
    max: 1, 
    tick: {
      color: 'black',
      strokeColor: 'black',
      width: AXIS_TICK_WIDTH,
      count: AXIS_DEFAULT_TICK_COUNT,
      format: AXIS_DEFAULT_FORMAT,
    }
  }
}

export const defaultPointOption: PointOption = {
  radius: POINT_RADIUS,
}

export const defaultGuideOption: GuideOption = {
  color: 'white',
  strokeColor: 'black',
  backgroundColor: 'black',
  drag: {
    strokeColor: '#469ae4',
    backgroundColor: 'rgba(225,225,225,0.4)',
  }
}

export const defaultBackgroundOption: BackgroundOption = {
  color: 'white',
}

export const defaultGridOption: GridOption = {
  strokeColor: '#d1d1d1',
}