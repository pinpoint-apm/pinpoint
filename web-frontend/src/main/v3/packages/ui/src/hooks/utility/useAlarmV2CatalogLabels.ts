import React from 'react';
import { useAlarmV2DataSourcesQuery } from '@pinpoint-fe/ui/src/hooks/api';

/**
 * Human labels for the ids the alarm API speaks in, for both data sources and metrics.
 * The forms already show these labels through the catalog; lists use this so the same
 * value never reads differently on two screens. Falls back to the id while the catalog
 * is loading, or for an id it does not know.
 */
export const useAlarmV2CatalogLabels = () => {
  const { data: dataSources } = useAlarmV2DataSourcesQuery();
  return React.useMemo(() => {
    const dataSourceLabels = new Map<string, string>();
    const metricLabels = new Map<string, string>();
    dataSources?.forEach((dataSource) => {
      dataSourceLabels.set(dataSource.value, dataSource.label);
      dataSource.metrics?.forEach((metric) => metricLabels.set(metric.value, metric.label));
    });
    return {
      dataSourceLabel: (value?: string) => (value ? (dataSourceLabels.get(value) ?? value) : ''),
      metricLabel: (value: string) => metricLabels.get(value) ?? value,
    };
  }, [dataSources]);
};
