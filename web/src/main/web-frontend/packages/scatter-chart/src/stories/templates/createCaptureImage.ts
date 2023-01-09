import { newScatterChart } from "./createDefault";

export const createCaptureIamge = () => {
  const wrapper = document.createElement('div');
  const btnElement = document.createElement('button');
  btnElement.innerText = 'Capture Image';
  
  setTimeout(() => {
    
    const SC = newScatterChart(wrapper);
    wrapper.append(btnElement);

    btnElement.addEventListener('click', () => {
      SC.toBase64Image();
    });
  }, 500);
  return wrapper;
}