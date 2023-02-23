import { SCATTER_CHART_IDENTIFIER } from "../constants/ui";
import { DataStyleMap, LegendEventTypes, LegendOption, ScatterChartEventsTypes } from "../types/types";

export type LegendProps = { 
  types: string[], 
  legendOptions: LegendOption, 
  dataStyleMap: DataStyleMap,
  width?: number;
}

export interface LegendEventCallback {
  (event: MouseEvent, data: {
    checked: string[];
  }): void;
}

interface LegendChangeCallback {
  (event: MouseEvent, data: {
    checked: string[];
    unChecked: string[];
  }): void;
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
  private eventHandlers: {
    change?: LegendChangeCallback,
    clickLegend?: LegendEventCallback,
  } = {}
  
  constructor(rootWrapper: HTMLElement, { types, legendOptions, dataStyleMap, width }: LegendProps) {
    this.rootWrapper = rootWrapper;
    this.types = types;
    this.options = legendOptions;
    this.dataStyleMap = dataStyleMap;
    this.containerElement = document.createElement('div');
    this.containerElement.className = Legend.LEGEND_CONTAINER_CLASS;
    this.setSize(width);
    this.addEventListener();
  }

  get container() {
    return this.containerElement
  }
  
  public setSize(width?: number) {
    this.containerElement.style.width = `${width}px` || `${this.rootWrapper.clientWidth}px`;
  }

  private addEventListener = () => {
    this.containerElement.addEventListener('click', (event) => {
      const { target } = event;
      if (target instanceof HTMLElement) {
        const isInputNode = target.nodeName === 'INPUT';

        if (isInputNode) {
          const checkedInputElements = this.containerElement.querySelectorAll('input:checked');
          const checkedTypes = [...checkedInputElements].map(inputElement => {
            return inputElement.getAttribute('data-name') || '';
          })

          this.eventHandlers.change?.(event, {
            checked: checkedTypes,
            unChecked: this.types.filter(type => !checkedTypes.includes(type)),
          });
          this.eventHandlers.clickLegend?.(event, {
            checked: checkedTypes
          });
        }
      }
    })
  }

  public onChange(callback: LegendChangeCallback) {
    this.eventHandlers['change'] = callback;
  }

  public render() {
    const options = this.options;
    const dataTypes = this.types;

    dataTypes.forEach(type => {
      // wrapper div
      const legendWrapper = document.createElement('div');
      legendWrapper.className = `${Legend.LEGEND_CLASS} ${type}`;

      // mark
      const markElement = document.createElement('span');
      markElement.style.background = this.dataStyleMap[type].legend;
      // const formattedLabel = options?.formatLabel?.(type) || type;
      markElement.className = Legend.MARK_CLASS;

      // count span
      const countElement = document.createElement('span');
      countElement.className = Legend.COUNT_CLASS;
      countElement.innerHTML = '0';

      // label
      const labelElement = document.createElement('label');
      const formattedLabel = options?.formatLabel?.(type) || type;
      labelElement.htmlFor = `${Legend.LEGEND_CLASS}_${type}_input`;
      labelElement.append(`${formattedLabel}`, countElement);


      // input
      const inputElement = document.createElement('input');
      inputElement.id = `${Legend.LEGEND_CLASS}_${type}_input`;
      inputElement.type = 'checkbox';
      inputElement.dataset.name = type;
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

  public on(eventType: LegendEventTypes, callback: LegendEventCallback) {
    this.eventHandlers[eventType] = callback;
  }

  public off(eventType: LegendEventTypes) {
    delete this.eventHandlers[eventType];
  }
}