import { OtlpMetricDefUserDefined } from '@pinpoint-fe/ui/constants';
import { useOpenTelemetrySearchParameters, usePostOtlpMetricData } from '@pinpoint-fe/ui/hooks';
import React from 'react';
import { assign } from 'lodash';
import { ReChart } from '../../../components/ReChart';
import { useInView } from 'react-intersection-observer';

export interface OpenTelemetryMetricFetcherProps {
  metricDefinition: OtlpMetricDefUserDefined.Metric;
  dashboardId?: string;
  inView?: boolean;
}

export const OpenTelemetryMetricFetcher = ({
  metricDefinition,
  dashboardId,
}: OpenTelemetryMetricFetcherProps) => {
  const { mutate, data } = usePostOtlpMetricData();
  const { dateRange, agentId } = useOpenTelemetrySearchParameters();
  const prevDateRange = React.useRef<{ from: Date; to: Date }>();
  const prevAgentId = React.useRef<string>();
  const { ref, inView } = useInView({
    initialInView: false,
    threshold: 0.1,
  });

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
    <div ref={ref} className="w-full h-full">
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
    </div>
  );
};
