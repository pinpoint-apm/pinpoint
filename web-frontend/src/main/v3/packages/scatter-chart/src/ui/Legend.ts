import { SCATTER_CHART_IDENTIFIER } from '../constants/ui';
import { DataStyleMap, LegendOption } from '../types/types';

export type LegendProps = {
  legendOptions: LegendOption;
  dataStyleMap: DataStyleMap;
  width?: number;
};

export type LegendEventTypes = 'clickLegend' | 'change';

type LegendClickCallbackData = {
  checked: string[];
};

type LegendChangeChallbackData = {
  unChecked: string[];
} & LegendClickCallbackData;

type LegendEventData<T extends LegendEventTypes> = T extends 'clickLegend'
  ? LegendClickCallbackData
  : T extends 'change'
  ? LegendChangeChallbackData
  : never;

export interface LegendEventCallback<T extends LegendEventTypes> {
  (event: MouseEvent, data: LegendEventData<T>): void;
}

interface LegendEventHandlers {
  [key: string]: LegendEventCallback<LegendEventTypes>;
}

export class Legend {
  static LEGEND_CLASS = `${SCATTER_CHART_IDENTIFIER}legend`;
  static LEGEND_CONTAINER_CLASS = `${Legend.LEGEND_CLASS}_container`;
  static MARK_CLASS = `${Legend.LEGEND_CLASS}_mark`;
  static COUNT_CLASS = `${Legend.LEGEND_CLASS}_count`;
  private uniqId = new Date().getTime();
  private rootWrapper;
  private options;
  private dataStyleMap!: DataStyleMap;
  private containerElement: HTMLElement;
  private legendElements: { [key: string]: HTMLDivElement } = {};
  private eventHandlers: LegendEventHandlers = {};
  private types!: string[];
  private abortController: AbortController;

  constructor(rootWrapper: HTMLElement, { legendOptions, dataStyleMap, width }: LegendProps) {
    this.abortController = new AbortController();
    this.rootWrapper = rootWrapper;
    this.options = legendOptions;
    this.containerElement = document.createElement('div');
    this.containerElement.className = Legend.LEGEND_CONTAINER_CLASS;
    this.rootWrapper.append(this.containerElement);
    this.setSize(width);
    this.addEventListener();
    this.setDataStyleMap(dataStyleMap);
  }

  get container() {
    return this.containerElement;
  }

  public setSize(width?: number) {
    this.containerElement.style.width = `${width}px` || `${this.rootWrapper.clientWidth}px`;
  }

  public setDataStyleMap(dataStyleMap: DataStyleMap) {
    this.dataStyleMap = dataStyleMap;
    this.types = Object.keys(dataStyleMap);
    return this;
  }

  private addEventListener = () => {
    const { signal } = this.abortController;
    this.containerElement.addEventListener(
      'click',
      (event) => {
        const { target } = event;
        if (target instanceof HTMLElement) {
          const isInputNode = target.nodeName === 'INPUT';

          if (isInputNode) {
            const checkedInputElements = this.containerElement.querySelectorAll('input:checked');
            const checkedTypes = [...checkedInputElements].map((inputElement) => {
              return inputElement.getAttribute('data-name') || '';
            });

            this.eventHandlers.change?.(event, {
              checked: checkedTypes,
              unChecked: this.types.filter((type) => !checkedTypes.includes(type)),
            });
            this.eventHandlers.clickLegend?.(event, {
              checked: checkedTypes,
            });
          }
        }
      },
      { signal },
    );
  };

  public onChange(callback: LegendEventCallback<'change'>) {
    this.eventHandlers['change'] = callback;
  }

  public render() {
    const options = this.options;
    const dataTypes = this.types;

    this.containerElement.style.visibility = 'visible';
    dataTypes.forEach((type) => {
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
      labelElement.htmlFor = `${Legend.LEGEND_CLASS}_${type}_input_${this.uniqId}`;
      labelElement.append(`${formattedLabel}`, countElement);

      // input
      const inputElement = document.createElement('input');
      inputElement.id = `${Legend.LEGEND_CLASS}_${type}_input_${this.uniqId}`;
      inputElement.type = 'checkbox';
      inputElement.dataset.name = type;
      inputElement.checked = true;

      this.legendElements[type] = legendWrapper;

      legendWrapper.append(markElement, labelElement, inputElement);
      this.containerElement.append(legendWrapper);
    });
    return this;
  }

  public unmount() {
    const containerElement = this.containerElement;
    while (containerElement.firstChild) {
      containerElement.removeChild(containerElement.firstChild);
    }
    return this;
  }

  public setLegendCount(type: string, value: number) {
    const legendElement = this.legendElements[type];
    const countElement = legendElement.getElementsByClassName(Legend.COUNT_CLASS)[0];

    countElement.innerHTML = `${this.options.formatValue?.(value)}`;
  }

  public on(eventType: LegendEventTypes, callback: LegendEventCallback<'clickLegend'>) {
    this.eventHandlers[eventType] = callback;
  }

  public off(eventType: LegendEventTypes) {
    delete this.eventHandlers[eventType];
  }

  public destroy() {
    this.abortController.abort();
    this.unmount();
    this.containerElement.remove();
    const keys = Object.keys(this.eventHandlers) as LegendEventTypes[];
    keys.forEach((key: LegendEventTypes) => {
      this.off(key);
    });
  }
}
