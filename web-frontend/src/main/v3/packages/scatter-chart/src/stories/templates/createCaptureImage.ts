import { newScatterChart } from "./createDefault";

export const createCaptureIamge = () => {
  const wrapper = document.createElement('div');
  const btnElement = document.createElement('button');
  btnElement.innerText = 'Capture Image';
  
  setTimeout(() => {
    
    const SC = newScatterChart(wrapper);
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