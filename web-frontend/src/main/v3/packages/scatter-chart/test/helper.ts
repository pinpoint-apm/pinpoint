import { ScatterChart } from '../src';
import { ScatterChartOption } from '../src/ui/ScatterChart';

export class ScatterChartTestHelper extends ScatterChart {
  constructor(wrapper: HTMLElement, options: ScatterChartOption) {
    super(wrapper, options);
  }

  getXAxis() {
    return this.xAxis;
  }

  getYAxis() {
    return this.yAxis;
  }

  getGridAxis() {
    return this.gridAxis;
  }

  getLegend() {
    return this.legend;
  }

  getGuide() {
    return this.guide;
  }

  getData() {
    return this.data;
  }
}

export const initOption = {
  axis: {
    x: {
      min: 1669103462000,
      max: 1669103509335,
      tick: {
        count: 5,
      },
    },
    y: {
      min: 0,
      max: 10000,
      tick: {
        count: 5,
      },
    },
  },
  data: [
    {
      type: 'success',
      color: 'green',
      priority: 11,
    },
    {
      type: 'fail',
      color: 'red',
      priority: 1,
    },
  ],
};

export const simulateDrag = (canvas: HTMLCanvasElement, startX: number, startY: number, endX: number, endY: number) => {
  const mouseDownEvent = new MouseEvent('mousedown', {
    clientX: startX,
    clientY: startY,
  });
  canvas.dispatchEvent(mouseDownEvent);

  const mouseMoveEvent = new MouseEvent('mousemove', {
    clientX: endX,
    clientY: endY,
  });
  canvas.dispatchEvent(mouseMoveEvent);

  const mouseUpEvent = new MouseEvent('mouseup', {
    clientX: endX,
    clientY: endY,
  });
  canvas.dispatchEvent(mouseUpEvent);
};
