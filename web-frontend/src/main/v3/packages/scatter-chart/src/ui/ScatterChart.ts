import merge from 'lodash.merge';
import Color from 'color';
import html2canvas from 'html2canvas';

import {
  AxisOption,
  BackgroundOption,
  Coord,
  DataOption,
  DeepNonNullable,
  GridOption,
  GuideOption,
  LegendOption,
  Padding,
  PointOption,
  ScatterDataType,
  DataStyleMap,
  RenderOption,
} from '../types/types';
import { Layer } from './Layer';
import { Viewport, ViewportEventCallback, ViewportEventTypes } from './Viewport';
import { drawArea, drawCircle, drawRect } from '../utils/draw';
import { YAxis } from './YAxis';
import { XAxis } from './XAxis';
import { COLORS, CONTAINER_HEIGHT, CONTAINER_PADDING, CONTAINER_WIDTH, LAYER_DEFAULT_PRIORITY } from '../constants/ui';
import { GridAxis } from './GridAxis';
import { Legend, LegendEventCallback, LegendEventTypes } from './Legend';
import { Guide, GuideEventCallback, GuideEventTypes } from './Guide';
import {
  defaultAxisOption,
  defaultBackgroundOption,
  defaultDataOption,
  defaultGridOption,
  defaultGuideOption,
  defaultLegendOption,
  defaultPointOption,
  defaultRenderOption,
} from '../constants/options';
import { getLongestText, getTickTexts } from '../utils/helper';

export interface ScatterChartOption {
  axis: { x: AxisOption; y: AxisOption };
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
  axis: { x: AxisOption; y: AxisOption };
  data: DataOption[];
  legend: LegendOption;
  guide: GuideOption;
  background: BackgroundOption;
  grid: GridOption;
  padding: DeepNonNullable<Padding>;
  point: PointOption;
  render: RenderOption;
}

export type ScatterChartEventsTypes = Exclude<GuideEventTypes | LegendEventTypes | ViewportEventTypes, 'change'>;
export type EventData<T> = T extends (...args: any[]) => void ? Parameters<T>[1] : never;
export type EventCallback<T> = T extends 'clickLegend'
  ? LegendEventCallback<T>
  : T extends GuideEventTypes
  ? GuideEventCallback<T>
  : T extends ViewportEventTypes
  ? ViewportEventCallback<T>
  : never;

export class ScatterChart {
  static REALTIME_MULTIPLE = 3;
  public viewport!: Viewport;
  protected options!: ScatterChartSettedOption;
  protected xAxis!: XAxis;
  protected yAxis!: YAxis;
  protected gridAxis!: GridAxis;
  protected legend?: Legend;
  protected guide?: Guide;
  protected data: ScatterDataType[] = [];
  private datas: { [key: string]: Coord[] } = {};
  private rootContainer;
  private dataStyleMap!: DataStyleMap;
  private dataLayers: { [key: string]: Layer } = {};
  private xRatio = 1;
  private yRatio = 1;
  private coordX = 0;
  private coordY = 0;
  private realtimeAxisMinX = 0;
  private realtimeAxisMaxX = 0;
  private realtimeCycle = 0;
  private width = 0;
  private height = 0;
  private t0 = 0;
  private reqAnimation = 0;
  private compositedPadding: DeepNonNullable<Padding>;

  constructor(rootContainer: HTMLElement, options: ScatterChartOption) {
    this.rootContainer = rootContainer;
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
      render: { ...defaultRenderOption, ...options.render },
    };
  }

  private setWidthAndHeight() {
    this.width = this.rootContainer.clientWidth || CONTAINER_WIDTH;
    this.height = this.rootContainer.clientHeight || CONTAINER_HEIGHT;
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
    });

    this.viewport.addLayer(this.yAxis);
    this.viewport.addLayer(this.xAxis);
    this.viewport.addLayer(this.gridAxis);
  }

  private setPadding() {
    const xAxisOption = this.xAxis.getOption();
    const yAxisOptoin = this.yAxis.getOption();
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
      bottom:
        maxXTickTextHeight +
        xTickPadding.top +
        xTickPadding.bottom +
        xAxisOption.tick!.width! +
        this.compositedPadding.bottom,
      left:
        (maxXTickTextWidth / 2 > maxYTickTextWidth ? maxXTickTextWidth / 2 : maxYTickTextWidth) +
        yTickPadding.left +
        yTickPadding.right +
        yAxisOptoin.tick!.width! +
        this.compositedPadding.left,
    };

    this.xAxis.setPadding(this.options.padding);
    this.yAxis.setPadding(this.options.padding);
    this.gridAxis.setPadding(this.options.padding);
  }

  private setRatio() {
    const axisOption = this.options?.axis;
    const xAxis = this.xAxis.getOption();
    const yAxis = this.yAxis.getOption();
    const padding = this.options.padding;
    const width = this.viewport.canvas.width / this.viewport.viewLayer.dpr;
    const height = this.viewport.canvas.height / this.viewport.viewLayer.dpr;
    const minX = xAxis.min;
    const maxX = xAxis.max;
    const minY = yAxis.min;
    const maxY = yAxis.max;
    const innerPaddingX = axisOption.x.padding ?? this.xAxis.innerPadding;
    const innerPaddingY = axisOption.y.padding ?? this.yAxis?.innerPadding;

    this.xRatio = (width - padding.left - padding.right - innerPaddingX * 2) / (maxX - minX);
    this.yRatio = (height - padding.bottom - padding.top - innerPaddingY * 2) / (maxY - minY);
  }

  private setGuide() {
    if (!this.options.guide.hidden) {
      this.guide = new Guide(this.viewport.containerElement, {
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
      });
    }
  }

  private setLayers() {
    const width = this.viewport.styleWidth;
    const height = this.viewport.styleHeight;
    const dataOptions = this.options.data;
    this.setDataStyle(dataOptions);

    dataOptions.forEach(({ type, priority = LAYER_DEFAULT_PRIORITY }) => {
      this.setLayer(type, width, height, priority);
    });
  }

  private setViewPort() {
    this.viewport = new Viewport(this.rootContainer, { width: this.width, height: this.height });
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
          shape: curr.shape || 'point',
          color: color,
          legend: ogColor,
          radius: curr.radius || this.options.point.radius,
        },
      };
    }, {});
  };

  private setLegends() {
    if (!this.options.legend.hidden) {
      this.legend = new Legend(this.rootContainer, {
        dataStyleMap: this.dataStyleMap,
        legendOptions: this.options!.legend!,
      });

      this.legend.onChange((_, { checked, unChecked }) => {
        checked.forEach((type) => this.viewport.showLayer(type));
        unChecked.forEach((type) => this.viewport.hideLayer(type));
        this.shoot();
      });

      this.legend.render();
    }
  }

  private setLegendCount({
    type,
    minCoord,
    maxCoord,
    drawOutOfRange,
  }: {
    type: string;
    minCoord: Coord;
    maxCoord: Coord;
    drawOutOfRange: RenderOption['drawOutOfRange'];
  }) {
    const count =
      this.datas[type]?.reduce((acc, curr) => {
        const isInRangeX = curr.x >= minCoord.x && curr.x <= maxCoord.x;
        const isInRangeY = drawOutOfRange ? curr.y >= minCoord.y : curr.y >= minCoord.y && curr.y <= maxCoord.y;
        if (isInRangeX && isInRangeY) {
          return ++acc;
        }
        return acc;
      }, 0) || 0;
    this.legend?.setLegendCount(type, count);
  }

  private shoot() {
    this.viewport.clear();
    drawRect(this.viewport.context, 0, 0, this.width, this.height, {
      color: this.options.background?.color,
    });
    this.viewport.render(this.coordX, this.coordY);
  }

  private animate(duration: number, now: number) {
    this.shoot();
    if (!this.t0) this.t0 = now;
    const dt = now - this.t0;
    const innerPadding = this.xAxis.innerPadding;
    const pureWidth =
      this.viewport.styleWidth - this.options.padding.left - this.options.padding.right - innerPadding * 2;
    const pixcelPerFrame = (pureWidth / duration) * dt;
    this.t0 = now;
    this.coordX = this.coordX - pixcelPerFrame;
    this.realtimeCycle++;

    if (this.realtimeCycle % 15 === 0) {
      const x = Math.abs(this.coordX + innerPadding) / this.xRatio + this.realtimeAxisMinX;

      Object.keys(this.datas).forEach((key) => {
        this.datas[key] = this.datas[key].filter((d) => d.x > x);
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
      });
      this.guide?.updateMinX(Math.abs(this.coordX) / this.xRatio + this.realtimeAxisMinX);
      this.realtimeCycle = 0;
    }

    if (this.coordX + innerPadding < -pureWidth) {
      const nextAxisMinX =
        this.realtimeAxisMinX + (this.realtimeAxisMaxX - this.realtimeAxisMinX) / ScatterChart.REALTIME_MULTIPLE;
      const nextAxisMaxX =
        this.realtimeAxisMaxX + (this.realtimeAxisMaxX - this.realtimeAxisMinX) / ScatterChart.REALTIME_MULTIPLE;
      this.realtimeAxisMinX = nextAxisMinX;
      this.realtimeAxisMaxX = nextAxisMaxX;

      this.coordX = this.coordX + pureWidth;
      this.xAxis
        .setOption({
          min: this.realtimeAxisMinX,
          max: this.realtimeAxisMaxX,
        })
        .render();

      Object.values(this.dataLayers).forEach((layer) => {
        if (!layer.isFixed) {
          layer.swapCanvasImage({
            width: pureWidth,
            startAt: this.xAxis.innerPadding + this.options.padding.left,
          });
        }
      });
    }
    this.reqAnimation = requestAnimationFrame((t) => this.animate(duration, t));
  }

  private addNewDataType = (type: string) => {
    const { styleWidth, styleHeight } = this.viewport;
    this.options.data = [...this.options.data, { type }];
    this.setDataStyle(this.options.data);
    this.setLayer(type, styleWidth, styleHeight, LAYER_DEFAULT_PRIORITY);
    this.legend?.unmount().setDataStyleMap(this.dataStyleMap).render();
  };

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
        // clear all data layers
        Object.values(this.dataLayers).forEach((layer) => layer.clear());
      }
    }

    data.forEach(({ x, y, type, hidden }) => {
      const legend = type ? type : 'unknown';
      const dataStyle = this.dataStyleMap[legend];
      const radius = dataStyle?.radius;
      if (!this.dataLayers[legend]) {
        this.addNewDataType(legend);
      }

      if (this.datas[legend]) {
        this.datas[legend].push({ x, y });
      } else {
        this.datas[legend] = [{ x, y }];
      }

      const isInRangeX = x >= this.xAxis.min && x <= this.xAxis.max;
      const isInRangeY = renderOption.drawOutOfRange ? y >= this.yAxis.min : y >= this.yAxis.min && y <= this.yAxis.max;

      if (isInRangeX && isInRangeY) {
        const xCoordinate = this.xRatio * (x - this.xAxis.min) + padding.left + this.xAxis.innerPadding;
        const yCoordinate =
          renderOption.drawOutOfRange && y > this.yAxis.max
            ? styleHeight - padding.bottom - this.yAxis.innerPadding - this.yRatio * (this.yAxis.max - this.yAxis.min)
            : styleHeight - padding.bottom - this.yAxis.innerPadding - this.yRatio * (y - this.yAxis.min);

        if (!hidden) {
          if (dataStyle.shape === 'point') {
            drawCircle(this.dataLayers[legend].context, xCoordinate, yCoordinate, {
              fillColor: dataStyle?.color,
              radius: radius,
            });
          } else if (dataStyle.shape === 'area') {
            const dataLength = this.datas[legend].length;
            if (dataLength > 1) {
              const startData = this.datas[legend][dataLength - 2];
              const xCoordinateStart =
                this.xRatio * (startData.x - this.xAxis.min) + padding.left + this.xAxis.innerPadding;
              const yCoordinateStart =
                styleHeight - padding.bottom - this.yAxis.innerPadding - this.yRatio * (startData.y - this.yAxis.min);
              const ctx = this.dataLayers[legend].context;

              drawArea(
                ctx,
                xCoordinateStart,
                yCoordinateStart,
                xCoordinate,
                yCoordinate,
                styleHeight - padding.bottom - this.yAxis.innerPadding,
                {
                  color: dataStyle.color,
                },
              );
            }
          }
        }
      }
    });

    Object.keys(this.dataLayers).forEach((key) => {
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
      });
    });

    this.shoot();
  }

  public on<T extends ScatterChartEventsTypes>(
    eventType: T,
    callback: (event: MouseEvent, data: EventData<EventCallback<T>>) => void,
  ) {
    if (eventType === 'clickLegend') {
      this.legend?.on(eventType, callback as LegendEventCallback<LegendEventTypes>);
    } else if (eventType === 'resize') {
      this.viewport.on(eventType, callback as unknown as ViewportEventCallback<ViewportEventTypes>);
    } else {
      this.guide?.on(eventType, callback as GuideEventCallback<GuideEventTypes>);
    }
  }

  public off<T extends ScatterChartEventsTypes>(eventType: T) {
    if (eventType === 'clickLegend') {
      this.legend?.off(eventType);
    } else if (eventType === 'resize') {
      this.viewport.off(eventType);
    } else {
      this.guide?.off(eventType);
    }
  }

  public resize(width?: number, height?: number) {
    const w = width || this.viewport.containerElement.clientWidth;
    const h = height || this.viewport.containerElement.clientHeight;
    this.width = w;
    this.height = h;

    this.viewport.setSize(w, h, true);
    this.setRatio();
    this.guide?.setOptions({
      width: w,
      height: h,
      xAxis: this.xAxis,
      yAxis: this.yAxis,
      ratio: { x: this.xRatio, y: this.yRatio },
    });
    Object.values(this.dataLayers).forEach((layer) => layer.setSize(w, h));
    this.legend?.setSize(w);
    this.render(this.data);
  }

  public setOption({
    axis,
    render,
  }: {
    axis?: {
      x?: Partial<AxisOption>;
      y?: Partial<AxisOption>;
    };
    render?: RenderOption;
  }) {
    this.setOptions(merge(this.options, { render }));
    this.xAxis.setOption(axis?.x);
    this.yAxis.setOption(axis?.y);
    this.setPadding();
    this.setRatio();
    this.guide?.setOptions({
      xAxis: this.xAxis,
      yAxis: this.yAxis,
      padding: this.options.padding,
      ratio: { x: this.xRatio, y: this.yRatio },
    });
    this.render(this.data);
  }

  public async toBase64Image() {
    const layer = new Layer({ width: this.width, height: this.height });
    const containerCanvas = await html2canvas(document.querySelector(`.${Viewport.VIEW_CONTAINER_CLASS}`)!);
    const legendCanvas = await html2canvas(document.querySelector(`.${Legend.LEGEND_CONTAINER_CLASS}`)!);

    layer.setSize(containerCanvas.width, containerCanvas.height + legendCanvas.height);
    layer.context.drawImage(containerCanvas, 0, 0);
    layer.context.drawImage(legendCanvas, 0, containerCanvas.height);

    const image = layer.canvas.toDataURL('image/png').replace('image/png', 'image/octet-stream');

    return image;
  }

  public startRealtime(duration: number) {
    if (this.reqAnimation) return;
    const xAxisOption = this.xAxis.getOption();
    const realtimeWidth =
      this.width * ScatterChart.REALTIME_MULTIPLE -
      (this.options.padding.left + this.options.padding.right + this.xAxis.innerPadding * 2) *
        (ScatterChart.REALTIME_MULTIPLE - 1);
    this.realtimeAxisMinX = xAxisOption.min;
    this.realtimeAxisMaxX = (xAxisOption.max - xAxisOption.min) * ScatterChart.REALTIME_MULTIPLE + xAxisOption.min;
    this.coordX = -this.xAxis.innerPadding;

    this.xAxis
      .setSize(realtimeWidth, this.height)
      .setOption({
        min: this.realtimeAxisMinX,
        max: this.realtimeAxisMaxX,
        tick: {
          count: xAxisOption.tick!.count! * ScatterChart.REALTIME_MULTIPLE - (ScatterChart.REALTIME_MULTIPLE - 1),
        },
      })
      .render();

    this.gridAxis.setSize(realtimeWidth, this.height);

    Object.values(this.dataLayers).forEach((layer) => {
      layer.setSize(realtimeWidth, this.height);
    });
    this.render(this.data);

    this.animate(duration, this.t0);
  }

  public stopRealtime() {
    if (!this.reqAnimation) return;

    cancelAnimationFrame(this.reqAnimation);
    const xAxisOption = this.xAxis.getOption();
    this.reqAnimation = 0;
    this.coordX = 0;
    this.t0 = 0;

    this.xAxis
      .setSize(this.width, this.height)
      .setOption(
        merge({}, xAxisOption, {
          tick: {
            count: (xAxisOption.tick!.count! + (ScatterChart.REALTIME_MULTIPLE - 1)) / ScatterChart.REALTIME_MULTIPLE,
          },
        }),
      )
      .render();
    this.realtimeAxisMinX = xAxisOption.min;
    this.realtimeAxisMaxX = xAxisOption.max;

    this.guide?.setOptions({ xAxis: this.xAxis });

    this.gridAxis.setSize(this.width, this.height).render();

    this.render(this.data);
  }

  get isRealtime() {
    return !!this.reqAnimation;
  }

  public clear() {
    this.render([]);
  }

  public getOption() {
    return {
      ...this.options,
      axis: {
        x: this.xAxis.getOption(),
        y: this.yAxis.getOption(),
      },
    };
  }

  public destroy() {
    this.viewport.destroy();
    this.guide?.destroy();
    this.legend?.destroy();
  }
}
