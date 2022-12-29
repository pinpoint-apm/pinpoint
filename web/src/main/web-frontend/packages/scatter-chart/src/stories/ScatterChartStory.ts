import { ScatterChart } from "../ui";

const newScatterChart = (wrapper: HTMLElement) => {
  return new ScatterChart(wrapper, {
    axis: {
      x: {
        min: 1671684304000,
        max: 1671687904000,
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
}

export const createScatterChart = () => {
  const wrapper = document.createElement('div');
  setTimeout(() => {
    newScatterChart(wrapper);
  }, 500);
  return wrapper;
}


export const createScatterChartResizable = () => {
  const wrapper = document.createElement('div');
  const btnElement1 = document.createElement('button');
  btnElement1.innerHTML = 'resize';
  const btnElement2 = document.createElement('button');
  btnElement2.innerHTML = 'resize 500 by 500';
  
  setTimeout(() => {
    
    const SC = newScatterChart(wrapper);
    wrapper.append(btnElement1);
    wrapper.append(btnElement2);

    btnElement1.addEventListener('click', () => {
      SC.resize();
    });

    btnElement2.addEventListener('click', () => {
      SC.resize(500, 500);
    });

  }, 500);
  return wrapper;
}