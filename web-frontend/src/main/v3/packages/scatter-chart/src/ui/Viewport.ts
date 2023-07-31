import { SCATTER_CHART_IDENTIFIER } from '../constants/ui';
import { getSafeDrawImageArgs } from '../utils/helper';
import { Layer } from './Layer';

export interface ViewportOption {
  width: number;
  height: number;
}

export type ViewportEventTypes = 'resize';

type ViewportEventCallbackData = {
  width: number;
  height: number;
};

type ViewportEventData<T extends ViewportEventTypes> = T extends 'resize' ? ViewportEventCallbackData : never;

export interface ViewportEventCallback<T extends ViewportEventTypes> {
  (event: string, data: ViewportEventData<T>): void;
}

interface ViewportEventHandlers {
  [key: string]: ViewportEventCallback<ViewportEventTypes>;
}

export class Viewport {
  static VIEW_CONTAINER_CLASS = `${SCATTER_CHART_IDENTIFIER}container`;
  private view: Layer;
  private layers: Layer[];
  private useSafeDrawImageArgs: boolean;
  private eventHandlers: ViewportEventHandlers = {};
  private rootContainer: HTMLElement;
  private viewContainer: HTMLElement;

  constructor(rootContainer: HTMLElement, { width = 0, height = 0 }) {
    // handle safari issue
    this.rootContainer = rootContainer;
    this.viewContainer = document.createElement('div');
    this.viewContainer.className = Viewport.VIEW_CONTAINER_CLASS;
    this.viewContainer.style.position = 'relative';
    this.useSafeDrawImageArgs = /^((?!chrome|android).)*safari/i.test(navigator.userAgent);
    this.layers = [];
    this.view = new Layer({
      width,
      height,
    });
    this.view.canvas.style.display = 'block';
    this.viewContainer.append(this.view.canvas);
    this.rootContainer.append(this.viewContainer);
  }

  get containerElement() {
    return this.viewContainer;
  }

  get viewLayer() {
    return this.view;
  }

  get canvas() {
    return this.view.canvas;
  }

  get context() {
    return this.view.context;
  }

  get styleWidth() {
    return this.view.canvas.width / this.view.dpr;
  }

  get styleHeight() {
    return this.view.canvas.height / this.view.dpr;
  }

  public render(x: number, y: number) {
    this.layers.forEach((layer) => {
      const layerCanvas = layer.canvas;
      const dpr = layer.dpr;

      if (layer.isDisplay) {
        layer?.render();
        if (layer.isFixed) {
          this.view.context.drawImage(
            layerCanvas,
            0,
            0,
            layerCanvas.width,
            layerCanvas.height,
            0,
            0,
            layerCanvas.width / dpr,
            layerCanvas.height / dpr,
          );
        } else {
          const args: ReturnType<typeof getSafeDrawImageArgs> = [
            layerCanvas,
            -x * dpr,
            y * dpr,
            layerCanvas.width,
            layerCanvas.height,
            0,
            y,
            layerCanvas.width / dpr,
            layerCanvas.height / dpr,
          ];
          const safeArgs = this.useSafeDrawImageArgs ? getSafeDrawImageArgs(...args) : args;
          this.view.context.drawImage(...safeArgs);
        }
      }
    });
  }

  public hideLayer(id: string) {
    this.layers.filter((layer) => layer.id === id)[0].hide();
  }

  public showLayer(id: string) {
    this.layers.filter((layer) => layer.id === id)[0].show();
  }

  public addLayer(layer: Layer | Layer[]) {
    if (Array.isArray(layer)) {
      this.layers = [...this.layers, ...layer];
    } else {
      this.layers.push(layer);
    }
    this.layers.sort((a, b) => {
      if (a.priority > b.priority) {
        return -1;
      } else return 1;
    });

    return this;
  }

  public setSize(width: number, height: number, resize?: boolean) {
    this.view.setSize(width, height);
    this.layers.forEach((layer) => {
      layer.setSize(width, height);
    });
    if (resize) {
      this.eventHandlers?.['resize']?.('resize', { width, height });
    }
    return this;
  }

  public clear() {
    this.view.context.clearRect(0, 0, this.view.canvas.width, this.view.canvas.height);
  }

  public on<T extends ViewportEventTypes>(eventType: ViewportEventTypes, callback: ViewportEventCallback<T>) {
    this.eventHandlers[eventType] = callback;
  }

  public off(eventType: ViewportEventTypes) {
    delete this.eventHandlers[eventType];
  }

  public destroy() {
    this.view.destroy();
    this.layers.forEach((layer) => layer.destroy());
    this.viewContainer.remove();
    const keys = Object.keys(this.eventHandlers) as ViewportEventTypes[];
    keys.forEach((key: ViewportEventTypes) => {
      this.off(key);
    });
  }
}
