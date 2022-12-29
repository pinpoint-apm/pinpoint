import { defaultPointOption } from "../constants/options";

export const drawCircle = (
  ctx: CanvasRenderingContext2D, 
  x: number, 
  y: number, 
  { 
    radius = defaultPointOption.radius!,
    fillColor = 'black',
  }: { 
    radius?: number,
    fillColor?: string,
  } = {}
) => {
  ctx.beginPath()
  ctx.arc(x, y, radius, 0, radius * Math.PI);
  ctx.fillStyle = fillColor;
  ctx.fill();
}

export const drawLine = (
  ctx: CanvasRenderingContext2D,
  fromX: number,
  fromY: number,
  toX: number,
  toY: number,
  { 
    color = 'black', 
  }: { 
    color?: string 
  } = {}
) => {
  ctx.beginPath();
  ctx.moveTo(fromX, fromY);
  ctx.lineTo(toX, toY);
  ctx.strokeStyle = color;
  ctx.stroke();
}

export const drawText = (
  ctx: CanvasRenderingContext2D,
  text: string,
  x: number,
  y: number,
  {
    color = 'black',
    textAlign = 'center',
    textBaseline = 'alphabetic',
  }: { 
    color?: string,
    textAlign?: CanvasTextAlign,
    textBaseline?: CanvasTextBaseline,
  } = {}
) => {
  ctx.textAlign = textAlign; 
  ctx.textBaseline = textBaseline; 
  ctx.fillStyle = color;
  ctx.fillText(text, x, y);
}

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
    color?: string,
    strokeColor?: string,
  } = {}
) => {
  ctx.fillStyle = color;
  ctx.fillRect(x, y, width, height);
  if (strokeColor) {
    ctx.strokeStyle = strokeColor;
    ctx.strokeRect(x, y, width, height);        
  }
}