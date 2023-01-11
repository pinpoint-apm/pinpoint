import { AXIS_TICK_LENGTH, TEXT_MARGIN_RIGHT } from "../constants/ui";
import { TickOption } from "../types/types";
import { drawLine, drawRect, drawText } from "../utils/draw";
import { Axis, AxisProps } from "./Axis";

export interface YAxisProps extends AxisProps {};

export class YAxis extends Axis {
  constructor({
    ...props
  }: YAxisProps = {}) {
    super(props);
    this.render();
  }

  public setSize(width: number, height: number){
    super.setSize(width, height);
    this.render();
    return this;
  }

  public render() {
    this.clear();
    const { min, max, tick, innerPadding } = this;
    const { format, count } = tick as DeepNonNullable<TickOption>;
    const padding = this.padding;
    const width = this.canvas.width / this.dpr;
    const height = this.canvas.height / this.dpr;
    
    const startX = padding.left;
    const startY = padding.top + innerPadding;
    const endY = height - padding.bottom - innerPadding;
    const hGap = (endY - startY) / (count - 1);
    const yTickGap = (max - min) / (count - 1);

    drawRect(this.context, 0, 0, padding.left, endY + innerPadding + AXIS_TICK_LENGTH);
    drawRect(this.context, width - padding.right, 0, width, endY + innerPadding + AXIS_TICK_LENGTH);
    drawRect(this.context, 0, 0, width, padding.top);
    
    [...Array(count)].forEach((_, i) => {
      const y = hGap * i + startY;
      const label = format(yTickGap * (count - 1 - i) + min);
      
      drawLine(this.context, startX - AXIS_TICK_LENGTH, y, startX, y);
      drawText(this.context, `${label}`, startX - TEXT_MARGIN_RIGHT - AXIS_TICK_LENGTH, y + 3, { textAlign: 'end' });
    })
    drawLine(this.context, startX, startY - innerPadding, startX, endY + innerPadding);
  }
}