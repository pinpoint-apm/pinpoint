import { DataOption, LegendOption, PointOption } from "../types";
import { AXIS_DEFAULT_TICK_COUNT, POINT_RADIUS } from "./ui";

export const AXIS_DEFAULT_FORMAT = ((value: number) => value);

export const defaultOption = {};

export const defaultDataOption: DataOption[] = []

export const defaultLegendOption: LegendOption = {
  formatLabel: (label) => label,
  formatValue: (value) => value,
}

export const defaultAxisOption = {
  x: {
    min: 0,
    max: 1,
    tick: {
      count: AXIS_DEFAULT_TICK_COUNT,
      format: AXIS_DEFAULT_FORMAT,
    }
  },
  y: {
    min: 0,
    max: 1, 
    tick: {
      count: AXIS_DEFAULT_TICK_COUNT,
      format: AXIS_DEFAULT_FORMAT,
    }
  }
}

export const defaultPointOption: PointOption = {
  radius: POINT_RADIUS,
}