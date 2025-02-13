import React from 'react';
import { ScatterDataType } from '@pinpoint-fe/scatter-chart';
import {
  getScatterFullScreenPath,
  convertParamsToQueryString,
  getTranscationListQueryString,
  getTransactionListPath,
} from '@pinpoint-fe/ui/src/utils';
import {
  ApplicationType,
  SCATTER_DATA_TOTAL_KEY,
  SEARCH_PARAMETER_DATE_FORMAT,
  BASE_PATH,
} from '@pinpoint-fe/ui/src/constants';
import { format } from 'date-fns';
import {
  ScatterChartCore,
  ScatterChartCoreProps,
  ScatterChartHandle,
} from './core/ScatterChartCore';
import { useServerMapSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { useStoragedAxisY } from './core/useStoragedAxisY';

export interface ScatterChartStaticProps
  extends Pick<ScatterChartCoreProps, 'toolbarOption' | 'onDragEnd'> {
  application: ApplicationType;
  data?: ScatterDataType[];
  range: [number, number];
  selectedAgentId?: string;
}

export const ScatterChartStatic = ({
  application,
  data = [],
  range,
  selectedAgentId,
  ...props
}: ScatterChartStaticProps) => {
  const { searchParameters } = useServerMapSearchParameters();
  const scatterRef = React.useRef<ScatterChartHandle>(null);
  const [x, setX] = React.useState<[number, number]>([range[0], range[1]]);
  const [y, setY] = useStoragedAxisY();
  const isScatterMounted = scatterRef?.current?.isMounted();

  React.useEffect(() => {
    if (isScatterMounted) {
      scatterRef?.current?.clear();

      setX([range[0], range[1]]);
    }
  }, [isScatterMounted, range[0], range[1]]);

  React.useEffect(() => {
    if (selectedAgentId && isScatterMounted) {
      renderSelectedServerData();
    }
  }, [selectedAgentId, isScatterMounted, data.length]);

  const handleResize = React.useCallback<NonNullable<ScatterChartCoreProps['onResize']>>(() => {
    if (selectedAgentId) {
      renderSelectedServerData();
    }
  }, [selectedAgentId, data]);

  const renderSelectedServerData = () => {
    scatterRef?.current?.clear();
    scatterRef?.current?.render(data || []);
  };

  const handleApplyAxisSetting = ({ yMin, yMax }: { yMin: number; yMax: number }) => {
    setY([yMin, yMax]);
  };

  return (
    <ScatterChartCore
      x={x}
      y={y}
      ref={scatterRef}
      onResize={handleResize}
      resizable={true}
      toolbarOption={{
        captureImage: {
          fileName: `Pinpoint_Scatter_Chart__${selectedAgentId || ''}`,
        },
        axisSetting: {
          onApply: handleApplyAxisSetting,
        },
        help: {
          hide: true,
        },
        expand: {
          onClick: () => {
            window.open(
              `${BASE_PATH}${getScatterFullScreenPath(application)}?${convertParamsToQueryString({
                from: format(range[0], SEARCH_PARAMETER_DATE_FORMAT),
                to: format(range[1], SEARCH_PARAMETER_DATE_FORMAT),
                agentId: selectedAgentId === SCATTER_DATA_TOTAL_KEY ? undefined : selectedAgentId,
              })}`,
            );
          },
        },
        ...props.toolbarOption,
      }}
      onDragEnd={(data, checkedLegends) => {
        if (props?.onDragEnd) {
          props.onDragEnd(data, checkedLegends);
        } else {
          window.open(
            `${BASE_PATH}${getTransactionListPath(
              application,
              searchParameters,
            )}&${getTranscationListQueryString({
              ...data,
              checkedLegends,
              agentId: selectedAgentId === SCATTER_DATA_TOTAL_KEY ? '' : selectedAgentId,
            })}`,
          );
        }
      }}
    />
  );
};
