import React from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useExperimentals, useGetConfiguration } from '@pinpoint-fe/ui/src/hooks';
import { useSetAtom } from 'jotai';
import { configurationAtom, searchParametersAtom } from '@pinpoint-fe/ui/src/atoms';
import { APP_PATH, Configuration } from '@pinpoint-fe/ui/src/constants';
import { getApplicationTypeAndName } from '@pinpoint-fe/ui/src/utils';

export const InitialFetchOutlet = () => {
  const navigate = useNavigate();
  const { data, error } = useGetConfiguration<Configuration>();
  const setConfiguration = useSetAtom(configurationAtom);
  const { pathname, search } = useLocation();
  const application = getApplicationTypeAndName(pathname);
  const searchParameters = Object.fromEntries(new URLSearchParams(search));
  const setSearchParameters = useSetAtom(searchParametersAtom);

  useExperimentals(data);

  React.useEffect(() => {
    if (application && searchParameters) {
      setSearchParameters({ application, searchParameters });
    }
  }, [
    application?.applicationName,
    application?.serviceType,
    searchParameters?.to,
    searchParameters?.from,
  ]);

  React.useEffect(() => {
    setConfiguration(data);
  }, [data]);

  React.useEffect(() => {
    if (error) {
      navigate(APP_PATH.API_CHECK);
    }
  }, [error, navigate]);

  if (error) {
    return null;
  }

  return <Outlet />;
};
