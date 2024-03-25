import { Configuration, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/constants';
import { useLocalStorage } from './useLocalStorage';
import { useUpdateEffect } from 'usehooks-ts';

export const useExperimentals = (initialValue?: Configuration) => {
  const [enableServerMapRealTime, setEnableServerMapRealTime] = useLocalStorage(
    EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVER_MAP_REAL_TIME,
    initialValue?.['experimental.enableServerMapRealTime.value'],
  );
  const [useStatisticsAgentState, setUseStatisticsAgentState] = useLocalStorage(
    EXPERIMENTAL_CONFIG_KEYS.USE_STATISTICS_AGENT_STATE,
    initialValue?.['experimental.useStatisticsAgentState.value'],
  );

  useUpdateEffect(() => {
    enableServerMapRealTime ??
      setEnableServerMapRealTime(initialValue?.['experimental.enableServerMapRealTime.value']);
    useStatisticsAgentState ??
      setUseStatisticsAgentState(initialValue?.['experimental.useStatisticsAgentState.value']);
  }, [initialValue]);

  const experimentalMap = {
    [EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVER_MAP_REAL_TIME]: {
      description: initialValue?.['experimental.enableServerMapRealTime.description'],
      value: enableServerMapRealTime,
      setter: setEnableServerMapRealTime,
    },
    [EXPERIMENTAL_CONFIG_KEYS.USE_STATISTICS_AGENT_STATE]: {
      description: initialValue?.['experimental.useStatisticsAgentState.description'],
      value: useStatisticsAgentState,
      setter: setUseStatisticsAgentState,
    },
  };

  return experimentalMap;
};
