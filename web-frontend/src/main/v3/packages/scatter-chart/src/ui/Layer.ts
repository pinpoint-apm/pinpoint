import { LAYER_DEFAULT_PRIORITY } from '../constants/ui';
import { getDevicePicelRatio } from '../utils/helper';

export interface LayerProps {
  width?: number;
  height?: number;
  display?: boolean;
  fixed?: boolean;
  priority?: number;
}

export class Layer {
  private cvs: HTMLCanvasElement;
  private ctx: CanvasRenderingContext2D;
  private identifier = '';
  private display;
  private fixed;
  private priorityOrder;
  private displayPixcelRatio;
  private abortController: AbortController;
  protected abortSignal: AbortSignal;

  constructor({
    width = 0,
    height = 0,
    display = true,
    fixed = false,
    priority = LAYER_DEFAULT_PRIORITY,
  }: LayerProps = {}) {
    this.displayPixcelRatio = getDevicePicelRatio();
    this.display = display;
    this.fixed = fixed;
    this.priorityOrder = priority;
    this.cvs = document.createElement('canvas');
    this.ctx = this.cvs.getContext('2d')!;
    this.cvs.style.width = `${width}px`;
    this.cvs.style.height = `${height}px`;
    this.cvs.width = width * this.dpr;
    this.cvs.height = height * this.dpr;
    this.ctx.scale(this.dpr, this.dpr);
    this.abortController = new AbortController();
    this.abortSignal = this.abortController.signal;
  }

  private resetDpr() {
    this.displayPixcelRatio = getDevicePicelRatio();
  }

  public setSize(width: number, height: number) {
    this.resetDpr();
    this.cvs.style.width = `${width}px`;
    this.cvs.style.height = `${height}px`;
    this.cvs.width = width * this.dpr;
    this.cvs.height = height * this.dpr;
    this.ctx.scale(this.dpr, this.dpr);
  }

  public setStyle() {
    // override method
  }

  public render() {
    // override method
  }

  public show() {
    this.display = true;
  }

  public hide() {
    this.display = false;
  }

  public clear() {
    this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
  }

  public destroy() {
    this.cvs.width = 0;
    this.cvs.height = 0;
    this.abortController.abort();
  }

  get dpr() {
    return this.displayPixcelRatio;
  }

  get priority() {
    return this.priorityOrder;
  }

  set priority(priority: number) {
    this.priorityOrder = priority;
  }

  get id() {
    return this.identifier;
  }

  set id(id: string) {
    this.identifier = id;
  }

  get isFixed() {
    return this.fixed;
  }

  set isFixed(fixed: boolean) {
    this.fixed = fixed;
  }

  get canvas() {
    return this.cvs;
  }

  get context() {
    return this.ctx;
  }

  get isDisplay() {
    return this.display;
  }

  public swapCanvasImage({ width, startAt }: { width: number; startAt: number }) {
    const rightImage = this.context.getImageData(
      (startAt + width) * this.dpr,
      0,
      this.canvas.width - (startAt + width) * this.dpr,
      this.canvas.height,
    );
    this.clear();
    this.context.putImageData(rightImage, startAt * this.dpr, 0);
  }

  public getTextWidth(text: string) {
    const lines = `${text}`.split('\n');
    const largestWidth = lines.reduce((width, txt) => {
      const textWidth = this.context.measureText(`${txt}`).width;
      return width > textWidth ? width : textWidth;
    }, 0);
    return largestWidth;
  }

  public getTextHeight(text: string) {
    const lines = `${text}`.split('\n');
    const totalHeight = lines.reduce((sum, txt) => {
      const metrics = this.context.measureText(`${txt}`);

      const textAscent = metrics.fontBoundingBoxAscent || metrics.actualBoundingBoxAscent;
      const textDescent = metrics.fontBoundingBoxDescent || metrics.actualBoundingBoxDescent;
      const textHeight = textAscent + textDescent;

      return textHeight + sum;
    }, 0);
    return totalHeight;
  }
}
