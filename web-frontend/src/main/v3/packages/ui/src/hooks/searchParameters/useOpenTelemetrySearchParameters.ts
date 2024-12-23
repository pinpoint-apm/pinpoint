import React from 'react';
import { useLocation } from 'react-router-dom';
import { getApplicationTypeAndName } from '@pinpoint-fe/ui/utils';
import { getDateRange, getSearchParameters } from './utils';

export const useOpenTelemetrySearchParameters = () => {
  const { search, pathname } = useLocation();
  const application = React.useMemo(() => getApplicationTypeAndName(pathname), [pathname]);
  const searchParameters = React.useMemo(() => getSearchParameters(search), [search]);
  const dateRange = React.useMemo(() => getDateRange(search, false), [search]);
  const agentId = searchParameters?.agentId;

  return { application, dateRange, agentId, searchParameters };
};
