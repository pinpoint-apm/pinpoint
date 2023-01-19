import { newScatterChart } from "./createDefault";
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
        color: 'aliceBlue'
      },
      guide: {
        color: 'yellow',
        backgroundColor: 'green',
        strokeColor: 'aqua',
        drag: {
          backgroundColor: 'rgba(32, 178, 7, 0.6)',
          strokeColor: 'black',
        }
      },
      grid: {
        // hidden: true,
        strokeColor: 'rgb(255, 0, 255, 0.4)'
      },
      point: {
        radius: 10,
      },
    });
    SC.setAxisOption({
      x: {
        padding: 20,
        tick: {
          font: '20px serif',
          color: 'red',
          strokeColor: 'blue',
          width: 10,
        }
      },
      y: {
        padding: 50,
        tick: {
          font: '15px serif',
          color: 'green',
          strokeColor: 'purple',
          width: 10,
        }
      }
    })
    wrapper.append(btnStart);
    wrapper.append(btnStop);

    btnStart.addEventListener('click', () => {
      SC.startRealtime(47335);
      const newData = data1.data.map(d => ({...d, x: d.x + 47335}));
      SC.render(newData, {append: true});
    });

    btnStop.addEventListener('click', () => {
      SC.stopRealtime();
    });
  }, 500);
  return wrapper;
}