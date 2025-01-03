import { ScatterChartOption } from '@pinpoint-fe/scatter-chart';
import { addCommas, formatNewLinedDateString } from '@pinpoint-fe/ui/utils';

export const getDefaultOption = ({ x, y }: { x: [number, number]; y: [number, number] }) =>
  ({
    axis: {
      x: {
        width: 1000,
        min: x[0],
        max: x[1],
        tick: {
          format: (value: number) => {
            return `${formatNewLinedDateString(value)}`;
          },
        },
      },
      y: {
        min: y[0],
        max: y[1],
        tick: {
          format: (value) => addCommas(value),
        },
      },
    },
    data: [
      {
        type: 'success',
        color: 'rgba(61, 207, 168)',
        opacity: 0.6,
        priority: 2,
      },
      {
        type: 'failed',
        color: 'rgba(235, 71, 71)',
        priority: 1,
      },
    ],
    legend: {
      formatLabel: (label) => label.replace(/\b[a-z]/, (letter) => letter.toUpperCase()),
      formatValue: (value) => addCommas(value),
    },
    render: {
      drawOutOfRange: true,
    },
  }) as ScatterChartOption;

export const getAreaChartOption = ({ x, y }: { x: [number, number]; y: [number, number] }) =>
  ({
    axis: {
      x: {
        min: x[0],
        max: x[1],
        tick: {
          count: 6,
          width: 2,
          strokeColor: 'transparent',
          format: () => '',
        },
      },
      y: {
        min: y[0],
        max: y[1],
        tick: {
          count: 3,
          width: 2,
          strokeColor: 'transparent',
          format: (value) => addCommas(value),
        },
      },
    },
    guide: {
      hidden: true,
    },
    legend: {
      hidden: true,
      // formatLabel: (label) => label.replace(/\b[a-z]/, (letter) => letter.toUpperCase()),
    },
    data: [
      {
        type: '1s',
        color: 'rgba(61, 207, 168)',
        priority: 4,
        shape: 'area',
      },
      {
        type: '3s',
        color: '#74b8eb',
        priority: 3,
        shape: 'area',
      },
      {
        type: '5s',
        color: '#ff9800',
        priority: 2,
        shape: 'area',
      },
      {
        type: 'slow',
        color: '#f36c01',
        priority: 1,
        shape: 'area',
      },
    ],
  }) as ScatterChartOption;
