import merge from 'lodash.merge';
import html2canvas from 'html2canvas';

import { AxisOption, DataOption, LegendOption, Padding, ScatterDataType } from "../types";
import { Layer } from "./Layer";
import { Viewport } from "./Viewport";
import { drawCircle, drawRect, drawText } from "../utils/draw";
import { YAxis } from "./YAxis";
import { XAxis } from "./XAxis";
import { AXIS_INNER_PADDING, AXIS_TICK_LENGTH, CONTAINER_HEIGHT, CONTAINER_PADDING, CONTAINER_WIDTH, LAYER_DEFAULT_PRIORITY, SCATTER_CHART_IDENTIFIER, TEXT_MARGIN_BOTTOM, TEXT_MARGIN_LEFT, TEXT_MARGIN_RIGHT, TEXT_MARGIN_TOP } from "../constants/ui";
import { GridAxis } from "./GridAxis";
import { Legend } from "./Legend";
import { Guide } from "./Guide";
import { defaultAxisOption, defaultDataOption, defaultLegendOption } from "../constants/options";
import { getLongestTextWidth, getTickTexts } from '../utils/helper';

export interface ScatterChartOption {
  axis: { x: AxisOption, y: AxisOption };
  data: DataOption[];
  legend?: LegendOption;
}

export class ScatterChart {
  static SCATTER_CHART_CONTAINER_CLASS = `${SCATTER_CHART_IDENTIFIER}container`;
  private wrapper;
  private canvasWrapper;
  private options!: ScatterChartOption;
  private xAxis!: XAxis;
  private yAxis!: YAxis;
  private gridAxis!: GridAxis;
  private legend!: Legend;
  private dataColorMap!: { [key: string]: string };
  private viewport!: Viewport;
  private guide!: Guide;
  private dataLayers: { [key: string]: Layer } = {};
  private data: ScatterDataType[] = [];
  private datas: {[key: string]: number[]} = {};
  private xRatio = 1;
  private yRatio = 1;
  private coordX = 0;
  private coordY = 0;
  private realtimeAxisMinX = 0;
  private realtimeAxisMaxX = 0;
  private width = 0;
  private height = 0;
  private padding = CONTAINER_PADDING;
  private t0: number = 0;
  private reqAnimation = 0;

  constructor(wrapper: HTMLElement, options: ScatterChartOption) {
    this.wrapper = wrapper;
    this.canvasWrapper = document.createElement('div');
    this.canvasWrapper.className = ScatterChart.SCATTER_CHART_CONTAINER_CLASS;
    this.canvasWrapper.style.position = 'relative';
    this.wrapper.append(this.canvasWrapper);

    this.setOptions(options);
    this.setWidthAndHeight();
    this.setViewPort();
    this.setPadding();
    this.setRatio();
    this.setAxis();
    this.setGuide();
    this.setLayers();
    this.setLegends();

    this.shoot();
    this.animate = this.animate.bind(this);
  }

  private setOptions(options: ScatterChartOption) {
    this.options = {
      // TODO deep copy
      axis: merge(defaultAxisOption, options.axis),
      data: [...defaultDataOption, ...options.data],
      legend: merge(defaultLegendOption, options.legend),
    };

    this.dataColorMap = this.options.data.reduce((prev, curr) => {
      return {
        [curr.type]: curr.color,
        ...prev,
      }
    }, {});
  }

  private setWidthAndHeight() {
    this.width = this.canvasWrapper.clientWidth || CONTAINER_WIDTH;
    this.height = this.canvasWrapper.clientHeight || CONTAINER_HEIGHT;
  }

  private setPadding() {
    const xformatter = this.options.axis.x.tick?.format;
    const xTicks = getTickTexts(this.options.axis.x);
    const yTicks = getTickTexts(this.options.axis.y);
    const maxXTickTextWidth = getLongestTextWidth(xTicks, (t) => this.viewport.viewLayer.getTextWidth(t));
    const maxYTickTextWidth = getLongestTextWidth(yTicks, (t) => this.viewport.viewLayer.getTextWidth(t));
    const formattedXSample = xformatter ? xformatter(this.options.axis.x.max) : this.options.axis.x.max;

    this.padding.left = (maxXTickTextWidth / 2 > maxYTickTextWidth ? maxXTickTextWidth / 2 : maxYTickTextWidth) + TEXT_MARGIN_LEFT + TEXT_MARGIN_RIGHT + AXIS_TICK_LENGTH;
    this.padding.right = maxXTickTextWidth / 2 + TEXT_MARGIN_RIGHT;
    this.padding.bottom = this.viewport.viewLayer.getTextHeight(formattedXSample) + TEXT_MARGIN_TOP + TEXT_MARGIN_BOTTOM + AXIS_TICK_LENGTH;
  }

  private setViewPort() {
    this.viewport = new Viewport(
      this.canvasWrapper,
      { width: this.width, height: this.height }
    );
  }

  private setGuide() {
    this.guide = new Guide(
      this.canvasWrapper,
      { 
        width: this.width, 
        height: this.height, 
        padding: this.padding, 
        axisOption: this.options.axis,
        ratio: {
          x: this.xRatio,
          y: this.yRatio,
        }
      }
    )
  }

  private setAxis() {
    const options = this.options;

    this.yAxis = new YAxis({
      axisOption: options.axis.y,
      width: this.width, 
      height: this.height, 
      padding: this.padding,
      priority: -2,
      fixed: true,
    });

    this.xAxis = new XAxis({
      axisOption: options.axis.x,
      width: this.width, 
      height: this.height, 
      padding: this.padding,
      priority: -1,
    });

    this.gridAxis = new GridAxis({
      width: this.width, 
      height: this.height, 
      padding: this.padding,
      priority: 9999,
      xTickCount: options.axis.x.tick?.count,
      yTickCount: options.axis.y.tick?.count,
    })

    this.viewport.addLayer(this.yAxis);
    this.viewport.addLayer(this.xAxis);
    this.viewport.addLayer(this.gridAxis);
  }

  private setLayers() {
    const width = this.viewport.styleWidth;
    const height = this.viewport.styleHeight;
    const dataOptions = this.options.data;

    dataOptions.forEach(({ type, priority = LAYER_DEFAULT_PRIORITY }) => {
      this.setLayer(type, width, height, priority);
    })
  }

  private setLayer(legend: string, width: number, height: number, priority: number) {
    const layer = new Layer({ width, height });
    layer.id = legend;
    layer.priority = priority;
    this.dataLayers[legend] = layer;
    this.viewport.addLayer(layer);
  }

  private setLegends() {
    this.legend = new Legend(this.wrapper, { 
      types: Object.keys(this.dataLayers), 
      dataColorMap: this.dataColorMap,
      legendOptions: this.options?.legend!
    });
    this.legend.addEvents(({ type, checked }) => {
      if (type) {
        if (checked) {
          this.viewport.showLayer(type);
        } else {
          this.viewport.hideLayer(type);
        }
      }
      this.shoot();
    }).render();
  }

  private setRatio() {
    const axis = this.options?.axis;
    const padding = this.padding;
    const width = this.viewport.canvas.width / this.viewport.viewLayer.dpr;
    const height = this.viewport.canvas.height / this.viewport.viewLayer.dpr;
    const minX = axis.x.min;
    const maxX = axis.x.max;
    const minY = axis.y.min;
    const maxY = axis.y.max;

    this.xRatio = (width - padding.left - padding.right - AXIS_INNER_PADDING * 2) / (maxX - minX);
    this.yRatio = (height - padding.bottom - padding.top - AXIS_INNER_PADDING * 2) / (maxY - minY);
  }

  private shoot() {
    this.viewport.clear();
    drawRect(this.viewport.context, 0, 0, this.width, this.height);
    this.viewport.render(this.coordX, this.coordY);
    
    Object.keys(this.datas).forEach(key => {
      this.legend.setLegendCount(key, this.datas[key].length);
    })
  }

  private animate(duration: number, now: number) {
    this.shoot();
    if (!this.t0) this.t0 = now;
    const dt = now - this.t0;
    const pixcelPerFrame = (this.viewport.styleWidth - this.padding.left - this.padding.right - AXIS_INNER_PADDING * 2) / duration * dt;
    const pixcelPerSecond = pixcelPerFrame * 60;

    this.t0 = now;
    this.coordX = this.coordX - pixcelPerFrame;

    // 특정 주기마다 시행
    if (Math.abs(Math.floor(this.coordX)) % (Math.floor(pixcelPerSecond / 5)) === 0) {
      const x = Math.abs(this.coordX) / this.xRatio;
      Object.keys(this.datas).forEach(key => {
        this.datas[key] = this.datas[key].filter(d => d > x);
      })

      this.guide.updateXAxis({ min: x + this.xAxis.min});
    }

    if (this.coordX > -(this.viewport.styleWidth - this.padding.left - this.padding.right - AXIS_INNER_PADDING * 2)) {
      this.reqAnimation = requestAnimationFrame((t) => this.animate(duration, t))
    } else {
      // swap
      const nextAxisMinX = this.realtimeAxisMinX + (this.realtimeAxisMaxX - this.realtimeAxisMinX) / 2;
      const nextAxisMaxX = this.realtimeAxisMaxX + (this.realtimeAxisMaxX - this.realtimeAxisMinX) / 2;
      this.realtimeAxisMinX = nextAxisMinX;
      this.realtimeAxisMaxX = nextAxisMaxX;
      this.coordX = 0;

      this.xAxis
        .setMinMax(this.realtimeAxisMinX, this.realtimeAxisMaxX)
        .render();
      Object.values(this.dataLayers).filter(layer => !layer.isFixed).forEach(layer =>  layer.swapCanvasImage());
      this.reqAnimation = requestAnimationFrame((t) => this.animate(duration, t))
    }
  }

  public render(data: ScatterDataType[], { append = false } = {}) {
    const { styleWidth, styleHeight } = this.viewport;
    const { x: xAxisOptoin, y: yAxisOption } = this.options.axis
    const padding = this.padding;

    if (append) {
      this.data = [...this.data, ...data];
    } else {
      this.data = data;
      this.datas = {};
      Object.values(this.dataLayers).forEach(layer => layer.clear());
    }

    data.forEach(({ x, y, type, hidden }) => {
      const legend = type ? type : 'unknown';

      if (!this.dataLayers[legend]) {
        this.setLayer(legend, styleWidth, styleHeight, LAYER_DEFAULT_PRIORITY);
      }
      
      if (this.datas[legend]) {
        this.datas[legend].push(x);
      } else {
        this.datas[legend] = [x];
      }

      !hidden && drawCircle(
        this.dataLayers[legend].context,
        this.xRatio * (x - this.xAxis.min) + padding.left + AXIS_INNER_PADDING,
        this.viewport.canvas.height / this.viewport.viewLayer.dpr - this.yRatio * y - padding.bottom - AXIS_INNER_PADDING,
        {
          fillColor: this.dataColorMap[legend],
        }
      );
    });
    this.shoot();
  }

  public on(evetntType: string, callback: (data: any) => void) {
    this.guide.on(evetntType, callback);
  }

  public resize(width?: number, height?: number) {
    const w = width || this.canvasWrapper.clientWidth;
    const h = height || this.canvasWrapper.clientHeight;

    this.viewport.setSize(w, h);
    this.setRatio();
    this.xAxis.setSize(w, h);
    this.yAxis.setSize(w, h);
    this.gridAxis.setSize(w, h);
    this.guide.setOptions({
      width: w,
      height: h,
      ratio: { x: this.xRatio, y: this.yRatio }
    });
    Object.values(this.dataLayers).forEach(layer => layer.setSize(w, h));
    this.legend.setSize(w);
    this.render(this.data);
  }

  public setAxisOption(axisOption: {
    x?: Partial<AxisOption>,
    y?: Partial<AxisOption>,
  }) {
    this.setOptions(merge(this.options, { axis: axisOption }));
    this.setPadding();
    this.setRatio();
    this.guide.setOptions({
      padding: this.padding,
      ratio: {x: this.xRatio, y: this.yRatio},
    });
    this.xAxis
      .setOptions({
        ...this.options.axis.x,
        padding: this.padding,
      })
      .render();
    this.yAxis
      .setOptions({
        ...this.options.axis.y,
        padding: this.padding,
      })
      .render();
    this.gridAxis
      .setOptions({ padding: this.padding })
      .render();
    this.render(this.data);
  }

  public async toBase64Image() {
    const layer = new Layer({ width: this.width , height: this.height});
    const containerCanvas = await html2canvas(document.querySelector(`.${ScatterChart.SCATTER_CHART_CONTAINER_CLASS}`)!).then(canvas => canvas);
    const legendCanvas = await html2canvas(document.querySelector(`.${Legend.LEGEND_CONTAINER_CLASS}`)!).then(canvas => canvas);

    layer.setSize(containerCanvas.width, containerCanvas.height + legendCanvas.height);
    layer.context.drawImage(containerCanvas, 0, 0);
    layer.context.drawImage(legendCanvas, 0, containerCanvas.height);

    const image = layer.canvas
      .toDataURL("image/png")
      .replace("image/png", "image/octet-stream");

    return image;
  }

  // public startRealtime(duration: number) {
  //   if (this.reqAnimation) return;
  //   const axisOptions = this.options.axis;
  //   this.xAxis
  //     .setTickCount(axisOptions.x.tick?.count! * 2 - 1)
  //     .setSize(this.width * 2 - this.padding.left - AXIS_INNER_PADDING * 2 - this.padding.right, this.height)
  //     .setMinMax(axisOptions.x.min, axisOptions.x.max * 2 - axisOptions.x.min)
  //     .render();
  //   this.realtimeAxisMinX = axisOptions.x.min;
  //   this.realtimeAxisMaxX = axisOptions.x.max * 2 - axisOptions.x.min;

  //   this.gridAxis
  //     .setXTickCount(axisOptions.x.tick?.count! * 2 - 1)
  //     .setSize(this.width * 2 - this.padding.left - AXIS_INNER_PADDING * 2 - this.padding.right, this.height)
  //     .render();
  //   this.animate(duration, this.t0);
  // }

  // public stopRealtime() {
  //   cancelAnimationFrame(this.reqAnimation);
  //   const axisOptions = this.options.axis;
  //   this.reqAnimation = 0;
  //   this.coordX = 0;
  //   this.t0 = 0;

  //   this.xAxis
  //     .setTickCount(axisOptions.x.tick?.count!)
  //     .setSize(this.width, this.height)
  //     .setMinMax(axisOptions.x.min, axisOptions.x.max)
  //     .render();
  //   this.realtimeAxisMinX = axisOptions.x.min;
  //   this.realtimeAxisMaxX = axisOptions.x.max;

  //   this.guide.updateXAxis({ min: axisOptions.x.min });

  //   this.gridAxis
  //     .setXTickCount(axisOptions.x.tick?.count!)
  //     .setSize(this.width, this.height)
  //     .render();
  //   this.shoot();
  // }
}
