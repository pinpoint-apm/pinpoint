import merge from 'lodash.merge';
import Color from 'color';
import html2canvas from 'html2canvas';

import { AxisOption, BackgroundOption, Coord, DataOption, DeepNonNullable, GridOption, GuideOption, LegendOption, Padding, PointOption, ScatterDataType, DataStyleMap, RenderOption } from "../types/types";
import { Layer } from "./Layer";
import { Viewport } from "./Viewport";
import { drawCircle, drawRect } from "../utils/draw";
import { YAxis } from "./YAxis";
import { XAxis } from "./XAxis";
import {
  COLORS, CONTAINER_HEIGHT, CONTAINER_PADDING,
  CONTAINER_WIDTH, LAYER_DEFAULT_PRIORITY, SCATTER_CHART_IDENTIFIER
} from "../constants/ui";
import { GridAxis } from "./GridAxis";
import { Legend, LegendEventCallback, LegendEventTypes } from "./Legend";
import { Guide, GuideEventCallback, GuideEventTypes } from "./Guide";
import { defaultAxisOption, defaultBackgroundOption, defaultDataOption, defaultGridOption, defaultGuideOption, defaultLegendOption, defaultPointOption, defaultRenderOption } from "../constants/options";
import { getLongestText, getTickTexts } from '../utils/helper';

export interface ScatterChartOption {
  axis: { x: AxisOption, y: AxisOption };
  data: DataOption[];
  legend?: LegendOption;
  guide?: GuideOption;
  background?: BackgroundOption;
  grid?: GridOption;
  padding?: Padding;
  point?: PointOption;
  render?: RenderOption;
}

interface ScatterChartSettedOption {
  axis: { x: AxisOption, y: AxisOption };
  data: DataOption[];
  legend: LegendOption;
  guide: GuideOption;
  background: BackgroundOption;
  grid: GridOption;
  padding: DeepNonNullable<Padding>;
  point: PointOption;
  render: RenderOption;
}

export type ScatterChartEventsTypes = Exclude<GuideEventTypes | LegendEventTypes, 'change'>;
export type EventData<T> = T extends (...args: any[]) => void ? Parameters<T>[1] : never;
export type EventCallback<T> = T extends 'clickLegend' ? LegendEventCallback<T> : T extends 'click' | 'dragEnd' ? GuideEventCallback<T> : never;

export class ScatterChart {
  static REALTIME_MULTIPLE = 3;
  static SCATTER_CHART_CONTAINER_CLASS = `${SCATTER_CHART_IDENTIFIER}container`;
  public viewport!: Viewport;
  protected options!: ScatterChartSettedOption;
  protected xAxis!: XAxis;
  protected yAxis!: YAxis;
  protected gridAxis!: GridAxis;
  protected legend!: Legend;
  protected guide!: Guide;
  protected data: ScatterDataType[] = [];
  private datas: { [key: string]: Coord[] } = {};
  private wrapper;
  private canvasWrapper;
  private dataStyleMap!: DataStyleMap;
  private dataLayers: { [key: string]: Layer } = {};
  private xRatio = 1;
  private yRatio = 1;
  private coordX = 0;
  private coordY = 0;
  private realtimeAxisMinX = 0;
  private realtimeAxisMaxX = 0;
  private width = 0;
  private height = 0;
  private t0: number = 0;
  private reqAnimation = 0;
  private compositedPadding: DeepNonNullable<Padding>;

  constructor(wrapper: HTMLElement, options: ScatterChartOption) {
    this.wrapper = wrapper;
    this.canvasWrapper = document.createElement('div');
    this.canvasWrapper.className = ScatterChart.SCATTER_CHART_CONTAINER_CLASS;
    this.canvasWrapper.style.position = 'relative';
    this.wrapper.append(this.canvasWrapper);
    this.compositedPadding = { ...CONTAINER_PADDING, ...options.padding };

    this.setOptions(options);
    this.setWidthAndHeight();
    this.setViewPort();
    this.setAxis();
    this.setPadding();
    this.setRatio();
    this.setGuide();
    this.setLayers();
    this.setLegends();

    this.shoot();

    this.animate = this.animate.bind(this);
  }

  private setOptions(options: ScatterChartOption) {
    this.options = {
      // TODO deep copy
      axis: merge({}, defaultAxisOption, options?.axis),
      data: [...defaultDataOption, ...options?.data],
      legend: merge({}, defaultLegendOption, options?.legend),
      guide: merge({}, defaultGuideOption, options?.guide),
      background: merge({}, defaultBackgroundOption, options?.background),
      grid: { ...defaultGridOption, ...options?.grid },
      padding: { ...this.compositedPadding, ...options?.padding },
      point: { ...defaultPointOption, ...options.point },
      render: { ...defaultRenderOption, ...options.render }
    };
  }

  private setWidthAndHeight() {
    this.width = this.canvasWrapper.clientWidth || CONTAINER_WIDTH;
    this.height = this.canvasWrapper.clientHeight || CONTAINER_HEIGHT;
  }

  private setAxis() {
    const options = this.options;

    this.yAxis = new YAxis({
      option: options.axis.y,
      width: this.width,
      height: this.height,
      backgroundColor: options.background?.color,
    });

    this.xAxis = new XAxis({
      option: options.axis.x,
      width: this.width,
      height: this.height,
    });

    this.gridAxis = new GridAxis({
      option: options.grid,
      width: this.width,
      height: this.height,
      xAxis: this.xAxis,
      yAxis: this.yAxis,
    })

    this.viewport.addLayer(this.yAxis);
    this.viewport.addLayer(this.xAxis);
    this.viewport.addLayer(this.gridAxis);
  }

  private setPadding() {
    const { x: xAxisOption, y: yAxisOptoin } = this.options.axis;
    const xTicks = getTickTexts(xAxisOption);
    const yTicks = getTickTexts(yAxisOptoin);
    const maxXTickTextWidth = getLongestText(xTicks, (t) => this.xAxis.getTextWidth(t));
    const maxXTickTextHeight = getLongestText(xTicks, (t) => this.xAxis.getTextHeight(t));
    const maxYTickTextWidth = getLongestText(yTicks, (t) => this.yAxis.getTextWidth(t));
    const xTickPadding = this.xAxis.tick?.padding as DeepNonNullable<Padding>;
    const yTickPadding = this.yAxis.tick?.padding as DeepNonNullable<Padding>;

    this.options.padding = {
      top: this.compositedPadding.top,
      right: this.compositedPadding.right + maxXTickTextWidth / 2 + xTickPadding.right,
      bottom: maxXTickTextHeight + xTickPadding.top + xTickPadding.bottom + xAxisOption.tick?.width! + this.compositedPadding.bottom,
      left: (maxXTickTextWidth / 2 > maxYTickTextWidth ? maxXTickTextWidth / 2 : maxYTickTextWidth) + yTickPadding.left + yTickPadding.right + yAxisOptoin.tick?.width! + this.compositedPadding.left,
    }

    this.xAxis.setPadding(this.options.padding);
    this.yAxis.setPadding(this.options.padding);
    this.gridAxis.setPadding(this.options.padding);
  }

  private setRatio() {
    const axisOption = this.options?.axis;
    const padding = this.options.padding;
    const width = this.viewport.canvas.width / this.viewport.viewLayer.dpr;
    const height = this.viewport.canvas.height / this.viewport.viewLayer.dpr;
    const minX = axisOption.x.min;
    const maxX = axisOption.x.max;
    const minY = axisOption.y.min;
    const maxY = axisOption.y.max;
    const innerPaddingX = axisOption.x.padding ?? this.xAxis.innerPadding;
    const innerPaddingY = axisOption.y.padding ?? this.yAxis?.innerPadding;

    this.xRatio = (width - padding.left - padding.right - innerPaddingX * 2) / (maxX - minX);
    this.yRatio = (height - padding.bottom - padding.top - innerPaddingY * 2) / (maxY - minY);
  }

  private setGuide() {
    this.guide = new Guide(
      this.canvasWrapper,
      {
        width: this.width,
        height: this.height,
        padding: this.options.padding,
        xAxis: this.xAxis,
        yAxis: this.yAxis,
        ratio: {
          x: this.xRatio,
          y: this.yRatio,
        },
        option: this.options.guide!,
      }
    )
  }

  private setLayers() {
    const width = this.viewport.styleWidth;
    const height = this.viewport.styleHeight;
    const dataOptions = this.options.data;
    this.setDataStyle(dataOptions)

    dataOptions.forEach(({ type, priority = LAYER_DEFAULT_PRIORITY }) => {
      this.setLayer(type, width, height, priority);
    })
  }

  private setViewPort() {
    this.viewport = new Viewport(
      this.canvasWrapper,
      { width: this.width, height: this.height }
    );
  }

  private setLayer(legend: string, width: number, height: number, priority: number) {
    const layer = new Layer({ width, height });
    layer.id = legend;
    layer.priority = priority;
    this.dataLayers[legend] = layer;
    this.viewport.addLayer(layer);
    return layer;
  }

  private setDataStyle = (data: DataOption[]) => {
    this.dataStyleMap = data.reduce((prev, curr, i) => {
      const opacity = curr.opacity || this.options.point.opacity || 1;
      const ogColor = curr.color?.trim() || COLORS[i % COLORS.length];
      const color = Color(ogColor).alpha(opacity);

      return {
        ...prev,
        [curr.type]: {
          point: color,
          legend: ogColor,
          radius: curr.radius || this.options.point.radius,
        },
      }
    }, {});
  }

  private setLegends() {
    this.legend = new Legend(this.wrapper, {
      dataStyleMap: this.dataStyleMap,
      legendOptions: this.options?.legend!
    });

    this.legend.onChange((_, { checked, unChecked }) => {
      checked.forEach(type => this.viewport.showLayer(type));
      unChecked.forEach(type => this.viewport.hideLayer(type));
      this.shoot();
    })

    this.legend.render();
  }

  private setLegendCount({
    type, 
    minCoord, 
    maxCoord,
    drawOutOfRange,
  } : { 
    type: string, 
    minCoord: Coord, 
    maxCoord: Coord,
    drawOutOfRange: RenderOption['drawOutOfRange']
  }) {
    const count = this.datas[type]?.reduce((acc, curr) => {
      const isInRangeX = curr.x >= minCoord.x && curr.x <= maxCoord.x;
      const isInRangeY = drawOutOfRange 
        ? curr.y >= minCoord.y
        : curr.y >= minCoord.y && curr.y <= maxCoord.y;
      if (isInRangeX && isInRangeY) {
        return ++acc;
      }
      return acc;
    }, 0) || 0;
    this.legend.setLegendCount(type, count);
  }

  private shoot() {
    this.viewport.clear();
    drawRect(this.viewport.context, 0, 0, this.width, this.height, { color: this.options.background?.color });
    this.viewport.render(this.coordX, this.coordY);
  }

  private animate(duration: number, now: number) {
    this.shoot();
    if (!this.t0) this.t0 = now;
    const dt = now - this.t0;
    const innerPadding = this.xAxis.innerPadding;
    const pureWidth = this.viewport.styleWidth - this.options.padding.left - this.options.padding.right - innerPadding * 2;
    const pixcelPerFrame = pureWidth / duration * dt;
    const pixcelPerSecond = pixcelPerFrame * 60;
    this.t0 = now;
    this.coordX = this.coordX - pixcelPerFrame;
    
    if (Math.abs(Math.floor(this.coordX)) % (Math.floor(pixcelPerSecond / 4)) === 0) {
      const x = Math.abs(this.coordX + innerPadding) / this.xRatio + this.realtimeAxisMinX;
 
      Object.keys(this.datas).forEach(key => {
        this.datas[key] = this.datas[key].filter(d => d.x > x);
        this.setLegendCount({
          type: key,
          minCoord: {
            x: x,
            y: this.yAxis.min,
          },
          maxCoord: {
            x: x + this.xAxis.max - this.xAxis.min,
            y: this.yAxis.max,
          },
          drawOutOfRange: this.options.render.drawOutOfRange,
        });
      })
      this.guide.updateMinX(Math.abs(this.coordX) / this.xRatio + this.realtimeAxisMinX);
    }

    if (this.coordX + innerPadding < -pureWidth) {
      const nextAxisMinX = this.realtimeAxisMinX + (this.realtimeAxisMaxX - this.realtimeAxisMinX) / ScatterChart.REALTIME_MULTIPLE;
      const nextAxisMaxX = this.realtimeAxisMaxX + (this.realtimeAxisMaxX - this.realtimeAxisMinX) / ScatterChart.REALTIME_MULTIPLE;
      this.realtimeAxisMinX = nextAxisMinX;
      this.realtimeAxisMaxX = nextAxisMaxX;

      this.coordX = this.coordX + pureWidth;;
      this.xAxis
        .setOption({
          min: this.realtimeAxisMinX,
          max: this.realtimeAxisMaxX,
        })
        .render();
      Object.values(this.dataLayers)
        .forEach(layer => {
          if (!layer.isFixed) {
            layer.swapCanvasImage({
              width: pureWidth,
              startAt: this.xAxis.innerPadding + this.options.padding.left,
            })
          }
        });
    }
    this.reqAnimation = requestAnimationFrame((t) => this.animate(duration, t));
  }

  private addNewDataType = (type: string) => {
    const { styleWidth, styleHeight } = this.viewport;
    this.options.data = [...this.options.data, { type }]
    this.setDataStyle(this.options.data);
    this.setLayer(type, styleWidth, styleHeight, LAYER_DEFAULT_PRIORITY);
    this.legend.unmount().setDataStyleMap(this.dataStyleMap).render();
  }

  public render(data: ScatterDataType[], option?: RenderOption) {
    const { styleHeight } = this.viewport;
    const { padding } = this.options;
    const renderOption = { ...this.options.render, ...option };

    if (this.reqAnimation === 0) {
      if (renderOption.append) {
        this.data = [...this.data, ...data];
      } else {
        this.data = data;
        this.datas = {};
        Object.values(this.dataLayers).forEach(layer => layer.clear());
      }
    }

    data.forEach(({ x, y, type, hidden }) => {
      const legend = type ? type : 'unknown';
      const radius = this.dataStyleMap[legend]?.radius;
      if (!this.dataLayers[legend]) {
        this.addNewDataType(legend);
      }

      if (this.datas[legend]) {
        this.datas[legend].push({ x, y });
      } else {
        this.datas[legend] = [{ x, y }];
      }

      const isInRangeX = x >= this.xAxis.min && x <= this.xAxis.max;
      const isInRangeY = renderOption.drawOutOfRange
        ? y >= this.yAxis.min
        : y >= this.yAxis.min && y <= this.yAxis.max;

      if (isInRangeX && isInRangeY) {
        const xCoordinate = this.xRatio * (x - this.xAxis.min) + padding.left + this.xAxis.innerPadding;
        const yCoordinate = renderOption.drawOutOfRange && y > this.yAxis.max
          ? styleHeight - padding.bottom - this.yAxis.innerPadding - this.yRatio * (this.yAxis.max - this.yAxis.min)
          : styleHeight - padding.bottom - this.yAxis.innerPadding - this.yRatio * (y - this.yAxis.min);
        
          !hidden && drawCircle(
          this.dataLayers[legend].context,
          xCoordinate,
          yCoordinate,
          {
            fillColor: this.dataStyleMap[legend]?.point,
            radius: radius,
          }
        );
      }
    });

    Object.keys(this.dataLayers).forEach(key => {
      this.setLegendCount({
        type: key,
        minCoord: {
          x: this.xAxis.min,
          y: this.yAxis.min,
        },
        maxCoord: {
          x: this.xAxis.max,
          y: this.yAxis.max,
        },
        drawOutOfRange: renderOption.drawOutOfRange,
      })
    })

    this.shoot();
  }

  public on<T extends ScatterChartEventsTypes>(eventType: T, callback: (event: MouseEvent, data: EventData<EventCallback<T>>) => void) {
    if (eventType === 'clickLegend') {
      this.legend.on(eventType, callback as LegendEventCallback<LegendEventTypes>)
    } else {
      this.guide.on(eventType, callback as GuideEventCallback<GuideEventTypes>);
    }
  }

  public off<T extends ScatterChartEventsTypes>(eventType: T) {
    if (eventType === 'clickLegend') {
      this.legend.off(eventType)
    } else {
      this.guide.off(eventType);
    }
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
      xAxis: this.xAxis,
      yAxis: this.yAxis,
      ratio: { x: this.xRatio, y: this.yRatio }
    });
    Object.values(this.dataLayers).forEach(layer => layer.setSize(w, h));
    this.legend.setSize(w);
    this.render(this.data);
  }

  public setOption({
    axis,
    render,
  }: {
    axis?: { 
      x?: Partial<AxisOption>, 
      y?: Partial<AxisOption>,
    },
    render?: RenderOption,
  }) {
    this.setOptions(merge(this.options, { axis, render }));
    this.xAxis.setOption(this.options.axis.x)
    this.yAxis.setOption(this.options.axis.y)
    this.setPadding();
    this.setRatio();
    this.guide.setOptions({
      xAxis: this.xAxis,
      yAxis: this.yAxis,
      padding: this.options.padding,
      ratio: { x: this.xRatio, y: this.yRatio },
    });
    this.render(this.data);
  }

  public async toBase64Image() {
    const layer = new Layer({ width: this.width, height: this.height });
    const containerCanvas = await html2canvas(document.querySelector(`.${ScatterChart.SCATTER_CHART_CONTAINER_CLASS}`)!);
    const legendCanvas = await html2canvas(document.querySelector(`.${Legend.LEGEND_CONTAINER_CLASS}`)!);

    layer.setSize(containerCanvas.width, containerCanvas.height + legendCanvas.height);
    layer.context.drawImage(containerCanvas, 0, 0);
    layer.context.drawImage(legendCanvas, 0, containerCanvas.height);

    const image = layer.canvas
      .toDataURL("image/png")
      .replace("image/png", "image/octet-stream");

    return image;
  }

  public startRealtime(duration: number) {
    if (this.reqAnimation) return;
    const axisOptions = this.options.axis;
    const realtimeWidth = this.width * ScatterChart.REALTIME_MULTIPLE - (this.options.padding.left + this.options.padding.right + this.xAxis.innerPadding * 2) * (ScatterChart.REALTIME_MULTIPLE - 1);
    this.realtimeAxisMinX = axisOptions.x.min;
    this.realtimeAxisMaxX = (axisOptions.x.max - axisOptions.x.min) * ScatterChart.REALTIME_MULTIPLE + axisOptions.x.min;
    this.coordX = -this.xAxis.innerPadding;

    this.xAxis
      .setSize(realtimeWidth, this.height)
      .setOption({
        min: this.realtimeAxisMinX,
        max: this.realtimeAxisMaxX,
        tick: { count: axisOptions.x.tick?.count! * ScatterChart.REALTIME_MULTIPLE - (ScatterChart.REALTIME_MULTIPLE - 1) },
      })
      .render();

    this.gridAxis
      .setSize(realtimeWidth, this.height)

    Object.values(this.dataLayers).forEach(layer => {
      layer.setSize(realtimeWidth, this.height);
    });
    this.render(this.data);

    this.animate(duration, this.t0);
  }

  public stopRealtime() {
    cancelAnimationFrame(this.reqAnimation);
    const axisOptions = this.options.axis;
    this.reqAnimation = 0;
    this.coordX = 0;
    this.t0 = 0;

    this.xAxis
      .setSize(this.width, this.height)
      .setOption(axisOptions.x)
      .render();
    this.realtimeAxisMinX = axisOptions.x.min;
    this.realtimeAxisMaxX = axisOptions.x.max;

    this.guide.setOptions({ xAxis: this.xAxis });

    this.gridAxis
      .setSize(this.width, this.height)
      .render();

    this.render(this.data);
  }

  public clear() {
    this.render([]);
  }
}
