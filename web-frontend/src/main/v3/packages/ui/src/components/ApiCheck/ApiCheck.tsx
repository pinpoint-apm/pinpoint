import { ApiStatus } from '.';
import { useGetServerTime, useGetConfiguration } from '@pinpoint-fe/ui/hooks';
import { END_POINTS } from '@pinpoint-fe/ui/constants';

export interface ApiCheckProps {
  children?: React.ReactNode;
}

export const ApiCheck = ({ children }: ApiCheckProps) => {
  const {
    data: serverTimeData,
    isLoading: isServerTimeLoading,
    error: serverTimeError,
  } = useGetServerTime({ suspense: false });
  const {
    data: configurationData,
    isLoading: isConfigurationLoading,
    error: configurationError,
  } = useGetConfiguration({ suspense: false });

  return (
    <div className="flex flex-col gap-4 items-center justify-center p-5 h-[calc(100%-4rem)]">
      <ApiStatus
        {...{
          data: serverTimeData,
          isLoading: isServerTimeLoading,
          error: serverTimeError,
          path: END_POINTS.SERVER_TIME,
        }}
      />
      <ApiStatus
        {...{
          data: configurationData,
          isLoading: isConfigurationLoading,
          error: configurationError,
          path: END_POINTS.CONFIGURATION,
        }}
      />
      {children}
    </div>
  );
};
