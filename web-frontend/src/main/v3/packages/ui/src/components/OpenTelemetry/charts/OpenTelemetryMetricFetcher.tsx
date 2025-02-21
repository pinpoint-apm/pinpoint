import { useTranslation } from 'react-i18next';
import {
  colors,
  ErrorDetailResponse,
  OtlpMetricDefUserDefined,
} from '@pinpoint-fe/ui/src/constants';
import { useOpenTelemetrySearchParameters, usePostOtlpMetricData } from '@pinpoint-fe/ui/src/hooks';
import React from 'react';
import { assign } from 'lodash';
import { ReChart } from '../../../components/ReChart';
import { useInView } from 'react-intersection-observer';
import { Button, Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '../../ui';
import { ErrorDetailDialog } from '../../Error/ErrorDetailDialog';
import { Widget } from '../../Dashboard/Widget';
import { HiMiniExclamationCircle } from 'react-icons/hi2';
import * as TooltipPrimitive from '@radix-ui/react-tooltip';

export interface OpenTelemetryMetricFetcherProps {
  metricDefinition: OtlpMetricDefUserDefined.Metric;
  dashboardId?: string;
  onDeleted?: (metric: OtlpMetricDefUserDefined.Metric) => void;
  onEdit?: (metric: OtlpMetricDefUserDefined.Metric) => void;
}

export const OpenTelemetryMetricFetcher = ({
  metricDefinition,
  dashboardId,
  onDeleted,
  onEdit,
}: OpenTelemetryMetricFetcherProps) => {
  const { t } = useTranslation();
  const { mutate, data, error } = usePostOtlpMetricData();
  const { dateRange, agentId } = useOpenTelemetrySearchParameters();
  const prevDateRange = React.useRef<{ from: Date; to: Date }>();
  const prevAgentId = React.useRef<string>();
  const { ref, inView } = useInView({
    initialInView: false,
    threshold: 0.1,
  });

  function getMetricData() {
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
  }

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

    getMetricData();
  }, [inView, dateRange, agentId, metricDefinition]);

  const { stack, stackDetails } = metricDefinition;

  if (error) {
    const errorObj = JSON.parse(error.message);
    return (
      <div className="flex flex-col items-center justify-center w-full h-full">
        {errorObj?.message}
        <ErrorDetailDialog error={errorObj as unknown as ErrorDetailResponse} />
        <Button className="text-xs" variant="outline" onClick={() => getMetricData()}>
          {t('COMMON.TRY_AGAIN')}
        </Button>
      </div>
    );
  }

  return (
    <Widget
      title={
        <div className="flex flex-row gap-1">
          {metricDefinition.title}
          {data?.message && (
            <TooltipProvider delayDuration={0}>
              <Tooltip>
                <TooltipTrigger asChild>
                  <div>
                    <HiMiniExclamationCircle color={colors.gray[400]} size={16} />
                  </div>
                </TooltipTrigger>
                <TooltipPrimitive.Portal>
                  <TooltipContent>
                    {data?.message?.split('\n').map((m, i) => <p key={i}>{m}</p>)}
                  </TooltipContent>
                </TooltipPrimitive.Portal>
              </Tooltip>
            </TooltipProvider>
          )}
        </div>
      }
      onClickDelete={() => {
        onDeleted?.(metricDefinition);
      }}
      onClickEdit={() => {
        onEdit?.(metricDefinition);
      }}
    >
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
    </Widget>
  );
};
