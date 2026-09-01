import { EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';
import { pickEnableServiceMap } from '@pinpoint-fe/ui/src/utils';
import { useConfiguration } from './useConfiguration';
import { useLocalStorage } from './useLocalStorage';

/**
 * service / servicemap 기능이 켜져 있는지. 화면에서 이 값을 읽는 유일한 경로다.
 *
 * configuration API의 값을 기본값으로 쓰되, 사용자가 Experimental 설정에서 값을 정했으면
 * localStorage에 저장된 그 값이 이긴다(`pickEnableServiceMap`).
 *
 * `useLocalStorage`는 같은 key를 쓰는 다른 인스턴스의 쓰기를 `local-storage` 이벤트로 받으므로,
 * Experimental 설정에서 체크박스를 누르면 이 훅을 쓰는 화면들이 새로고침 없이 함께 반응한다.
 *
 * 초기값을 `null`로 두는 것이 중요하다. 여기서 boolean을 기본값으로 주면 configuration이
 * 도착하기 전에 그 값이 localStorage에 기록되어, 사용자가 고른 적 없는 값이 서버 기본값을
 * 영구히 덮어쓴다.
 */
export const useEnableServiceMap = () => {
  const configuration = useConfiguration();
  const [storedEnableServiceMap] = useLocalStorage<boolean | null>(
    EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP,
    null,
  );

  return pickEnableServiceMap(storedEnableServiceMap, configuration);
};
