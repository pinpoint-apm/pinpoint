import { LAYER_DEFAULT_PRIORITY } from "../constants/ui";
import { Coord } from "../types";
import { getDevicePicelRatio } from "../utils/helper";

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
  private identifier: string = '';
  private display;
  private fixed;
  private priorityOrder;
  private displayPixcelRatio;

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
  }

  private resetDpr() {
    this.displayPixcelRatio = getDevicePicelRatio();
  }

  public setSize(width: number, height: number) {
    this.clear();
    this.resetDpr();
    this.cvs.style.width = `${width}px`;
    this.cvs.style.height = `${height}px`;
    this.cvs.width = width * this.dpr;
    this.cvs.height = height * this.dpr;
    this.ctx.scale(this.dpr, this.dpr);
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

  public swapCanvasImage() {
    const width = this.canvas.width / this.dpr;
    const height = this.canvas.height / this.dpr;
    const rightImage = this.getCroppedImage(this.ctx, { x: width, y: 0 }, { x: width * 2, y: height });
    this.ctx.clearRect(0, 0, width, height);
    this.ctx.putImageData(rightImage, 0, 0)
  }

  public getCroppedImage(ctx: CanvasRenderingContext2D, lCoord: Coord, rCoord: Coord) {
    return ctx.getImageData(lCoord.x, lCoord.y, rCoord.x, rCoord.y);
  }

  public getTextWidth(text: string | number) {
    return this.context.measureText(`${text}`).width;
  }

  public getTextHeight(text: string | number) {
    const metrics = this.context.measureText(`${text}`);
    let fontHeight = metrics.fontBoundingBoxAscent + metrics.fontBoundingBoxDescent;
    // let actualHeight = metrics.actualBoundingBoxAscent + metrics.actualBoundingBoxDescent;
    return fontHeight;
  }
}