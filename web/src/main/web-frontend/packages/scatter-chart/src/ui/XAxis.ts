import { AXIS_INNER_PADDING, AXIS_TICK_LENGTH, TEXT_MARGIN_BOTTOM } from "../constants/ui";
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
    const { format, count } = this.tickOption;
    const padding = this.padding;
    const width = this.canvas.width / this.dpr;
    const height = this.canvas.height / this.dpr;

    const startX = padding.left + AXIS_INNER_PADDING;
    const endX = width - padding.right - AXIS_INNER_PADDING;
    const endY = height - padding.bottom; 
    const wGap = (endX - startX) / (count - 1);
    const xTickGap = (this.max - this.min) / (count - 1);

    [...Array(count)].forEach((_ , i) => {
      const x = wGap * i + startX;
      const label = format(xTickGap * i + this.min)
      drawText(this.context, `${label}`, x, height - TEXT_MARGIN_BOTTOM, { textAlign: 'center', textBaseline: 'bottom' });
      drawLine(this.context, x, endY, x, endY + AXIS_TICK_LENGTH);
    })
    drawLine(this.context, startX - AXIS_INNER_PADDING, endY, endX  + AXIS_INNER_PADDING, endY);
  }
}