import cloneDeep from 'lodash.clonedeep';
import { Coord, DeepNonNullable, GuideOption, Padding } from '../types/types';
import { drawLine, drawRect, drawText } from '../utils/draw';
import { Axis } from './Axis';
import { Layer } from './Layer';

export interface GuideOptions {
  width: number;
  height: number;
  xAxis: Axis;
  yAxis: Axis;
  ratio: {
    x: number;
    y: number;
  };
  option: GuideOption;
  padding: DeepNonNullable<Padding>;
}

export type GuideEventTypes = 'click' | 'dragEnd';

type GuideDragEndCallbackData = {
  x1: number;
  y1: number;
  x2: number;
  y2: number;
};

type GuideEventData<T extends GuideEventTypes> = T extends 'click'
  ? Coord
  : T extends 'dragEnd'
  ? GuideDragEndCallbackData
  : never;

export interface GuideEventCallback<T extends GuideEventTypes> {
  (event: MouseEvent, data: GuideEventData<T>): void;
}

interface GuideEventHandlers {
  [key: string]: GuideEventCallback<GuideEventTypes>;
}

export class Guide extends Layer {
  private wrapper;
  private isMouseDown = false;
  private isDragging = false;
  private dragStartX = 0;
  private dragStartY = 0;
  private eventHandlers: GuideEventHandlers = {};
  private padding!: GuideOptions['padding'];
  private ratio!: GuideOptions['ratio'];
  private xAxis!: GuideOptions['xAxis'];
  private yAxis!: GuideOptions['yAxis'];
  private option;

  constructor(wrapper: HTMLElement, props: GuideOptions) {
    super({ width: props.width, height: props.height });
    this.canvas.style.position = 'absolute';
    this.canvas.style.zIndex = '999';
    this.canvas.style.top = '0px';
    this.canvas.style.left = '0px';
    this.canvas.style.background = 'transparent';
    this.option = props.option;
    this.setOptions(props);
    this.wrapper = wrapper;
    this.wrapper.append(this.canvas);

    this.addEventListener();
  }

  private isMouseInValidArea(x: number, y: number) {
    const width = this.canvas.width / this.dpr;
    const height = this.canvas.height / this.dpr;
    const padding = this.padding;

    return (
      x >= padding.left + this.xAxis.innerPadding &&
      x <= width - padding.right - this.xAxis.innerPadding &&
      y >= padding.top + this.yAxis.innerPadding &&
      y <= height - padding.bottom - this.yAxis.innerPadding
    );
  }

  private addEventListener() {
    const signal = this.abortSignal;
    this.canvas.addEventListener(
      'mousedown',
      ({ offsetX, offsetY }) => {
        this.isMouseDown = true;
        this.dragStartX = offsetX;
        this.dragStartY = offsetY;
      },
      { signal },
    );

    this.canvas.addEventListener(
      'mousemove',
      ({ offsetX, offsetY }) => {
        const width = this.canvas.width / this.dpr;
        const height = this.canvas.height / this.dpr;
        this.context.clearRect(0, 0, width, height);
        const x = offsetX;
        const y = offsetY;
        if (this.isMouseInValidArea(x, y)) {
          // vertical line
          // drawLine(this.context, x , 0, x, height - padding.bottom + AXIS_INNER_PADDING)
          // horizontal line
          // drawLine(this.context, padding.left, y, width, y)

          this.drawGuideText(x, y);
        }
        if (this.isMouseDown) {
          this.isDragging = true;
          drawRect(
            this.context,
            this.dragStartX,
            this.dragStartY,
            offsetX - this.dragStartX,
            offsetY - this.dragStartY,
            {
              color: this.option?.drag?.backgroundColor,
              strokeColor: this.option?.drag?.strokeColor,
            },
          );
        }
      },
      { signal },
    );

    this.canvas.addEventListener(
      'mouseout',
      () => {
        const width = this.canvas.width / this.dpr;
        const height = this.canvas.height / this.dpr;
        this.isMouseDown = false;
        this.isDragging = false;
        this.context.clearRect(0, 0, width, height);
      },
      { signal },
    );

    this.canvas.addEventListener(
      'mouseup',
      (event) => {
        const width = this.canvas.width / this.dpr;
        const height = this.canvas.height / this.dpr;
        const { offsetX, offsetY } = event;
        this.context.clearRect(0, 0, width, height);

        if (this.isDragging) {
          this.isMouseInValidArea(offsetX, offsetY) && this.drawGuideText(offsetX, offsetY);
          const minX = this.xAxis.min;
          const maxY = this.yAxis.max;
          const xPadding = this.padding.left + this.xAxis.innerPadding;
          const yPadding = this.padding.top + this.yAxis.innerPadding;
          const startX = (this.dragStartX - xPadding) / this.ratio.x + minX;
          const startY = maxY - (this.dragStartY - yPadding) / this.ratio.y;
          const endX = (offsetX - xPadding) / this.ratio.x + minX;
          const endY = maxY - (offsetY - yPadding) / this.ratio.y;
          let x1, x2, y1, y2;

          if (startX > endX) {
            x1 = endX;
            x2 = startX;
          } else {
            x1 = startX;
            x2 = endX;
          }

          if (startY > endY) {
            y1 = startY;
            y2 = endY;
          } else {
            y1 = endY;
            y2 = startY;
          }

          this.eventHandlers['dragEnd']?.(event, {
            x1,
            y1,
            x2,
            y2,
          });
        }
        this.isMouseDown = false;
      },
      { signal },
    );

    this.canvas.addEventListener(
      'click',
      (event) => {
        const { offsetX, offsetY } = event;
        if (!this.isDragging) {
          const xPadding = this.padding.left + this.xAxis.innerPadding;
          const yPadding = this.padding.top + this.yAxis.innerPadding;

          this.eventHandlers['click']?.(event, {
            x: (offsetX - xPadding) / this.ratio.x + this.xAxis.min,
            y: this.yAxis.max - (offsetY - yPadding) / this.ratio.y,
          });
        }
        this.isDragging = false;
      },
      { signal },
    );
  }

  private drawGuideText(x: number, y: number) {
    const { padding, context, canvas, ratio, xAxis, yAxis } = this;
    const { color, backgroundColor, strokeColor, font } = this.option;

    const height = canvas.height / this.dpr;
    const xText = `${xAxis.tick?.format!((x - padding.left - xAxis.innerPadding) / ratio.x + xAxis.min)}`;
    const xTextLines = `${xText}`.split('\n');
    const yText = `${yAxis.tick?.format!(
      Math.floor(Math.abs((height - padding.bottom - yAxis.innerPadding - y) / ratio.y + yAxis.min)),
    )}`;

    // x1
    const xTextWidth = this.getTextWidth(xText) + xAxis.tick!.padding!.left! + xAxis.tick!.padding!.right!;
    const xTextHeight = this.getTextHeight(xText);
    // y
    const yTextWidth = this.getTextWidth(yText);
    const yTextHeight = this.getTextHeight(yText);
    const yRectWidth = yTextWidth + yAxis.tick!.padding!.left! + yAxis.tick!.padding!.left!;
    const yRectHeight = yTextHeight + yAxis.tick!.padding!.top! + yAxis.tick!.padding!.bottom!;

    this.context.font = font || '';

    // x
    drawRect(
      context,
      x - xTextWidth / 2,
      height - padding.bottom + xAxis.tick!.width!,
      xTextWidth,
      xTextHeight + xAxis.tick!.padding!.top! + xAxis.tick!.padding!.bottom!,
      { color: backgroundColor },
    );
    drawLine(context, padding.left - xAxis.tick!.width!, y, padding.left, y, { color: strokeColor });

    xTextLines.reverse().forEach((xLine, i) => {
      drawText(
        context,
        xLine,
        x,
        height -
          padding.bottom +
          xAxis.tick!.width! +
          xTextHeight +
          xAxis.tick!.padding!.top! -
          i * this.getTextHeight(xLine),
        { color, textAlign: 'center', textBaseline: 'bottom' },
      );
    });

    // y
    drawRect(context, padding.left - yAxis.tick!.width! - yRectWidth, y - yRectHeight / 2, yRectWidth, yRectHeight, {
      color: backgroundColor,
    });
    drawLine(context, x, height - padding.bottom, x, height - padding.bottom + yAxis.tick!.width!, {
      color: strokeColor,
    });
    drawText(context, yText, padding.left - yAxis.tick!.width! - yAxis.tick!.padding!.right!, y + yTextHeight / 4, {
      color,
      textAlign: 'end',
    });
  }

  public setOptions({
    width = this.canvas.width / this.dpr,
    height = this.canvas.height / this.dpr,
    ratio = this.ratio,
    padding = this.padding,
    xAxis = this.xAxis,
    yAxis = this.yAxis,
  }) {
    super.setSize(width, height);
    this.padding = { ...this.padding, ...padding };
    this.ratio = cloneDeep(ratio);
    this.xAxis = cloneDeep(xAxis);
    this.yAxis = cloneDeep(yAxis);
  }

  public updateMinX(minX: number) {
    this.xAxis.min = minX;
    return this;
  }

  public on<T extends GuideEventTypes>(eventType: GuideEventTypes, callback: GuideEventCallback<T>) {
    this.eventHandlers[eventType] = callback;
  }

  public off(eventType: GuideEventTypes) {
    delete this.eventHandlers[eventType];
  }

  public destroy() {
    super.destroy();
    this.wrapper.removeChild(this.canvas);
    const keys = Object.keys(this.eventHandlers) as GuideEventTypes[];
    keys.forEach((key: GuideEventTypes) => {
      this.off(key);
    });
  }
}
