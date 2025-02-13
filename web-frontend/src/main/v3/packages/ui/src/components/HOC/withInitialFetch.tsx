import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useExperimentals, useGetConfiguration } from '@pinpoint-fe/ui/src/hooks';
import { useSetAtom } from 'jotai';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { APP_PATH, Configuration } from '@pinpoint-fe/ui/src/constants';

export const withInitialFetch =
  <P extends object>(WrappedComponent: React.ComponentType<P>) =>
  (props: P) => {
    const navigate = useNavigate();
    const { data, error } = useGetConfiguration<Configuration>({ suspense: false });
    const setConfiguration = useSetAtom(configurationAtom);

    useExperimentals(data);

    React.useEffect(() => {
      setConfiguration(data);
    }, [data]);

    if (error) {
      navigate(APP_PATH.API_CHECK);
      return null;
    }

    return <WrappedComponent {...props} />;
  };
