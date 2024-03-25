import data1 from '../mock/data1.json';
import { newScatterChart } from './createDefault';

export const createResizable = () => {
  const wrapper = document.createElement('div');
  const btnElement1 = document.createElement('button');
  btnElement1.innerHTML = 'resize';
  const btnElement2 = document.createElement('button');
  btnElement2.innerHTML = 'resize 500 by 500';

  setTimeout(() => {
    const SC = newScatterChart(wrapper);
    SC.render(data1.data, { append: true });
    SC.on('resize', (_, { width, height }) => {
      alert(`resize() triggered, width is ${width}, height is ${height}`);
    });
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
};
