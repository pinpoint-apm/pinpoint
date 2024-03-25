import data1 from '../mock/data1.json';
import { newScatterChart } from './createDefault';

export const createHideLegend = () => {
  const wrapper = document.createElement('div');

  setTimeout(() => {
    const SC = newScatterChart(wrapper, { legend: { hidden: true } });
    SC.render(data1.data);
  }, 500);

  return wrapper;
};
