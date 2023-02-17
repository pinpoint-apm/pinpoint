import { SCATTER_CHART_IDENTIFIER } from "../constants/ui";
import { DataStyleMap, LegendOption } from "../types/types";

export type LegendProps = { 
  types: string[], 
  legendOptions: LegendOption, 
  dataStyleMap: DataStyleMap,
  width?: number;
}
export class Legend {
  static LEGEND_CLASS = `${SCATTER_CHART_IDENTIFIER}legend`;
  static LEGEND_CONTAINER_CLASS = `${Legend.LEGEND_CLASS}_container`;
  static MARK_CLASS = `${Legend.LEGEND_CLASS}_mark`;
  static COUNT_CLASS = `${Legend.LEGEND_CLASS}_count`;
  private rootWrapper;
  private types;
  private options;
  private dataStyleMap;
  private containerElement: HTMLElement;
  private legendElements: {[key: string]: HTMLDivElement} = {} 
  
  constructor(rootWrapper: HTMLElement, { types, legendOptions, dataStyleMap, width }: LegendProps) {
    this.rootWrapper = rootWrapper;
    this.types = types;
    this.options = legendOptions;
    this.dataStyleMap = dataStyleMap;
    this.containerElement = document.createElement('div');
    this.containerElement.className = Legend.LEGEND_CONTAINER_CLASS;
    this.setSize(width);
  }
  
  public setSize(width?: number) {
    this.containerElement.style.width = `${width}px` || `${this.rootWrapper.clientWidth}px`;
  }

  public addEvents(callback?: ({ type, checked }: { type?: string, checked?: boolean}) => void) {
    this.containerElement.addEventListener('click', (event) => {
      const { target } = event;
      if (target instanceof HTMLElement) {
        const isInputNode = target.nodeName === 'INPUT';
        const wrapper = target.closest('div');
        const checkbox = wrapper?.querySelector('input');

        if (isInputNode) {
          callback?.({ type: wrapper?.dataset.name, checked: checkbox?.checked });
        }
      }
    });
  }

  public render() {
    const options = this.options;
    const dataTypes = this.types;

    dataTypes.forEach(type => {
      // wrapper div
      const legendWrapper = document.createElement('div');
      legendWrapper.dataset.name = type;
      legendWrapper.className = `${Legend.LEGEND_CLASS} ${type}`;

      // mark
      const markElement = document.createElement('span');
      markElement.style.background = this.dataStyleMap[type].legend;
      // const formattedLabel = options?.formatLabel?.(type) || type;
      markElement.className = Legend.MARK_CLASS;

      // count span
      const countElement = document.createElement('span');
      countElement.className = Legend.COUNT_CLASS;

      // label
      const labelElement = document.createElement('label');
      const formattedLabel = options?.formatLabel?.(type) || type;
      labelElement.htmlFor = `${Legend.LEGEND_CLASS}_${type}_input`;
      labelElement.append(`${formattedLabel}`, countElement);


      // input
      const inputElement = document.createElement('input');
      inputElement.id = `${Legend.LEGEND_CLASS}_${type}_input`;
      inputElement.type = 'checkbox';
      inputElement.checked = true;

      this.legendElements[type] = legendWrapper;

      legendWrapper.append(markElement, labelElement, inputElement);
      this.containerElement.append(legendWrapper);
    })
    this.rootWrapper.append(this.containerElement);
  }

  public setLegendCount(type: string, value: number) {
    const legendElement = this.legendElements[type];
    const countElement = legendElement.getElementsByClassName(Legend.COUNT_CLASS)[0];
    
    countElement.innerHTML = `${this.options.formatValue?.(value)}`;
  }
}