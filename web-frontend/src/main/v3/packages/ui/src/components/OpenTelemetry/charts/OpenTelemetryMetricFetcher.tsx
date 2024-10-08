import { format } from 'date-fns';
import { OtlpMetricDefUserDefined } from '@pinpoint-fe/constants';
import { usePostOtlpMetricData } from '@pinpoint-fe/hooks';
import { getFormat } from '@pinpoint-fe/utils';
import { COLORS } from './constant';
import { ChartDataConfig, OpenTelemetryChart } from './OpenTelemetryChart';
import { getRandomColorInHSL } from '../../../lib/colors';
import React from 'react';

export interface OpenTelemetryMetricFetcherProps {
  metricDefinition: OtlpMetricDefUserDefined.Metric & {
    showTotal?: boolean; // will be added in the form
  };
  dashboardId?: string;
}

export const OpenTelemetryMetricFetcher = ({
  metricDefinition,
  dashboardId,
}: OpenTelemetryMetricFetcherProps) => {
  const { mutate, data } = usePostOtlpMetricData();

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
    } = metricDefinition;
    mutate({
      applicationName,
      metricGroupName,
      metricName,
      tagGroupList,
      chartType,
      aggregationFunction,
      fieldNameList,
      primaryForFieldAndTagRelation,
      from: 0,
      to: 0,
    });
  }, [metricDefinition]);

  const { stack, showTotal = false } = metricDefinition;

  const dataSets =
    data?.metricValues.reduce(
      (acc, metricValue) => {
        return {
          ...acc,
          [metricValue.legendName]: metricValue.values,
        };
      },
      {} as { [key: string]: number[] },
    ) || {};

  const dataKeys = Object.keys(dataSets);
  const chartData =
    data?.timestamp.map((t, i) => {
      const dataObj: { [key: string]: number } = { timestamp: t };

      for (const [key, dataArray] of Object.entries(dataSets)) {
        dataObj[key] = dataArray[i];
      }

      return dataObj;
    }) ?? [];
  const chartConfig = dataKeys.reduce((acc, key, i) => {
    return {
      ...acc,
      [key]: {
        label: key,
        color: COLORS[i] ?? getRandomColorInHSL(),
        stack,
      },
    };
  }, {}) satisfies ChartDataConfig;

  return (
    data && (
      <OpenTelemetryChart
        chartType={data.chartType}
        dashboardId={dashboardId}
        chartData={chartData}
        chartDataConfig={chartConfig}
        xAxisConfig={{
          dataKey: 'timestamp',
          tickFormatter: (value) => `${format(value, 'HH:mm')}`,
        }}
        yAxisConfig={{
          tickFormatter: (value) => getFormat(data.unit)(value),
          label: {
            value: data.unit,
            position: 'insideLeft',
            angle: -90,
            style: { fontSize: '0.75rem' },
          },
        }}
        tooltipConfig={{
          showTotal: showTotal || stack,
        }}
      />
    )
  );
};
