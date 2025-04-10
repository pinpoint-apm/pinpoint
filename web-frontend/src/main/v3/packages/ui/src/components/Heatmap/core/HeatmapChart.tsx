import React from 'react';
import ReactEChartsCore from 'echarts-for-react/lib/core';
import * as echarts from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { HeatmapChart as HeatmapChartEcharts } from 'echarts/charts';
import {
  GridComponent,
  TooltipComponent,
  VisualMapComponent,
  GraphicComponent,
} from 'echarts/components';
import { ECharts, EChartsOption } from 'echarts';
import { colors, GetHeatmapAppData } from '@pinpoint-fe/ui/src/constants';
import { capitalize, debounce, max } from 'lodash';
import { defaultTickFormatter } from '@pinpoint-fe/ui/src/components/ReChart';
import { HeatmapSettingType } from './HeatmapSetting';

echarts.use([
  CanvasRenderer, // 캔버스 렌더링
  HeatmapChartEcharts, // 히트맵 차트
  GridComponent, // 그리드
  TooltipComponent, // 툴팁
  VisualMapComponent, // 색상 범례
  GraphicComponent, // 그래픽
]);

echarts.use([HeatmapChartEcharts, CanvasRenderer]);

const colorSteps = 10;
export const HeatmapColor = {
  success: Array.from({ length: colorSteps }, (_, i) => {
    return colors.green[(i + 1) * 100 === 1000 ? 950 : (i + 1) * 100];
  }),
  fail: Array.from({ length: colorSteps }, (_, i) => {
    return colors.red[(i + 1) * 100 === 1000 ? 950 : (i + 1) * 100];
  }),
  selected: colors.yellow[200],
};

type HeatmapChartProps = {
  isLoading?: boolean;
  data?: GetHeatmapAppData.Response;
  setting: HeatmapSettingType;
};

type DataForRender = {
  value: [string, string, number];
  itemStyle?: {
    color: string;
    opacity: number;
  };
};

const HeatmapChart = React.forwardRef(
  ({ data, setting }: HeatmapChartProps, ref: React.Ref<HTMLDivElement>) => {
    const chartRef = React.useRef<ReactEChartsCore>(null);
    const [successRange, setSuccessRange] = React.useState(); // 성공 범위: [시작, 끝]
    const [failRange, setFailRange] = React.useState(); // 성공 범위: [시작, 끝]

    const [startCell, setStartCell] = React.useState(''); // 시작 셀: x-y
    const [endCell, setEndCell] = React.useState(''); // 끝 셀: x-y

    React.useEffect(() => {
      const wrapperElement = chartRef.current?.getEchartsInstance()?.getDom();

      if (!wrapperElement) return;

      const resizeObserver = new ResizeObserver(() => {
        chartRef.current?.getEchartsInstance()?.resize();
      });
      resizeObserver.observe(wrapperElement);

      return () => {
        resizeObserver.disconnect();
      };
    }, []);

    const isSelectedCell = React.useCallback(
      (x: number, y: number) => {
        const [startX, startY] = startCell.split('-').map(Number);
        const [endX, endY] = endCell.split('-').map(Number);

        if (!startX || !startY || !endX || !endY) {
          return false;
        }

        const left = Math.min(startX, endX);
        const right = Math.max(startX, endX);
        const top = Math.max(startY, endY);
        const bottom = Math.min(startY, endY);

        return x >= left && x <= right && y <= top && y >= bottom;
      },
      [startCell, endCell],
    );

    const maxCount = React.useMemo(() => {
      let success = 0;
      let fail = 0;

      const { heatmapData } = data || {};

      if (heatmapData) {
        for (const data of heatmapData) {
          for (const cell of data.cellDataList) {
            success = Math.max(success, cell.successCount);
            fail = Math.max(fail, cell.failCount);
          }
        }
      }

      return { success, fail };
    }, [data]);

    const dataForRender = React.useMemo(() => {
      const successData: DataForRender['value'][] = [];
      const failData: DataForRender['value'][] = [];
      const coverData: DataForRender[] = []; // 가장 위에 덮어져서 tooltip, select 이벤트를 받기 위한 것

      const { heatmapData } = data || {};
      heatmapData?.forEach((row) => {
        row?.cellDataList?.forEach((cell) => {
          coverData.push({
            value: [String(row.timestamp), String(cell.elapsedTime), 0],
            itemStyle: {
              color: isSelectedCell(row.timestamp, cell.elapsedTime)
                ? HeatmapColor.selected
                : 'transparent',
              opacity: 1,
            },
          });

          if (cell?.successCount) {
            successData.push([String(row.timestamp), String(cell.elapsedTime), cell.successCount]);
          }

          if (cell?.failCount) {
            failData.push([String(row.timestamp), String(cell.elapsedTime), cell.failCount]);
          }
        });
      });

      return { successData, failData, coverData };
    }, [data, startCell, endCell, maxCount]);

    const xAxisData = React.useMemo(() => {
      return data?.heatmapData?.map((row) => String(row.timestamp)) || [];
    }, [data]);
    const yAxisData = React.useMemo(() => {
      return data?.heatmapData?.[0].cellDataList?.map((cell) => String(cell.elapsedTime)) || [];
    }, [data]);

    const totalSuccessCount = React.useMemo(() => {
      return data?.summary?.totalSuccessCount || 0;
    }, [data]);
    const totalFailedCount = React.useMemo(() => {
      return data?.summary?.totalFailCount || 0;
    }, [data]);

    const option: EChartsOption = {
      tooltip: {
        show: !!startCell ? false : true,
        borderColor: colors.gray[300],
        textStyle: {
          fontFamily: 'inherit',
          fontSize: 8,
        },
        formatter: (params: any) => {
          const { data } = params;
          const [timestamp, elapsedTime] = data?.value;

          const failedCount = dataForRender?.failData.find((item: [string, string, number]) => {
            return item[0] === timestamp && item[1] === elapsedTime;
          })?.[2];

          const successCount = dataForRender?.successData.find((item: [string, string, number]) => {
            return item[0] === timestamp && item[1] === elapsedTime;
          })?.[2];

          return `
        <div style="display: flex; flex-direction: column; gap: 5px; padding: 2px;">
          <div style="margin-bottom: 5px;"><strong>${defaultTickFormatter(Number(timestamp))}</strong></div>
          ${['success', 'fail']
            .map((type) => {
              const count = type === 'success' ? successCount : failedCount;
              const color = HeatmapColor[type as 'success' | 'fail'][5];

              return `
              <div style="display: flex; justify-content: space-between; gap: 5px;">
                <div style="display: flex; gap: 6px; align-items: center;">
                  <div style="width: 8px; height: 8px; background: ${color}; border: ${count === 0 ? '1px solid black' : 'none'};"></div>${capitalize(type)}
                </div>
                <div>${count === undefined ? 0 : Number(count).toLocaleString()}</div>
              </div>
            `;
            })
            .join('')}
        </div>
        `;
        },
      },
      grid: {
        left: setting.yMax.toString().length * 10,
        right: '10px',
        top: '2%',
        bottom: '100px',
      },
      xAxis: {
        type: 'category',
        data: xAxisData.sort((a, b) => Number(a) - Number(b)),
        axisLabel: {
          interval: 'auto',
          showMaxLabel: true,
          showMinLabel: true,
          formatter: (value: string) => {
            return defaultTickFormatter(Number(value));
          },
        },
      },
      yAxis: {
        type: 'category',
        data: yAxisData.filter(
          (yValue) => Number(yValue) >= setting.yMin && Number(yValue) <= setting.yMax,
        ),
        offset: 1,
        axisLabel: {
          interval: (index: number, value: string) => {
            if (yAxisData.length <= 5) {
              return true;
            }

            const step = (yAxisData.length - 1) / 4;
            const positions = [
              0,
              ...Array.from({ length: 3 }, (_, i) => Math.round((i + 1) * step)),
              yAxisData.length - 1,
            ];

            return positions.includes(index);
          },
          formatter: (value: string) => {
            try {
              return Number(value).toLocaleString();
            } catch (err) {
              return value;
            }
          },
        },
      },
      visualMap: [
        {
          id: 'success',
          min: 0,
          max: maxCount.success,
          calculable: true,
          seriesIndex: 0,
          orient: 'horizontal',
          itemWidth: 14,
          itemHeight: (chartRef?.current?.getEchartsInstance()?.getWidth() || 100) * 0.3,
          right: '45%',
          bottom: '5%',
          hoverLink: false,
          formatter: (value) => {
            if (value === setting.yMax) {
              return '';
            }
            return Math.floor(Number(value)).toLocaleString();
          },
          inRange: {
            color: HeatmapColor.success,
          },
          range: successRange,
        },
        {
          id: 'fail',
          min: 0,
          max: maxCount.fail,
          calculable: true,
          seriesIndex: 1,
          orient: 'horizontal',
          itemWidth: 14,
          itemHeight: (chartRef?.current?.getEchartsInstance()?.getWidth() || 100) * 0.3,
          left: '55%',
          bottom: '5%',
          hoverLink: false,
          formatter: (value) => {
            return Math.floor(Number(value)).toLocaleString();
          },
          inRange: {
            color: HeatmapColor.fail,
          },
          range: failRange,
        },
        {
          id: 'cover',
          show: false,
          seriesIndex: 2,
        },
      ],
      graphic: [
        {
          type: 'text',
          bottom: '0%',
          left: 'center',
          style: {
            text: `Success {boldSuccess|${Math.floor(totalSuccessCount).toLocaleString()}}    Failed {boldFailed|${Math.floor(totalFailedCount).toLocaleString()}}`,
            fontSize: 15,
            fill: colors.gray[500],
            rich: {
              boldSuccess: {
                fontWeight: 'bold',
                fill: HeatmapColor.success[5],
              },
              boldFailed: {
                fontWeight: 'bold',
                fill: HeatmapColor.fail[5],
              },
            },
          },
        },
      ],
      series: [
        {
          name: 'success',
          type: 'heatmap',
          data: dataForRender?.successData,
        },
        {
          name: 'fail',
          type: 'heatmap',
          data: dataForRender?.failData,
        },
        {
          name: 'cover',
          type: 'heatmap',
          data: dataForRender?.coverData,
          emphasis: {
            itemStyle: {
              borderColor: '#333',
              borderWidth: 1,
            },
          },
        },
      ],
    };

    return (
      <div ref={ref} className="relative w-full h-full">
        <ReactEChartsCore
          ref={chartRef}
          echarts={echarts}
          option={option}
          style={{ height: '100%', width: '100%' }}
          // onEvents={{
          //   mousedown: (params: any, echartsInstance: ECharts) => {
          //     console.log('mousedown', params);
          //     setStartCell(`${params.value[0]}-${params.value[1]}`);
          //     setEndCell(`${params.value[0]}-${params.value[1]}`);
          //   },
          //   mousemove: (params: any) => {
          //     if (!startCell) {
          //       return;
          //     }
          //     setEndCell(`${params.value[0]}-${params.value[1]}`);
          //   },
          //   mouseup: (params: any) => {
          //     console.log('mouseup', params, startCell, endCell);
          //     setStartCell('');
          //     setEndCell('');
          //   },
          //   datarangeselected: debounce((params: any) => {
          //     if (params.visualMapId === 'success') {
          //       setSuccessRange(params.selected);
          //     } else if (params.visualMapId === 'fail') {
          //       setFailRange(params.selected);
          //     }
          //   }, 300),
          //   // click: (params: any, echartsInstance: ECharts) => {
          //   //   console.log('click', params);
          //   //   setStartCell(`${params.value[0]}-${params.value[1]}`);
          //   //   // setRange([1000, 3000]);
          //   // },
          // }}
        />
      </div>
    );
  },
);

export default HeatmapChart;
