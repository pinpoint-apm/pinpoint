import {
  AxisOption,
  BackgroundOption,
  DataOption,
  GridOption,
  GuideOption,
  LegendOption,
  PointOption,
  RenderOption,
} from '../types/types';
import {
  AXIS_DEFAULT_TICK_COUNT,
  AXIS_TICK_WIDTH,
  COLOR_BACKGROUND,
  COLOR_STROKE,
  COLOR_TEXT,
  POINT_RADIUS,
  TEXT_PADDING_BOTTOM,
  TEXT_PADDING_LEFT,
  TEXT_PADDING_RIGHT,
  TEXT_PADDING_TOP,
} from './ui';

export const AXIS_DEFAULT_FORMAT = (value: number | string) => value;

export const defaultOption = {};

export const defaultDataOption: DataOption[] = [];

export const defaultLegendOption: LegendOption = {
  formatLabel: (label) => label,
  formatValue: (value) => value,
};

export const defaultAxisOption: {
  x: AxisOption;
  y: AxisOption;
} = {
  x: {
    min: 0,
    max: 1,
    strokeColor: COLOR_STROKE,
    tick: {
      color: COLOR_TEXT,
      strokeColor: COLOR_STROKE,
      width: AXIS_TICK_WIDTH,
      count: AXIS_DEFAULT_TICK_COUNT,
      format: AXIS_DEFAULT_FORMAT,
      padding: {
        top: TEXT_PADDING_TOP,
        bottom: TEXT_PADDING_BOTTOM,
        left: TEXT_PADDING_LEFT,
        right: TEXT_PADDING_RIGHT,
      },
    },
  },
  y: {
    min: 0,
    max: 1,
    strokeColor: COLOR_STROKE,
    tick: {
      color: COLOR_TEXT,
      strokeColor: COLOR_STROKE,
      width: AXIS_TICK_WIDTH,
      count: AXIS_DEFAULT_TICK_COUNT,
      format: AXIS_DEFAULT_FORMAT,
      padding: {
        top: TEXT_PADDING_TOP,
        bottom: TEXT_PADDING_BOTTOM,
        left: TEXT_PADDING_LEFT,
        right: TEXT_PADDING_RIGHT,
      },
    },
  },
};

export const defaultPointOption: PointOption = {
  radius: POINT_RADIUS,
  opacity: 1,
};

export const defaultGuideOption: GuideOption = {
  color: COLOR_BACKGROUND,
  strokeColor: COLOR_STROKE,
  backgroundColor: 'black',
  drag: {
    strokeColor: '#469ae4',
    backgroundColor: 'rgba(225,225,225,0.4)',
  },
};

export const defaultBackgroundOption: BackgroundOption = {
  color: COLOR_BACKGROUND,
};

export const defaultGridOption: GridOption = {
  strokeColor: '#d1d1d1',
};

export const defaultRenderOption: RenderOption = {
  append: false,
  drawOutOfRange: false,
};
