import data1 from '../mock/data1.json';
import { newScatterChart } from './createDefault';

export const createDestroy = () => {
  const wrapper = document.createElement('div');
  const btnElement = document.createElement('button');
  btnElement.innerHTML = 'destroy';

  setTimeout(() => {
    const SC = newScatterChart(wrapper);
    SC.render(data1.data);
    wrapper.append(btnElement);

    btnElement.addEventListener('click', () => {
      SC.destroy();
    });
  }, 500);
  return wrapper;
};
