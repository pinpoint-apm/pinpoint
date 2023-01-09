import merge from "lodash.merge";
import { AXIS_INNER_PADDING, AXIS_TICK_LENGTH, CONTAINER_PADDING, TEXT_MARGIN_BOTTOM, TEXT_MARGIN_LEFT, TEXT_MARGIN_RIGHT, TEXT_MARGIN_TOP } from "../constants/ui";
import { AxisOption, FormatType, LegendOption, Padding } from "../types";
import { drawLine, drawRect, drawText } from "../utils/draw";
import { Layer } from "./Layer";

export interface GuideOptions {
  width: number;
  height: number;
  padding: Padding;
  ratio: {
    x: number;
    y: number;
  };
  axisOption: { x: AxisOption, y: AxisOption };
}

export class Guide extends Layer {
  private wrapper;
  private padding;
  private ratio;
  private axisOption;
  private isMouseDown = false;
  private isDragging = false;
  private dragStartX = 0;
  private dragStartY = 0;
  private eventHandlers: {[key: string]: Function} = {};

  constructor(wrapper: HTMLElement, {
    width, height, padding, ratio, axisOption,
  }: GuideOptions) {
    super({ width, height });
    this.canvas.style.position = 'absolute';
    this.canvas.style.zIndex = '999';
    this.canvas.style.top = '0px';
    this.canvas.style.left = '0px';
    this.canvas.style.background = 'transparent';
    this.padding = {...CONTAINER_PADDING, ...padding};
    this.ratio = ratio;
    this.axisOption = axisOption;
    this.wrapper = wrapper;
    this.wrapper.append(this.canvas);
    this.addEventListener();
  }

  private isMouseInValidArea(x: number, y: number) {
    const width = this.canvas.width / this.dpr;
    const height = this.canvas.height / this.dpr;
    const padding = this.padding

    return (
      x >= padding.left + AXIS_INNER_PADDING &&
      x <= width - padding.right - AXIS_INNER_PADDING && 
      y >= padding.top + AXIS_INNER_PADDING && 
      y <= height - padding.bottom - AXIS_INNER_PADDING
    )
  }

  private addEventListener() {
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
            color: 'rgba(225,225,225,0.5)',
            strokeColor: 'blue',
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
          x1: this.dragStartX / this.ratio.x + this.axisOption.x.min,
          y1: this.axisOption.y.max - (this.dragStartY - this.padding.top - AXIS_INNER_PADDING) / this.ratio.y,
          x2: this.axisOption.y.max - (offsetX - this.padding.top - AXIS_INNER_PADDING) / this.ratio.x + this.axisOption.x.min,
          y2: this.axisOption.y.max - (offsetY - this.padding.top - AXIS_INNER_PADDING) / this.ratio.y,
        });
      }
      this.isMouseDown = false;
    });

    this.canvas.addEventListener('click', event => {
      const { offsetX, offsetY } = event;
      if (!this.isDragging) {
        this.eventHandlers['click']?.({ 
          x: offsetX / this.ratio.x + this.axisOption.x.min,
          y: this.axisOption.y.max - (offsetY - this.padding.top - AXIS_INNER_PADDING) / this.ratio.y,
        })
      }
      this.isDragging = false;
    })
  }

  private removeEventListener() {
  }

  private drawGuideText(x: number, y: number) {
    const { padding, context, canvas, ratio, axisOption } = this;
    const height = canvas.height / this.dpr;
    const xText = `${axisOption.x.tick?.format!((x - padding.left - AXIS_INNER_PADDING) / ratio.x + axisOption.x.min)}`;
    const yText = `${axisOption.y.tick?.format!(Math.floor(Math.abs((height - padding.bottom - AXIS_INNER_PADDING - y) / ratio.y + axisOption.y.min)))}`;
    
    // x1
    const xTextWidth = this.getTextWidth(xText) + TEXT_MARGIN_LEFT + TEXT_MARGIN_RIGHT;
    const xTextHeight = this.getTextHeight(xText) + TEXT_MARGIN_TOP + TEXT_MARGIN_BOTTOM;
    // y
    const yTextWidth = this.getTextWidth(yText) + TEXT_MARGIN_LEFT + TEXT_MARGIN_RIGHT;
    const yTextHeight = this.getTextHeight(yText) + TEXT_MARGIN_TOP + TEXT_MARGIN_BOTTOM;

    // x
    drawRect(context, x - xTextWidth / 2, height - padding.bottom + AXIS_TICK_LENGTH, xTextWidth, xTextHeight, { color: 'black' });
    drawLine(context, padding.left - AXIS_TICK_LENGTH, y, padding.left, y);
    drawText(context, xText, x, height - TEXT_MARGIN_BOTTOM, { color: 'white', textAlign: 'center', textBaseline: 'bottom' });
    // y
    drawRect(context, padding.left - AXIS_TICK_LENGTH - yTextWidth, y - yTextHeight / 2, yTextWidth, yTextHeight, { color: 'black' });
    drawLine(context, x, height - padding.bottom, x, height - padding.bottom + AXIS_TICK_LENGTH);
    drawText(context, yText, padding.left - AXIS_TICK_LENGTH - TEXT_MARGIN_RIGHT, y + 3, { color: 'white', textAlign: 'end' });
    
  }

  public setOptions({ 
    width = this.canvas.width / this.dpr, 
    height = this.canvas.height / this.dpr,
    ratio = this.ratio,
    padding = this.padding,
    axisOption = this.axisOption,
  }) {
    this.removeEventListener();
    super.setSize(width, height);
    this.axisOption = merge(this.axisOption, axisOption);
    this.padding = {...CONTAINER_PADDING, ...padding};
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

  public updateXAxis(x: Partial<AxisOption>) {
    this.axisOption = {...this.axisOption, ...{ x: {...this.axisOption.x, ...x}}}
  }

  public on(evetntType: string, callback: (data: any) => void) {
    this.eventHandlers[evetntType] = callback;
  }
}