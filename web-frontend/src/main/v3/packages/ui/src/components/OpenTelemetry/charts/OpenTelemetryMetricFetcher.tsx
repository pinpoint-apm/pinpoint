import { OtlpMetricDefUserDefined } from '@pinpoint-fe/constants';
import { useOpenTelemetrySearchParameters, usePostOtlpMetricData } from '@pinpoint-fe/hooks';
import React from 'react';
import { assign } from 'lodash';
import { ReChart } from '../../../components/ReChart';

export interface OpenTelemetryMetricFetcherProps {
  metricDefinition: OtlpMetricDefUserDefined.Metric;
  dashboardId?: string;
  inView?: boolean;
}

export const OpenTelemetryMetricFetcher = ({
  metricDefinition,
  dashboardId,
  inView,
}: OpenTelemetryMetricFetcherProps) => {
  const { mutate, data } = usePostOtlpMetricData();
  const { dateRange, agentId } = useOpenTelemetrySearchParameters();
  const prevDateRange = React.useRef<{ from: Date; to: Date }>();
  const prevAgentId = React.useRef<string>();

  React.useEffect(() => {
    if (!inView) {
      return;
    }

    // Even if inView changes, if dateRange and agentId are the same value, no new call is made.
    if (
      prevDateRange.current?.from === dateRange?.from &&
      prevDateRange.current?.to === dateRange?.to &&
      prevAgentId.current === agentId
    ) {
      return;
    }

    prevDateRange.current = dateRange;
    prevAgentId.current = agentId;

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
  }, [inView, dateRange, agentId, metricDefinition]);

  const { stack, stackDetails } = metricDefinition;

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
};
