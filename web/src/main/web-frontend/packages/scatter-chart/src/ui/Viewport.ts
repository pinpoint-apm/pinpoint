import { drawRect } from "../utils/draw";
import { getSafeDrawImageArgs } from "../utils/helper";
import { Layer } from "./Layer";

export interface ViewportOption {
  width: number;
  height: number;
}

export class Viewport {
  private view: Layer;
  private layers: Layer[];
  private useSafeDrawImageArgs: boolean;

  constructor(wrapper: HTMLElement, {
    width = 0,
    height = 0,
  }) {
    // handle safari issue
    this.useSafeDrawImageArgs = /^((?!chrome|android).)*safari/i.test(navigator.userAgent);
    this.layers = [];
    this.view = new Layer({
      width,
      height,
    });
    this.view.canvas.style.display = 'block';
    wrapper.append(this.view.canvas);
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
    this.layers.forEach(layer => {
      const layerCanvas = layer.canvas;
      const dpr = layer.dpr;

      if (layer.isDisplay) {
        if (layer.isFixed) {
          this.view.context.drawImage(
            layerCanvas,
            0, 0, layerCanvas.width, layerCanvas.height,
            0, 0, layerCanvas.width / dpr, layerCanvas.height / dpr
          );
        } else {
          const args: ReturnType<typeof getSafeDrawImageArgs> = [
            layerCanvas,
            -x * dpr, y * dpr, layerCanvas.width, layerCanvas.height,
            0, y, layerCanvas.width / dpr, layerCanvas.height / dpr
          ]
          const safeArgs = this.useSafeDrawImageArgs ? getSafeDrawImageArgs(...args) : args;
          this.view.context.drawImage(
            ...safeArgs
          );
        }
      }
    })
  }

  public hideLayer(id: string) {
    this.layers.filter(layer => layer.id === id)[0].hide();
  }

  public showLayer(id: string) {
    this.layers.filter(layer => layer.id === id)[0].show();
  }

  public addLayer(layer: Layer | Layer[]) {
    if (Array.isArray(layer)) {
      this.layers = [
        ...this.layers,
        ...layer,
      ];
    } else {
      this.layers.push(layer);
    }
    this.layers.sort((a, b) => {
      if (a.priority > b.priority) {
        return -1;
      } else
        return 1;
    })

    return this;
  }

  public setSize(width: number, height: number) {
    this.view.setSize(width, height);
    this.layers.forEach(layer => {
      layer.setSize(width, height);
    });
    return this;
  }

  public clear() {
    this.view.context.clearRect(0, 0, this.view.canvas.width, this.view.canvas.height);
  }
}