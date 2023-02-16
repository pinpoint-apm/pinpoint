import { newScatterChart } from "./createDefault";
import data1 from '../mock/data1.json';

export const createPinpointTheme = () => {
  const wrapper = document.createElement('div');
  wrapper.style.fontSize = '20px';
  const btnStart = document.createElement('button');
  btnStart.innerText = 'start realtime';
  const btnStop = document.createElement('button');
  btnStop.innerText = 'stop realtime';
  const btnElement = document.createElement('button');
  btnElement.innerText = 'Capture Image';
  
  setTimeout(() => {
    const SC = newScatterChart(wrapper, {
      point: {
        radius: 4.5,
        opacity: 0.5,
      },
      data: [
        {
          type: 'success',
          color: 'rgba(61, 207, 168)',
          priority: 11,
        },
        {
          type: 'fail',
          color: 'rgba(235, 71, 71)',
          priority: 2,
          opacity: 1,
        },
      ],
    });
    SC.setAxisOption({
      x: {
        padding: 7,
      },
      y: {
        padding: 7,
        min: 0,
        max: 1000,
      }
    })
    wrapper.append(btnStart);
    wrapper.append(btnStop);
    const newData = data1.data.map(d => ({...d, x: d.x + 47335}));
    SC.render(newData, {append: true});

    btnStart.addEventListener('click', () => {
      SC.startRealtime(data1.to - data1.from);
    });

    btnStop.addEventListener('click', () => {
      SC.stopRealtime();
    });

    wrapper.append(btnElement);

    btnElement.addEventListener('click', async () => {
      
      const image = await SC.toBase64Image();

      const downloadElement = document.createElement('a');
      downloadElement.setAttribute("href", image);
      downloadElement.setAttribute('download', `${1}.png`);
      wrapper.appendChild(downloadElement);
      downloadElement.click();
      wrapper.removeChild(downloadElement);
    });
  }, 500);
  return wrapper;
}