import { ScatterChart } from "../../ui";
import data1 from '../mock/data1.json';

export const newScatterChart = (wrapper: HTMLElement) => {
  const SC = new ScatterChart(wrapper, {
    axis: {
      x: {
        min: 1669103462000,
        max: 1669103509335,
        tick: {
          count: 5,
          format: (value) => {
            const date = new Date(value);
            return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
          }, 
        }
      },
      y: {
        min: 0,
        max: 10000,
        tick: {
          count: 5,
          format: (value) => value.toLocaleString(),
        }
      }
    },
    data: [
      {
        type: 'success',
        color: 'green',
        priority: 11,
      },
      {
        type: 'fail',
        color: 'red',
        priority: 1,
      },
    ],
    legend: {
      formatLabel: (label) => label.toUpperCase(),
      formatValue: (value) => value.toLocaleString(),
    },
  });
  SC.render(data1.data);
  return SC
}

export const createDefault = () => {
  const wrapper = document.createElement('div');
  setTimeout(() => {
    newScatterChart(wrapper);
  }, 500);
  return wrapper;
}
