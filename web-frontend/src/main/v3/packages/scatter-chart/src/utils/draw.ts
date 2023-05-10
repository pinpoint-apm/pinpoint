import { defaultPointOption } from '../constants/options';

export const drawCircle = (
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  {
    radius = defaultPointOption.radius!,
    fillColor = 'black',
  }: {
    radius?: number;
    fillColor?: string;
  } = {},
) => {
  ctx.beginPath();
  ctx.arc(x, y, radius, 0, radius * Math.PI);
  ctx.fillStyle = fillColor;
  ctx.fill();
};

export const drawLine = (
  ctx: CanvasRenderingContext2D,
  fromX: number,
  fromY: number,
  toX: number,
  toY: number,
  {
    color = 'black',
  }: {
    color?: string;
  } = {},
) => {
  ctx.beginPath();
  ctx.moveTo(fromX, fromY);
  ctx.lineTo(toX, toY);
  ctx.strokeStyle = color;
  ctx.stroke();
};

export const drawText = (
  ctx: CanvasRenderingContext2D,
  text: string,
  x: number,
  y: number,
  {
    color = 'black',
    textAlign = 'center',
    textBaseline = 'alphabetic',
    font,
  }: {
    color?: string;
    textAlign?: CanvasTextAlign;
    textBaseline?: CanvasTextBaseline;
    font?: CanvasTextDrawingStyles['font'];
  } = {},
) => {
  font && (ctx.font = font);
  ctx.textAlign = textAlign;
  ctx.textBaseline = textBaseline;
  ctx.fillStyle = color;
  ctx.fillText(text, x, y);
};

export const drawRect = (
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  width: number,
  height: number,
  {
    color = 'white',
    strokeColor,
  }: {
    color?: string;
    strokeColor?: string;
  } = {},
) => {
  ctx.fillStyle = color;
  ctx.fillRect(x, y, width, height);
  if (strokeColor) {
    ctx.strokeStyle = strokeColor;
    ctx.strokeRect(x, y, width, height);
  }
};

export const drawArea = (
  ctx: CanvasRenderingContext2D,
  x1: number,
  y1: number,
  x2: number,
  y2: number,
  height: number,
  {
    color = 'black',
  }: {
    color?: string;
  } = {},
) => {
  ctx.beginPath();
  ctx.moveTo(x1, y1);
  ctx.lineTo(x2, y2);
  ctx.moveTo(x1, y1);
  ctx.lineTo(x1, height);
  ctx.lineTo(x2, height);
  ctx.lineTo(x2, y2);
  ctx.closePath();
  ctx.fillStyle = color;
  ctx.fill();
};
