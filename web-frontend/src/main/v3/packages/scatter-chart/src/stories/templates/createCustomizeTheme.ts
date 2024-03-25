import { newScatterChart } from './createDefault';
import data1 from '../mock/data1.json';

export const createCustomizeTheme = () => {
  const wrapper = document.createElement('div');
  wrapper.style.fontSize = '20px';
  const btnStart = document.createElement('button');
  btnStart.innerText = 'start realtime';
  const btnStop = document.createElement('button');
  btnStop.innerText = 'stop realtime';

  setTimeout(() => {
    const SC = newScatterChart(wrapper, {
      padding: {
        top: 40,
        bottom: 10,
        left: 10,
        right: 50,
      },
      background: {
        color: 'aliceBlue',
      },
      guide: {
        color: 'yellow',
        backgroundColor: 'green',
        strokeColor: 'aqua',
        drag: {
          backgroundColor: 'rgba(32, 178, 7, 0.6)',
          strokeColor: 'black',
        },
        font: '25px serif',
      },
      grid: {
        strokeColor: 'rgb(255, 0, 255, 0.4)',
      },
      point: {
        radius: 10,
      },
    });
    SC.setOption({
      axis: {
        x: {
          padding: 20,
          tick: {
            font: '25px serif',
            color: 'red',
            strokeColor: 'blue',
            width: 10,
            format: (value) => {
              const date = new Date(value);
              return `${String(date.getFullYear())}.${String(date.getMonth())}.${String(date.getDay())}\n${String(
                date.getHours(),
              ).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(
                2,
                '0',
              )}`;
            },
            padding: {
              top: 10,
              bottom: 10,
              left: 10,
              right: 10,
            },
          },
        },
        y: {
          padding: 50,
          tick: {
            font: '15px serif',
            color: 'green',
            strokeColor: 'purple',
            width: 10,
            padding: {
              top: 10,
              bottom: 10,
              left: 20,
              right: 20,
            },
          },
        },
      },
    });
    wrapper.append(btnStart);
    wrapper.append(btnStop);
    const newData = data1.data.map((d) => ({ ...d, x: d.x + 47335 }));
    SC.render(data1.data, { append: true });
    SC.render(newData, { append: true });
    SC.resize();
    btnStart.addEventListener('click', () => {
      SC.startRealtime(data1.to - data1.from);
    });

    btnStop.addEventListener('click', () => {
      SC.stopRealtime();
    });
  }, 500);
  return wrapper;
};
