import { TEXT_MARGIN_TOP } from "../constants/ui";
import { DeepNonNullable, Padding, TickOption } from "../types/types";
import { drawLine, drawText } from "../utils/draw";
import { Axis, AxisProps } from "./Axis";

export interface XAxisProps extends AxisProps { };

export class XAxis extends Axis {
  constructor({
    ...props
  }: XAxisProps) {
    super(props);
    this.priority = -1;
  }

  public setPadding(padding: Padding) {
    super.setPadding(padding)
    this.render();
    return this;
  }

  public setSize(width: number, height: number) {
    super.setSize(width, height);
    this.render();
    return this;
  }

  public render() {
    this.clear();
    const { min, max, tick, innerPadding, strokeColor } = this;
    const { format, count, color, width: tickWidth, strokeColor: tickStrokeColor } = tick as DeepNonNullable<TickOption>;
    const padding = this.padding;
    const width = this.canvas.width / this.dpr;
    const height = this.canvas.height / this.dpr;
    const startX = padding.left + innerPadding;
    const endX = width - padding.right - innerPadding;
    const endY = height - padding.bottom;
    const wGap = (endX - startX) / (count - 1);
    const xTickGap = (max - min) / (count - 1);

    [...Array(count)].forEach((_, i) => {
      const x = wGap * i + startX;
      const label = format(xTickGap * i + min);
      const textHeight = this.getTextHeight(label);
      drawText(
        this.context, `${label}`,
        x,
        height - padding.bottom + tick?.width! + textHeight + TEXT_MARGIN_TOP,
        { textAlign: 'center', textBaseline: 'bottom', color }
      );
      drawLine(this.context, x, endY, x, endY + tickWidth, { color: tickStrokeColor });
    })
    drawLine(this.context, startX - innerPadding, endY, endX + innerPadding, endY, { color: strokeColor });
  }
}