import { ScatterChart } from "../src";
import { ScatterChartOption } from "../src/ui/ScatterChart";

export class ScatterChartTestHelper extends ScatterChart {
  constructor(wrapper: HTMLElement, options: ScatterChartOption) {
    super(wrapper, options);
  }

  getOptions() {
    return this.options;
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
}

export const initOption = {
  axis: {
    x: {
      min: 1669103462000,
      max: 1669103509335,
      tick: {
        count: 5,
      }
    },
    y: {
      min: 0,
      max: 10000,
      tick: {
        count: 5,
      }
    }
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