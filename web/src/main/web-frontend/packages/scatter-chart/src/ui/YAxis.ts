import { TEXT_MARGIN_RIGHT } from "../constants/ui";
import { DeepNonNullable, Padding, TickOption } from "../types/types";
import { drawLine, drawRect, drawText } from "../utils/draw";
import { Axis, AxisProps } from "./Axis";

export interface YAxisProps extends AxisProps {
  backgroundColor?: string;
};

export class YAxis extends Axis {
  private backgroundColor;
  
  constructor({
    backgroundColor,
    ...props
  }: YAxisProps) {
    super(props);
    this.priority = -2;
    this.isFixed = true;
    this.backgroundColor = backgroundColor;
  }

  public setPadding(padding: Padding) {
    super.setPadding(padding)
    this.render();
    return this;
  }

  public setSize(width: number, height: number){
    super.setSize(width, height);
    this.render();
    return this;
  }

  public render() {
    this.clear();
    
    const { min, max, tick, innerPadding, backgroundColor, strokeColor } = this;
    const { format, count, color, width: tickWidth, strokeColor: tickStrokeColor } = tick as DeepNonNullable<TickOption>;
    const padding = this.padding;
    const width = this.canvas.width / this.dpr;
    const height = this.canvas.height / this.dpr;
    
    const startX = padding.left;
    const startY = padding.top + innerPadding;
    const endY = height - padding.bottom - innerPadding;
    const hGap = (endY - startY) / (count - 1);
    const yTickGap = (max - min) / (count - 1);

    drawRect(this.context, 0, 0, padding.left, endY + innerPadding + tick?.width!, { color: backgroundColor });
    drawRect(this.context, width - padding.right, 0, width, endY + innerPadding + tick?.width!, { color: backgroundColor });
    drawRect(this.context, 0, 0, width, padding.top, { color: backgroundColor });
    [...Array(count)].forEach((_, i) => {
      const y = hGap * i + startY;
      const label = format(yTickGap * (count - 1 - i) + min);
      drawText(
        this.context, `${label}`, 
        startX - TEXT_MARGIN_RIGHT - tickWidth, 
        y + this.getTextHeight(label) / 4, 
        { textAlign: 'end', color }
      );
      drawLine(this.context, startX - tickWidth, y, startX, y, {color: tickStrokeColor});
    })
    drawLine(this.context, startX, startY - innerPadding, startX, endY + innerPadding, {color: strokeColor});
  }
}