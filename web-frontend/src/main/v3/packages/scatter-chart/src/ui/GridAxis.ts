import { CONTAINER_PADDING } from '../constants/ui';
import { DeepNonNullable, GridOption, Padding, TickOption } from '../types/types';
import { drawLine } from '../utils/draw';
import { Axis } from './Axis';
import { Layer, LayerProps } from './Layer';

export interface GridAxisProps extends LayerProps {
  // xTickCount?: number;
  // yTickCount?: number;
  xAxis: Axis;
  yAxis: Axis;
  padding?: DeepNonNullable<Padding>;
  option?: GridOption;
}

export class GridAxis extends Layer {
  xAxis;
  yAxis;
  option;
  padding;

  constructor({ xAxis, yAxis, option, padding, ...props }: GridAxisProps) {
    super(props);
    this.xAxis = xAxis;
    this.yAxis = yAxis;
    this.option = option;
    this.padding = { ...CONTAINER_PADDING, ...padding };
    this.priority = 99999;

    if (option?.hidden) {
      this.hide();
    } else {
      this.show();
      this.render();
    }
  }

  private renderXGrid() {
    const { count } = this.xAxis.tick as DeepNonNullable<TickOption>;
    const { strokeColor } = this.option as DeepNonNullable<GridOption>;
    const padding = this.padding;
    const width = this.canvas.width / this.dpr;
    const height = this.canvas.height / this.dpr;

    const startX = padding.left + this.xAxis.innerPadding;
    const startY = padding.top;
    const endX = width - padding.right - this.xAxis.innerPadding;
    const endY = height - padding.bottom;
    const wGap = (endX - startX) / (count - 1);

    [...Array(count)].forEach((_, i) => {
      const x = wGap * i + startX;
      drawLine(this.context, x, startY, x, endY, { color: strokeColor });
    });
  }

  private renderYGrid() {
    const { count, width: tickWidth } = this.yAxis.tick as DeepNonNullable<TickOption>;
    const { strokeColor } = this.option as DeepNonNullable<GridOption>;
    const padding = this.padding;
    const width = this.canvas.width / this.dpr;
    const height = this.canvas.height / this.dpr;

    const startX = padding.left;
    const startY = padding.top + this.yAxis.innerPadding;
    const endX = width - padding.right + this.yAxis.innerPadding;
    const endY = height - padding.bottom - this.yAxis.innerPadding;
    const hGap = (endY - startY) / (count - 1);

    [...Array(count)].forEach((_, i) => {
      const y = hGap * i + startY;
      drawLine(this.context, startX - tickWidth, y, endX, y, { color: strokeColor });
    });
  }

  public render() {
    this.clear();
    this.renderXGrid();
    this.renderYGrid();
  }

  public setSize(width: number, height: number) {
    super.setSize(width, height);
    this.render();
    return this;
  }

  public setPadding(padding: Padding) {
    this.padding = { ...this.padding, ...padding };
    this.render();
    return this;
  }

  // public setXTickCount(tick: number) {
  //   this.xTickCount = tick;
  //   return this;
  // }

  // public setYickCount(tick: number) {
  //   this.yTickCount = tick;
  //   return this;
  // }
}
