import { TEXT_MARGIN_BOTTOM, TEXT_MARGIN_LEFT, TEXT_MARGIN_RIGHT, TEXT_MARGIN_TOP } from "../constants/ui";
import { AxisOption, GuideOption, Padding } from "../types/types";
import { drawLine, drawRect, drawText } from "../utils/draw";
import { Axis } from "./Axis";
import { Layer } from "./Layer";

export interface GuideOptions {
  width: number;
  height: number;
  xAxis: Axis,
  yAxis: Axis,
  ratio: {
    x: number;
    y: number;
  };
  option: GuideOption
  padding: DeepNonNullable<Padding>;
}

export class Guide extends Layer {
  private wrapper;
  private padding;
  private ratio;
  private isMouseDown = false;
  private isDragging = false;
  private dragStartX = 0;
  private dragStartY = 0;
  private eventHandlers: {[key: string]: Function} = {};
  private xAxis;
  private yAxis;
  private minX;
  private maxY;
  private option;

  constructor(wrapper: HTMLElement, {
    width, height, padding, ratio, xAxis, yAxis, option,
  }: GuideOptions) {
    super({ width, height });
    this.canvas.style.position = 'absolute';
    this.canvas.style.zIndex = '999';
    this.canvas.style.top = '0px';
    this.canvas.style.left = '0px';
    this.canvas.style.background = 'transparent';
    this.padding = padding;
    this.ratio = ratio;
    this.xAxis = xAxis;
    this.yAxis = yAxis;
    this.minX = this.xAxis.min;
    this.maxY = this.yAxis.max;
    this.option = option;
    this.wrapper = wrapper;
    this.wrapper.append(this.canvas);
    this.addEventListener();
  }

  private isMouseInValidArea(x: number, y: number) {
    const width = this.canvas.width / this.dpr;
    const height = this.canvas.height / this.dpr;
    const padding = this.padding

    return (
      x >= padding.left + this.xAxis.innerPadding &&
      x <= width - padding.right - this.xAxis.innerPadding && 
      y >= padding.top + this.yAxis.innerPadding && 
      y <= height - padding.bottom - this.yAxis.innerPadding
    )
  }

  private addEventListener() {
    const { drag } = this.option
    const width = this.canvas.width / this.dpr;
    const height = this.canvas.height / this.dpr;

    this.canvas.addEventListener('mousedown', ({ offsetX, offsetY }) => {
      this.isMouseDown = true;
      this.dragStartX = offsetX;
      this.dragStartY = offsetY;
    });

    this.canvas.addEventListener('mousemove', ({ offsetX, offsetY }) => {
      this.context.clearRect(0, 0, width, height);
      const x = offsetX;
      const y = offsetY;
      if (this.isMouseInValidArea(x, y)) {
        // vertical line
        // drawLine(this.context, x , 0, x, height - padding.bottom + AXIS_INNER_PADDING)
        // horizontal line
        // drawLine(this.context, padding.left, y, width, y)

        this.drawGuideText(x,y);
      }
      if (this.isMouseDown) {
        this.isDragging = true;
        drawRect(this.context, 
          this.dragStartX, this.dragStartY, offsetX - this.dragStartX, offsetY - this.dragStartY,
          {
            color: drag?.backgroundColor,
            strokeColor: drag?.strokeColor,
          }
        )
      }
    });

    this.canvas.addEventListener('mouseout', event => {
      this.isMouseDown = false;
      this.isDragging = false;
      this.context.clearRect(0, 0, width, height);
    });

    this.canvas.addEventListener('mouseup', event => {
      const { offsetX, offsetY } = event;
      this.context.clearRect(0, 0, width, height);
      
      if (this.isDragging) {
        this.isMouseInValidArea(offsetX, offsetY) && this.drawGuideText(offsetX, offsetY)
        this.eventHandlers['dragEnd']?.({ 
          x1: this.dragStartX / this.ratio.x + this.minX,
          y1: this.maxY - (this.dragStartY - this.padding.top - this.xAxis.innerPadding) / this.ratio.y,
          x2: this.maxY - (offsetX - this.padding.top - this.yAxis.innerPadding) / this.ratio.x + this.minX,
          y2: this.maxY - (offsetY - this.padding.top - this.yAxis.innerPadding) / this.ratio.y,
        });
      }
      this.isMouseDown = false;
    });

    this.canvas.addEventListener('click', event => {
      const { offsetX, offsetY } = event;
      if (!this.isDragging) {
        this.eventHandlers['click']?.({ 
          x: offsetX / this.ratio.x + this.minX,
          y: this.maxY - (offsetY - this.padding.top - this.yAxis.innerPadding) / this.ratio.y,
        })
      }
      this.isDragging = false;
    })
  }

  private removeEventListener() {
  }

  private drawGuideText(x: number, y: number) {
    const { padding, context, canvas, ratio, xAxis, yAxis } = this;
    const { color, backgroundColor, strokeColor } = this.option;

    const height = canvas.height / this.dpr;
    const xText = `${xAxis.tick?.format!((x - padding.left - xAxis.innerPadding) / ratio.x + this.minX)}`;
    const yText = `${yAxis.tick?.format!(Math.floor(Math.abs((height - padding.bottom - yAxis.innerPadding - y) / ratio.y + yAxis.min)))}`;
    
    // x1
    const xTextWidth = this.getTextWidth(xText) + TEXT_MARGIN_LEFT + TEXT_MARGIN_RIGHT;
    const xTextHeight = this.getTextHeight(xText);
    // y
    const yTextWidth = this.getTextWidth(yText) + TEXT_MARGIN_LEFT + TEXT_MARGIN_RIGHT;
    const yTextHeight = this.getTextHeight(yText) + TEXT_MARGIN_TOP + TEXT_MARGIN_BOTTOM;

    // x
    drawRect(context, x - xTextWidth / 2, height - padding.bottom + xAxis.tick?.width!, xTextWidth, xTextHeight + TEXT_MARGIN_TOP + TEXT_MARGIN_BOTTOM, { color: backgroundColor });
    drawLine(context, padding.left - xAxis.tick?.width!, y, padding.left, y, { color: strokeColor });
    drawText(context, xText, x, height - padding.bottom + xAxis.tick?.width! + xTextHeight + TEXT_MARGIN_TOP, { color, textAlign: 'center', textBaseline: 'bottom' });
    // y
    drawRect(context, padding.left - yAxis.tick?.width! - yTextWidth, y - yTextHeight / 2, yTextWidth, yTextHeight, { color: backgroundColor });
    drawLine(context, x, height - padding.bottom, x, height - padding.bottom + yAxis.tick?.width!, { color: strokeColor });
    drawText(context, yText, padding.left - yAxis.tick?.width! - TEXT_MARGIN_RIGHT, y + 3, { color, textAlign: 'end' });
    
  }

  public setOptions({ 
    width = this.canvas.width / this.dpr, 
    height = this.canvas.height / this.dpr,
    ratio = this.ratio,
    padding = this.padding,
  }) {
    this.removeEventListener();
    super.setSize(width, height);
    this.padding = {...this.padding, ...padding};
    this.ratio = ratio;
    this.addEventListener();
  }

  // public setSize(width: number, height: number){
  //   super.setSize(width, height);
  //   this.removeEventListener();
  //   this.addEventListener();
  //   return this;
  // }

  // public setPadding(padding: Padding) {
  //   this.padding = {...CONTAINER_PADDING, ...padding};
  //   return this;
  // }

  // public setRatio(ratio: { x: number, y: number }){
  //   this.ratio = ratio;
  //   this.removeEventListener();
  //   this.addEventListener();
  //   return this;
  // }

  // public updateXAxis(x: Partial<AxisOption>) {
  //   this.axisOption = {...this.axisOption, ...{ x: {...this.xAxis, ...x}}}
  // }
  public updateMinX(minX: number) {
    this.minX = minX;
    return this;
  }

  public on(evetntType: string, callback: (data: any) => void) {
    this.eventHandlers[evetntType] = callback;
  }
}