import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useExperimentals, useGetConfiguration } from '@pinpoint-fe/ui/src/hooks';
import { useSetAtom } from 'jotai';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { APP_PATH, Configuration } from '@pinpoint-fe/ui/src/constants';
import { searchParametersAtom } from '@pinpoint-fe/ui/src/atoms';
import { getApplicationTypeAndName } from '@pinpoint-fe/ui/src/utils';

export const withInitialFetch =
  <P extends object>(WrappedComponent: React.ComponentType<P>) =>
  (props: P) => {
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

    if (error) {
      navigate(APP_PATH.API_CHECK);
      return null;
    }

    return <WrappedComponent {...props} configuration={data} />;
  };
