import '@pinpoint-fe/scatter-chart/dist/index.css';

import React from 'react';
import {
  ScatterChart as SC,
  ScatterDataType,
  GuideEventCallback,
  ScatterChartOption,
} from '@pinpoint-fe/scatter-chart';
import { getDefaultOption } from './defaultOption';
import { BsGearFill } from 'react-icons/bs';
import { FaDownload, FaExpandArrowsAlt, FaQuestionCircle } from 'react-icons/fa';
import { CgSpinner } from 'react-icons/cg';
import { toast } from '../..';
import { cn } from '../../../lib';
import { ScatterSetting, ScatterSettingProps } from './ScatterSetting';

export type ScatterToolbarOption = {
  axisSetting?: {
    onApply?: ScatterSettingProps['onApply'];
    hide?: boolean;
  };
  captureImage?: {
    onClick?: () => void;
    fileName?: string;
    hide?: boolean;
  };
  expand?: {
    onClick?: () => void;
    hide?: boolean;
  };
  help?: {
    onClick?: () => void;
    hide?: boolean;
  };
  hide?: boolean;
  className?: string;
};

export interface ScatterChartCoreProps {
  x: [number, number];
  y: [number, number];
  className?: string;
  resizable?: boolean;
  getOption?: ({ x, y }: { x: [number, number]; y: [number, number] }) => ScatterChartOption;
  onDragEnd?: (data: Parameters<GuideEventCallback<'dragEnd'>>[1], checkedLabels: string[]) => void;
  onResize?: ({
    width,
    height,
    option,
  }: {
    width: number;
    height: number;
    option: ScatterChartOption;
  }) => void;
  toolbarOption?: ScatterToolbarOption;
}

export interface ScatterChartHandle {
  isMounted: () => boolean;
  getChartSize: () => { width: number; height: number };
  startRealtime: (duration: number) => void;
  stopRealtime: () => void;
  isRealtime: () => boolean;
  render: (data: ScatterDataType[]) => void;
  clear: () => void;
  setAxisOption: (option: Partial<ScatterChartOption['axis']>) => void;
}

export const ScatterChartCore = React.forwardRef<ScatterChartHandle, ScatterChartCoreProps>(
  (
    {
      x,
      y,
      className = '',
      resizable,
      getOption = getDefaultOption,
      onDragEnd,
      onResize,
      toolbarOption,
    }: ScatterChartCoreProps,
    ref,
  ) => {
    const wrapperRef = React.useRef<HTMLDivElement>(null);
    const scatterRef = React.useRef<InstanceType<typeof SC>>();
    const resizeObserverRef = React.useRef<ResizeObserver>();
    const resizeTimeout = React.useRef<NodeJS.Timeout>();
    const [isCapturingImage, setIsCapturingImage] = React.useState(false);
    const [isSettingAxis, setIsSettingAxis] = React.useState(false);
    const [checkedLegends, setCheckedLegends] = React.useState(['success', 'fail']);

    React.useImperativeHandle(ref, () => {
      return {
        isMounted: () => {
          return !!scatterRef.current;
        },
        getChartSize: () => {
          return getChartSize();
        },
        startRealtime: (duration: number) => {
          scatterRef.current?.startRealtime(duration);
        },
        stopRealtime: () => {
          scatterRef.current?.stopRealtime();
        },
        isRealtime: () => {
          return !!scatterRef.current?.isRealtime;
        },
        render: (data: ScatterDataType[]) => {
          scatterRef.current?.render(data, { append: true });
        },
        clear: () => {
          scatterRef.current?.clear();
        },
        setAxisOption: (option: Partial<ScatterChartOption['axis']>) => {
          scatterRef.current?.setOption({
            axis: option,
          });
        },
      };
    }, [scatterRef.current]);

    React.useEffect(() => {
      return () => {
        const sc = scatterRef.current;
        sc?.stopRealtime();
        sc?.destroy();
        scatterRef.current = undefined;
        resizable && resizeObserverRef?.current?.disconnect();
      };
    }, []);

    React.useEffect(() => {
      const wrapperElement = wrapperRef.current;
      const sc = scatterRef.current;
      if (!sc && wrapperElement) {
        scatterRef.current = new SC(wrapperElement as HTMLElement, getOption({ x, y }));

        addEventListeners();
        setResizeHandler();
      }
    }, [wrapperRef.current, scatterRef.current]);

    React.useEffect(() => {
      addEventListeners();
    }, [onResize, onDragEnd, checkedLegends]);

    const setResizeHandler = () => {
      const wrapperElement = wrapperRef.current;
      const sc = scatterRef.current;
      if (!wrapperElement || !sc) return;
      if (resizable) {
        // add resizeObserver
        if (resizeObserverRef.current) resizeObserverRef?.current?.disconnect();
        resizeObserverRef.current = new ResizeObserver(() => {
          // You can iterate all of the element entries observed
          clearTimeout(resizeTimeout.current);
          resizeTimeout.current = setTimeout(() => {
            const wrapperWidth = wrapperElement.clientWidth;
            const wrapperHeight = wrapperElement.clientHeight;

            if (wrapperWidth && wrapperHeight) {
              !sc.isRealtime && sc.resize(wrapperWidth, wrapperHeight);
            }
          }, 200);
        });
        resizeObserverRef.current.observe(wrapperElement);
      }
    };

    React.useEffect(() => {
      if (!scatterRef.current?.isRealtime) {
        scatterRef.current?.clear();
        scatterRef.current?.setOption({
          axis: {
            x: {
              min: x?.[0],
              max: x?.[1],
            },
          },
        });
      }
    }, [x?.[0], x?.[1]]);

    React.useEffect(() => {
      if (!scatterRef.current?.isRealtime) {
        scatterRef.current?.clear();
        scatterRef.current?.setOption({
          axis: {
            y: {
              min: y?.[0],
              max: y?.[1],
            },
          },
        });
      } else {
        // TODO scatter chart개선 후 정리 필요
        // eslint-disable-next-line
        // @ts-ignore
        const prevDatas = Object.keys(scatterRef.current.datas).reduce((acc, key) => {
          return [
            ...acc,
            // eslint-disable-next-line
            // @ts-ignore
            ...(scatterRef.current?.datas[key].map((d) => ({ ...d, type: key })) || []),
          ];
        }, [] as ScatterDataType[]);
        scatterRef.current.clear();
        // eslint-disable-next-line
        // @ts-ignore
        scatterRef.current?.yAxis.setOption({
          min: y?.[0],
          max: y?.[1],
        });
        // eslint-disable-next-line
        // @ts-ignore
        scatterRef.current.setPadding();
        // eslint-disable-next-line
        // @ts-ignore
        scatterRef.current.setYRatio();
        scatterRef.current.render(prevDatas);
      }
    }, [y?.[0], y?.[1]]);

    const addEventListeners = () => {
      const sc = scatterRef.current;
      if (sc) {
        sc.off('dragEnd');
        sc.off('resize');
        sc.on('dragEnd', (_, data) => {
          onDragEnd?.(
            { ...data, y1: data.y1 > y[1] ? Number.MAX_SAFE_INTEGER : data.y1 },
            checkedLegends,
          );
        });
        sc.on('clickLegend', (_, data) => {
          setCheckedLegends(data.checked);
        });
        sc.on('resize', () => {
          if (onResize) {
            const option = sc?.getOption();

            onResize({
              ...getChartSize(),
              option,
            });
          }
        });
      }
    };

    const getChartSize = () => {
      const sc = scatterRef.current;
      const wrapperElement = wrapperRef.current;
      let chartSize = { width: 0, height: 0 };
      if (sc && wrapperElement) {
        const option = sc?.getOption();
        const { padding, axis } = option;
        const chartWidth =
          wrapperElement.clientWidth - padding.left - padding.right - axis.x.padding! * 2;
        const chartHeight =
          wrapperElement.clientHeight - padding.bottom - padding.top - axis.y.padding! * 2;
        chartSize = { width: chartWidth, height: chartHeight };
      }
      return chartSize;
    };

    const handleClickAxisSetting = () => {
      setIsSettingAxis(true);
    };

    const handleApplySetting: ScatterSettingProps['onApply'] = ({ yMin, yMax }) => {
      let min = 0;
      let max = 1;
      if (yMin > 0) {
        min = yMin;
      }
      if (yMax <= min) {
        max = min + 2;
      } else {
        max = yMax;
      }
      toolbarOption?.axisSetting?.onApply?.({ yMin: min, yMax: max });
    };

    const handleClickCaptureImage = React.useCallback(async () => {
      const sc = scatterRef.current;
      const fileName = toolbarOption?.captureImage?.fileName;

      if (sc) {
        setIsCapturingImage(true);
        try {
          const image = await sc.toBase64Image();
          const downloadElement = document.createElement('a');
          downloadElement.setAttribute('href', image);
          downloadElement.setAttribute('download', `${fileName}.png`);
          wrapperRef.current?.appendChild(downloadElement);
          downloadElement.click();
          setIsCapturingImage(false);
          wrapperRef.current?.removeChild(downloadElement);
        } catch {
          setIsCapturingImage(false);
          toast.error('An error occurred while capturing the image. Please try again.');
        }
      }
    }, [toolbarOption?.captureImage?.fileName]);

    const handleClickExpand = () => {
      toolbarOption?.expand?.onClick?.();
    };

    const handleClickHelp = () => {};

    return (
      <div className={cn('relative h-full', className)}>
        {toolbarOption?.hide === true ? null : (
          <>
            <div
              className={cn(
                'flex gap-2 absolute -top-7 right-3 text-gray-400',
                toolbarOption?.className,
              )}
            >
              <button
                className={cn({ hidden: toolbarOption?.axisSetting?.hide })}
                onClick={handleClickAxisSetting}
              >
                <BsGearFill />
              </button>
              <button
                className={cn({ hidden: toolbarOption?.captureImage?.hide })}
                onClick={handleClickCaptureImage}
              >
                <FaDownload />
              </button>
              <button
                className={cn({ hidden: toolbarOption?.expand?.hide })}
                onClick={handleClickExpand}
              >
                <FaExpandArrowsAlt />
              </button>
              <button
                className={cn({ hidden: toolbarOption?.help?.hide || true })}
                onClick={handleClickHelp}
              >
                <FaQuestionCircle />
              </button>
            </div>
            {/* overlay */}
            <div
              className={cn(
                'w-full h-[calc(100%+24px)] absolute -top-6 z-[1000] pointer-events-[stroke] hidden justify-center items-center bg-background opacity-80',
                {
                  flex: isCapturingImage || isSettingAxis,
                },
              )}
            />
            <div
              className={cn(
                'hidden justify-center items-center w-full h-full absolute top-0 z-[1000]',
                { flex: isCapturingImage || isSettingAxis },
              )}
            >
              {isCapturingImage && (
                <>
                  Capturing Image...
                  <CgSpinner className="animate-spin" />
                </>
              )}
              {isSettingAxis && (
                <>
                  <ScatterSetting
                    defaultValues={{ yMin: y[0], yMax: y[1] }}
                    onClose={() => setIsSettingAxis(false)}
                    onApply={handleApplySetting}
                  />
                </>
              )}
            </div>
          </>
        )}
        {/* ScatterChart */}
        <div className="relative h-full" ref={wrapperRef}></div>
      </div>
    );
  },
);

// const StyledContainer = styled.div`
//   position: relative;
//   height: 100%;

//   .__scatter_chart__legend_container {
//     margin-top: 16px;
//     padding-bottom: 4px;
//   }

//   .__scatter_chart__legend_count {
//     font-weight: bold;
//     font-size: 1.25rem;
//     margin-left: 12px;
//   }

//   .__scatter_chart__legend_mark {
//     align-self: center;
//   }
// `;

// const StyledToolbarButton = styled.button<{ hide?: boolean }>`
//   display: ${({ hide }) => (hide ? 'none' : 'block')};
//   color: var(--icon-default);
//   font-size: 1.2rem;
// `;

// const StyledOverlay = styled.div<{ show?: boolean }>`
//   width: 100%;
//   height: calc(100% + 24px);
//   position: absolute;
//   top: -24px;
//   z-index: 9999;
//   pointer-events: stroke;
//   display: ${({ show }) => (show ? 'flex' : 'none')};
//   justify-content: center;
//   align-items: center;
//   background-color: var(--background-default);
//   opacity: 0.8;
// `;

// const StyledOverlayContentContainer = styled.div<{ show?: boolean }>`
//   display: ${({ show }) => (show ? 'flex' : 'none')};
//   justify-content: center;
//   align-items: center;
//   width: 100%;
//   height: 100%;
//   position: absolute;
//   top: 0px;
//   z-index: 99999;
// `;
