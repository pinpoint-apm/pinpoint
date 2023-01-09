import { newScatterChart } from "./createDefault";

const getMinMaxSettingElements = (title: string, defaultMinMax = {defaultMin: 0, defaultMax: 10000}) => {
  const wrapper = document.createElement('div')
  const titleElement = document.createElement('span');
  const minInputElement = document.createElement('input');
  const maxInputElement = document.createElement('input');
  const buttonElement = document.createElement('button');

  titleElement.style.fontWeight = 'bold';
  titleElement.innerText = title;
  minInputElement.type = 'number';
  minInputElement.value = `${defaultMinMax.defaultMin}`;
  maxInputElement.type = 'number';
  maxInputElement.value = `${defaultMinMax.defaultMax}`; 
  buttonElement.innerText = 'set Min Max';

  wrapper.append(titleElement);
  wrapper.append(' min');
  wrapper.append(minInputElement);
  wrapper.append(' max');
  wrapper.append(maxInputElement);
  wrapper.append(buttonElement);

  return wrapper;
}

export const createSetAxis = () => {
  const wrapper = document.createElement('div');

  setTimeout(() => {
    const SC = newScatterChart(wrapper);
    const XAxisSetter = getMinMaxSettingElements('X Axis');
    const YAxisSetter = getMinMaxSettingElements('Y Axis', {defaultMin: 10, defaultMax: 2500});
    
    wrapper.append(XAxisSetter);
    wrapper.append(YAxisSetter);

    XAxisSetter.getElementsByTagName('button')[0].addEventListener('click', () => {
      const min = XAxisSetter.getElementsByTagName('input')[0].value;
      const max = XAxisSetter.getElementsByTagName('input')[1].value;
      SC.setAxisOption({x:{ min: Number(min), max: Number(max) }})
    })
    YAxisSetter.getElementsByTagName('button')[0].addEventListener('click', () => {
      const min = YAxisSetter.getElementsByTagName('input')[0].value;
      const max = YAxisSetter.getElementsByTagName('input')[1].value;
      SC.setAxisOption({y:{ min: Number(min), max: Number(max) }})
    })

  }, 500);
  
  return wrapper;
}