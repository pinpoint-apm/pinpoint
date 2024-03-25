import data1 from '../mock/data1.json';
import { newScatterChart } from './createDefault';

export const createInteractions = () => {
  const wrapper = document.createElement('div');
  const btnElement = document.createElement('button');
  btnElement.innerText = 'clear';

  setTimeout(() => {
    const SC = newScatterChart(wrapper);
    SC.render(data1.data);
    SC.on('click', (_, { x, y }) => {
      alert(`x: ${x}, y: ${y}`);
    });
    SC.on('dragEnd', (_, { x1, y1, x2, y2 }) => {
      alert(`drag from: ${x1}, ${y1}, to: ${x2}, ${y2}`);
    });

    wrapper.append(btnElement);
    btnElement.addEventListener('click', () => {
      SC.clear();
    });
  }, 500);

  return wrapper;
};
