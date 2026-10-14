// import '@pinpoint-fe/scatter-chart/src/style/index.css';

import React from 'react';
import { ErrorBoundary as ReactErrorBoundary } from 'react-error-boundary';
import { lazyWithPreload } from '@pinpoint-fe/ui/src/utils/lazyWithPreload';
import {
  ScatterChart as SC,
  ScatterDataType,
  GuideEventCallback,
  ScatterChartOption,
} from '@pinpoint-fe/scatter-chart';
import { getDefaultOption } from './defaultOption';
import { BsGearFill } from 'react-icons/bs';
import { FaDownload, FaExpandArrowsAlt } from 'react-icons/fa';
import { CgSpinner } from 'react-icons/cg';
import { useReactToastifyToast, HelpPopover } from '../..';
import { cn } from '../../../lib';
import type { ScatterSettingProps } from './ScatterSetting';

// 설정 팝업은 눌러야 열리므로 react-hook-form 과 함께 필요할 때 불러온다.
// 배포로 청크 이름이 바뀐 뒤 예전 탭에서 열면 불러오기가 실패하므로, 렌더하는 곳에서 ErrorBoundary 로
// 팝업만 닫는다(페이지 전체가 에러 화면이 되지 않도록).
const { Component: ScatterSetting, preload: preloadScatterSetting } = lazyWithPreload(() =>
  import('./ScatterSetting').then((module) => module.ScatterSetting),
);
// 톱니바퀴에 마우스를 올리거나 focus 하면 미리 받아 두어, 처음 열 때 팝업이 비는 순간을 없앤다.
// 미리 받기의 실패는 여기서 무시한다 — 실제로 열 때 다시 불러오고, 그때의 실패는 ErrorBoundary 가 처리한다.
const prefetchScatterSetting = () => {
  preloadScatterSetting().catch(() => {});
};

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

export const ScatterChartCore = ({
  x,
  y,
  className = '',
  resizable,
  getOption = getDefaultOption,
  onDragEnd,
  onResize,
  toolbarOption,
  ref,
}: ScatterChartCoreProps & { ref?: React.Ref<ScatterChartHandle> }) => {
  const toast = useReactToastifyToast();
  const wrapperRef = React.useRef<HTMLDivElement>(null);
  const scatterRef = React.useRef<InstanceType<typeof SC> | undefined>(undefined);
  const resizeObserverRef = React.useRef<ResizeObserver | null>(null);
  const resizeTimeout = React.useRef<NodeJS.Timeout | undefined>(undefined);
  const [isCapturingImage, setIsCapturingImage] = React.useState(false);
  const [isSettingAxis, setIsSettingAxis] = React.useState(false);
  const [checkedLegends, setCheckedLegends] = React.useState(['success', 'fail']);

  React.useImperativeHandle(ref, () => {
    return {
      isMounted: () => {
        return !!scatterRef?.current;
      },
      getChartSize: () => {
        return getChartSize();
      },
      startRealtime: (duration: number) => {
        scatterRef?.current?.startRealtime(duration);
      },
      stopRealtime: () => {
        scatterRef?.current?.stopRealtime();
      },
      isRealtime: () => {
        return !!scatterRef?.current?.isRealtime;
      },
      render: (data: ScatterDataType[]) => {
        scatterRef?.current?.render(data, { append: true });
      },
      clear: () => {
        scatterRef?.current?.clear();
      },
      setAxisOption: (option: Partial<ScatterChartOption['axis']>) => {
        scatterRef?.current?.setOption({
          axis: option,
        });
      },
    };
  }, [scatterRef.current]);

  React.useEffect(() => {
    return () => {
      const sc = scatterRef?.current;
      sc?.stopRealtime();
      sc?.destroy();
      scatterRef.current = undefined;
      if (resizable) {
        resizeObserverRef?.current?.disconnect();
      }
    };
  }, []);

  React.useEffect(() => {
    const wrapperElement = wrapperRef?.current;
    const sc = scatterRef?.current;
    if (!sc && wrapperElement) {
      scatterRef.current = new SC(wrapperElement as HTMLElement, getOption({ x, y }));

      addEventListeners();
      setResizeHandler();
    }
  }, [wrapperRef?.current, scatterRef?.current]);

  React.useEffect(() => {
    addEventListeners();
  }, [onResize, onDragEnd, checkedLegends]);

  const setResizeHandler = () => {
    const wrapperElement = wrapperRef?.current;
    const sc = scatterRef?.current;
    if (!wrapperElement || !sc) return;
    if (resizable) {
      // add resizeObserver
      if (resizeObserverRef?.current) resizeObserverRef?.current?.disconnect();
      resizeObserverRef.current = new ResizeObserver(() => {
        // You can iterate all of the element entries observed
        clearTimeout(resizeTimeout.current);
        resizeTimeout.current = setTimeout(() => {
          const wrapperWidth = wrapperElement?.clientWidth;
          const wrapperHeight = wrapperElement?.clientHeight;

          if (wrapperWidth && wrapperHeight) {
            if (!sc?.isRealtime) {
              sc?.resize(wrapperWidth, wrapperHeight);
            }
          }
        }, 200);
      });
      resizeObserverRef?.current?.observe(wrapperElement);
    }
  };

  React.useEffect(() => {
    if (!scatterRef?.current?.isRealtime) {
      scatterRef?.current?.clear();
      scatterRef?.current?.setOption({
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
    if (!scatterRef?.current?.isRealtime) {
      scatterRef?.current?.clear();
      scatterRef?.current?.setOption({
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
      const prevDatas = Object.keys(scatterRef?.current?.datas || {}).reduce((acc, key) => {
        return [
          ...acc,
          // eslint-disable-next-line
          // @ts-ignore
          ...(scatterRef.current?.datas[key].map((d) => ({ ...d, type: key })) || []),
        ];
      }, [] as ScatterDataType[]);
      scatterRef?.current?.clear();
      // eslint-disable-next-line
      // @ts-ignore
      scatterRef?.current?.yAxis?.setOption({
        min: y?.[0],
        max: y?.[1],
      });
      // eslint-disable-next-line
      // @ts-ignore
      scatterRef?.current?.setPadding();
      // eslint-disable-next-line
      // @ts-ignore
      scatterRef?.current?.setYRatio();
      scatterRef?.current?.render(prevDatas);
    }
  }, [y?.[0], y?.[1]]);

  const addEventListeners = () => {
    const sc = scatterRef?.current;
    if (sc) {
      sc?.off('dragEnd');
      sc?.off('resize');
      sc?.on('dragEnd', (_, data) => {
        onDragEnd?.(
          { ...data, y1: data.y1 > y[1] ? Number.MAX_SAFE_INTEGER : data.y1 },
          checkedLegends,
        );
      });
      sc?.on('clickLegend', (_, data) => {
        setCheckedLegends(data.checked);
      });
      sc?.on('resize', () => {
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
    const sc = scatterRef?.current;
    const wrapperElement = wrapperRef?.current;
    let chartSize = { width: 0, height: 0 };
    if (sc && wrapperElement) {
      const option = sc?.getOption();
      const { padding, axis } = option;
      const chartWidth =
        wrapperElement?.clientWidth - padding.left - padding.right - axis.x.padding! * 2;
      const chartHeight =
        wrapperElement?.clientHeight - padding.bottom - padding.top - axis.y.padding! * 2;
      chartSize = { width: chartWidth, height: chartHeight };
    }
    return chartSize;
  };

  const handleClickAxisSetting = () => {
    setIsSettingAxis(true);
  };

  const handleApplySetting: ScatterSettingProps['onApply'] = ({ yMin, yMax }) => {
    let min = 0;
    if (yMin > 0) {
      min = yMin;
    }
    const max = yMax <= min ? min + 2 : yMax;
    toolbarOption?.axisSetting?.onApply?.({ yMin: min, yMax: max });
  };

  const handleClickCaptureImage = React.useCallback(async () => {
    const sc = scatterRef?.current;
    const fileName = toolbarOption?.captureImage?.fileName;

    if (sc) {
      setIsCapturingImage(true);
      try {
        const image = await sc?.toBase64Image();
        const downloadElement = document.createElement('a');
        downloadElement?.setAttribute('href', image);
        downloadElement?.setAttribute('download', `${fileName}.png`);
        wrapperRef?.current?.appendChild(downloadElement);
        downloadElement?.click();
        setIsCapturingImage(false);
        wrapperRef?.current?.removeChild(downloadElement);
      } catch {
        setIsCapturingImage(false);
        toast.error('An error occurred while capturing the image. Please try again.');
      }
    }
  }, [toolbarOption?.captureImage?.fileName]);

  const handleClickExpand = () => {
    toolbarOption?.expand?.onClick?.();
  };

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
              onMouseEnter={prefetchScatterSetting}
              onFocus={prefetchScatterSetting}
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
            <HelpPopover helpKey="HELP_VIEWER.SCATTER" />
          </div>
          {/* overlay */}
          <div
            className={cn(
              'w-full h-[calc(100%+24px)] absolute -top-6 z-chart-overlay pointer-events-[stroke] hidden justify-center items-center bg-background opacity-80',
              {
                flex: isCapturingImage || isSettingAxis,
              },
            )}
          />
          <div
            className={cn(
              'hidden justify-center items-center w-full h-full absolute top-0 z-chart-overlay',
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
              <ReactErrorBoundary fallback={null} onError={() => setIsSettingAxis(false)}>
                <React.Suspense fallback={null}>
                  <ScatterSetting
                    defaultValues={{ yMin: y[0], yMax: y[1] }}
                    onClose={() => setIsSettingAxis(false)}
                    onApply={handleApplySetting}
                  />
                </React.Suspense>
              </ReactErrorBoundary>
            )}
          </div>
        </>
      )}
      {/* ScatterChart */}
      <div
        className={cn('relative h-full', {
          'cursor-crosshair': !!onDragEnd,
        })}
        ref={wrapperRef}
      ></div>
    </div>
  );
};
