import { AXIS_DEFAULT_TICK_COUNT, AXIS_INNER_PADDING, AXIS_TICK_LENGTH } from "../constants/ui";
import { drawLine, drawText } from "../utils/draw";
import { Axis, AxisProps } from "./Axis";

export interface GridAxisProps extends AxisProps {
  xTickCount?: number;
  yTickCount?: number;
};

export class GridAxis extends Axis {
  xTickCount;
  yTickCount;

  constructor({
    xTickCount,
    yTickCount,
    ...props
  }: GridAxisProps = {}) {
    super(props);
    this.xTickCount = xTickCount || AXIS_DEFAULT_TICK_COUNT;
    this.yTickCount = yTickCount || AXIS_DEFAULT_TICK_COUNT;
    this.render();
  }

  private renderXGrid() {
    const tickCount = this.xTickCount;
    const padding = this.padding;
    const width = this.canvas.width / this.dpr;
    const height = this.canvas.height / this.dpr;

    const startX = padding.left + AXIS_INNER_PADDING;
    const startY = padding.top;
    const endX = width - padding.right - AXIS_INNER_PADDING;
    const endY = height - padding.bottom; 
    const wGap = (endX - startX) / (tickCount - 1);

    [...Array(tickCount)].forEach((_ , i) => {
      const x = wGap * i + startX;
      drawLine(this.context, x, startY, x, endY + AXIS_TICK_LENGTH, { color: '#d1d1d1'});
    })
  }

  private renderYGrid() {
    const tickCount = this.yTickCount;
    const padding = this.padding;
    const width = this.canvas.width / this.dpr;
    const height = this.canvas.height / this.dpr;
    
    const startX = padding.left;
    const startY = padding.top + AXIS_INNER_PADDING;
    const endX = width - padding.right + AXIS_INNER_PADDING;
    const endY = height - padding.bottom - AXIS_INNER_PADDING;
    const hGap = (endY - startY) / (tickCount - 1);

    [...Array(tickCount)].forEach((_, i) => {
      const y = hGap * i + startY;
      drawLine(this.context, startX - AXIS_TICK_LENGTH, y, endX, y, { color: '#d1d1d1'});
    })
  }

  public render() {
    this.clear();
    this.renderXGrid();
    this.renderYGrid();    
  }
  
  public setSize(width: number, height: number){
    super.setSize(width, height);
    this.render();
    return this;
  }

  public setXTickCount(tick: number) {
    this.xTickCount = tick;
    return this;
  }

  public setYickCount(tick: number) {
    this.yTickCount = tick;
    return this;
  }
}