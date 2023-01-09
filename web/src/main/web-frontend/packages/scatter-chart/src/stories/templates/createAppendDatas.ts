import { ScatterChart } from "../../ui";
import { newScatterChart } from "./createDefault";
import data2 from '../mock/data2.json';
import data3 from '../mock/data3.json';
import data4 from '../mock/data4.json';
import data5 from '../mock/data5.json';

export const createAppendDatas = () => {
  const wrapper = document.createElement('div');
  const datas = [data2, data3, data4, data5];
  setTimeout(() => {
    const SC = newScatterChart(wrapper);
    SC.setAxisOption({x: {min: 1669103462000, max: 1669103762000}});

    const btn2 = document.createElement('button');
    const btn3 = document.createElement('button');
    const btn4 = document.createElement('button');
    const btn5 = document.createElement('button');

    [btn2, btn3, btn4, btn5].forEach((btnElement, i) => {
      btnElement.innerText = `append data${i + 1}` 
      btnElement.addEventListener('click', () => {
        SC.render(datas[i].data, {append: true});
        btnElement.disabled = true;
      })
      wrapper.append(btnElement);
    })
  }, 500);
  return wrapper;
}
