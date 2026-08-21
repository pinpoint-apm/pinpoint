import React from 'react';
import { ScatterDataType } from '@pinpoint-fe/scatter-chart';
import {
  getScatterFullScreenPath,
  convertParamsToQueryString,
  getTransactionListQueryString,
  getTransactionListPath,
} from '@pinpoint-fe/ui/src/utils';
import {
  ApplicationType,
  SCATTER_DATA_TOTAL_KEY,
  SEARCH_PARAMETER_DATE_FORMAT,
  BASE_PATH,
} from '@pinpoint-fe/ui/src/constants';
import { formatInTimeZone } from 'date-fns-tz';
import {
  ScatterChartCore,
  ScatterChartCoreProps,
  ScatterChartHandle,
} from './core/ScatterChartCore';
import {
  useServerMapSearchParameters,
  useServiceNameForLink,
  useTimezone,
} from '@pinpoint-fe/ui/src/hooks';
import { useStoragedAxisY } from './core/useStoragedAxisY';

export interface ScatterChartStaticProps extends Pick<
  ScatterChartCoreProps,
  'toolbarOption' | 'onDragEnd'
> {
  application: ApplicationType;
  data?: ScatterDataType[];
  range: [number, number];
  selectedAgentId?: string;
  /**
   * 조회 대상이 소속된 service. 화면의 service와 다를 때만 넘긴다(map에서 다른 service의 노드를
   * 고른 경우). 넘기지 않으면 화면의 service로 조회한다.
   */
  serviceName?: string;
}

export const ScatterChartStatic = ({
  application,
  data = [],
  range,
  selectedAgentId,
  serviceName,
  ...props
}: ScatterChartStaticProps) => {
  const { searchParameters } = useServerMapSearchParameters();
  // 넘어온 값이 없으면 화면의 service로 조회한다(map 밖에서 쓰이는 경우).
  const screenServiceName = useServiceNameForLink();
  const requestServiceName = serviceName ?? screenServiceName;
  const [timezone] = useTimezone();
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
                from: formatInTimeZone(range[0], timezone, SEARCH_PARAMETER_DATE_FORMAT),
                to: formatInTimeZone(range[1], timezone, SEARCH_PARAMETER_DATE_FORMAT),
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
              requestServiceName,
            )}&${getTransactionListQueryString({
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
