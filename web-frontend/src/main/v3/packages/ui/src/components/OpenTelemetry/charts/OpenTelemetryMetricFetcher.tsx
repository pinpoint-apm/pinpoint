// import { format, isThisYear, isToday } from 'date-fns';
import { OtlpMetricDefUserDefined } from '@pinpoint-fe/constants';
import { useOpenTelemetrySearchParameters, usePostOtlpMetricData } from '@pinpoint-fe/hooks';
// import { getFormat } from '@pinpoint-fe/utils';
// import { COLORS } from './constant';
// import { ChartDataConfig, OpenTelemetryChart } from './OpenTelemetryChart';
// import { getRandomColorInHSL } from '../../../lib/colors';
import React from 'react';
// import { OpenTelemetryTick } from './OpenTelemetryTick';
import { assign } from 'lodash';
import { ReChart } from '../../../components/ReChart';

export interface OpenTelemetryMetricFetcherProps {
  metricDefinition: OtlpMetricDefUserDefined.Metric;
  dashboardId?: string;
}

export const OpenTelemetryMetricFetcher = ({
  metricDefinition,
  dashboardId,
}: OpenTelemetryMetricFetcherProps) => {
  const { mutate, data } = usePostOtlpMetricData();
  const { dateRange, agentId } = useOpenTelemetrySearchParameters();

  React.useEffect(() => {
    const {
      applicationName,
      metricGroupName,
      metricName,
      tagGroupList,
      chartType,
      aggregationFunction,
      fieldNameList,
      primaryForFieldAndTagRelation,
      samplingInterval,
    } = metricDefinition;
    mutate(
      assign(
        {
          applicationName,
          metricGroupName,
          metricName,
          tagGroupList,
          chartType,
          aggregationFunction,
          fieldNameList,
          primaryForFieldAndTagRelation,
          samplingInterval,
          from: dateRange?.from.getTime(),
          to: dateRange?.to.getTime(),
        },
        agentId ? { agentId } : {},
      ),
    );
  }, [dateRange, agentId, metricDefinition]);

  const { stack, stackDetails } = metricDefinition;

  // const dataSets =
  //   data?.metricValues.reduce(
  //     (acc, metricValue) => {
  //       return {
  //         ...acc,
  //         [metricValue.legendName]: metricValue.values,
  //       };
  //     },
  //     {} as { [key: string]: number[] },
  //   ) || {};

  // const dataKeys = Object.keys(dataSets);
  // const chartData =
  //   data?.timestamp.map((t, i) => {
  //     const dataObj: { [key: string]: number } = { timestamp: t };

  //     for (const [key, dataArray] of Object.entries(dataSets)) {
  //       dataObj[key] = (dataArray[i] === -1 ? null : dataArray[i]) as number;
  //     }

  //     return dataObj;
  //   }) ?? [];
  // const chartConfig = dataKeys.reduce((acc, key, i) => {
  //   return {
  //     ...acc,
  //     [key]: {
  //       label: key,
  //       color: COLORS[i] ?? getRandomColorInHSL(),
  //       stack,
  //     },
  //   };
  // }, {}) satisfies ChartDataConfig;

  return (
    <ReChart
      syncId={dashboardId}
      chartData={{
        title: '',
        timestamp: data?.timestamp || [],
        metricValueGroups: [
          {
            groupName: '',
            chartType: data?.chartType || '',
            unit: data?.unit || '',
            metricValues: (data?.metricValues || [])?.map((mv) => {
              return {
                fieldName: mv?.legendName,
                values: mv?.values,
              };
            }),
          },
        ],
      }}
      unit={data?.unit}
      tooltipConfig={{
        showTotal: stack && stackDetails?.showTotal,
      }}
    />
  );

  // return (
  //   data && (
  //     <OpenTelemetryChart
  //       chartType={data.chartType}
  //       dashboardId={dashboardId}
  //       chartData={chartData}
  //       chartDataConfig={chartConfig}
  //       xAxisConfig={{
  //         dataKey: 'timestamp',
  //         tick: OpenTelemetryTick,
  //         tickFormatter: (value) => {
  //           if (isToday(value)) {
  //             return format(value, 'HH:mm:ss');
  //           }
  //           if (isThisYear(value)) {
  //             return `${format(value, 'MM.dd')}\n${format(value, 'HH:mm')}`;
  //           }
  //           return `${format(value, 'yyyy.MM.dd')}\n${format(value, 'HH:mm')}`;
  //         },
  //       }}
  //       yAxisConfig={{
  //         tickFormatter: (value) => getFormat(data.unit)(value),
  //         label: {
  //           value: data.unit,
  //           position: 'insideLeft',
  //           angle: -90,
  //           style: { fontSize: '0.75rem' },
  //         },
  //       }}
  //       tooltipConfig={{
  //         showTotal: stack && stackDetails?.showTotal,
  //       }}
  //     />
  //   )
  // );
};
