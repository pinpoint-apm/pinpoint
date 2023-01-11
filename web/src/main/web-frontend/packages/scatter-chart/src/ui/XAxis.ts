import { AXIS_TICK_LENGTH, TEXT_MARGIN_BOTTOM } from "../constants/ui";
import { TickOption } from "../types/types";
import { drawLine, drawText } from "../utils/draw";
import { Axis, AxisProps } from "./Axis";

export interface XAxisProps extends AxisProps {};

export class XAxis extends Axis {
  constructor({
    ...props
  }: XAxisProps = {}) {
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

    const startX = padding.left + innerPadding;
    const endX = width - padding.right - innerPadding;
    const endY = height - padding.bottom; 
    const wGap = (endX - startX) / (count - 1);
    const xTickGap = (max - min) / (count - 1);

    [...Array(count)].forEach((_ , i) => {
      const x = wGap * i + startX;
      const label = format(xTickGap * i + min);
      drawText(this.context, `${label}`, x, height - TEXT_MARGIN_BOTTOM, { textAlign: 'center', textBaseline: 'bottom' });
      drawLine(this.context, x, endY, x, endY + AXIS_TICK_LENGTH);
    })
    drawLine(this.context, startX - innerPadding, endY, endX  + innerPadding, endY);
  }
}