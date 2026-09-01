import { Configuration, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';
import { pickEnableServiceMap } from '@pinpoint-fe/ui/src/utils';
import { useLocalStorage } from './useLocalStorage';
import { useUpdateEffect } from './useUpdateEffect';

export const useExperimentals = (initialValue?: Configuration) => {
  const [enableServerMapRealTime, setEnableServerMapRealTime] = useLocalStorage(
    EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVER_MAP_REAL_TIME,
    !!initialValue?.['experimental.enableServerMapRealTime.value'],
  );
  const [useStatisticsAgentState, setUseStatisticsAgentState] = useLocalStorage(
    EXPERIMENTAL_CONFIG_KEYS.USE_STATISTICS_AGENT_STATE,
    !!initialValue?.['experimental.useStatisticsAgentState.value'],
  );
  // 다른 experimental과 달리 초기값이 `null`이다. 이 값은 화면 밖(fetch 인터셉터)에서도
  // 읽히고 configuration이 도착하기 전에 이 훅이 먼저 마운트되므로, boolean을 기본값으로 주면
  // 사용자가 고른 적 없는 `false`가 localStorage에 박혀 서버 기본값을 영구히 덮어쓴다.
  // "저장된 값 없음"을 null로 남겨 두고, 실제 판단은 읽는 시점에 `pickEnableServiceMap`이 한다.
  const [enableServiceMap, setEnableServiceMap] = useLocalStorage<boolean | null>(
    EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP,
    null,
  );

  useUpdateEffect(() => {
    if (enableServerMapRealTime == null) {
      setEnableServerMapRealTime(
        initialValue?.['experimental.enableServerMapRealTime.value'] || false,
      );
    }
    if (useStatisticsAgentState == null) {
      setUseStatisticsAgentState(
        initialValue?.['experimental.useStatisticsAgentState.value'] || false,
      );
    }
    // enableServiceMap은 여기서 seed 하지 않는다. 위 주석 참고.
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
    [EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP]: {
      description: initialValue?.['experimental.enableServiceMap.description'],
      // 체크박스에 보이는 값도 화면·헤더와 같은 규칙에서 나와야 한다.
      value: pickEnableServiceMap(enableServiceMap, initialValue),
      setter: setEnableServiceMap,
    },
  };

  return experimentalMap;
};
