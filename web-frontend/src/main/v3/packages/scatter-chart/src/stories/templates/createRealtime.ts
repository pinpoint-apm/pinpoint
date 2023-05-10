import { newScatterChart } from './createDefault';
import data1 from '../mock/data1.json';

export const createRealtime = () => {
  const wrapper = document.createElement('div');
  const btnStart = document.createElement('button');
  btnStart.innerText = 'start realtime';
  const btnStop = document.createElement('button');
  btnStop.innerText = 'stop realtime';

  setTimeout(() => {
    const SC = newScatterChart(wrapper);
    const newData = data1.data.map((d) => ({ ...d, x: d.x + 47335 }));
    SC.render(data1.data, { append: true });
    SC.render(newData, { append: true });

    wrapper.append(btnStart);
    wrapper.append(btnStop);

    btnStart.addEventListener('click', () => {
      SC.startRealtime(data1.to - data1.from);
    });

    btnStop.addEventListener('click', () => {
      SC.stopRealtime();
    });
  }, 500);
  return wrapper;
};
