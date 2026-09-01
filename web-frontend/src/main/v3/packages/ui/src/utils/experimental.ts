import { Configuration, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';
import { getLocalStorageValue } from './localStorage';

/**
 * `experimental.enableServiceMap`의 값을 정하는 단 하나의 규칙.
 *
 * 사용자가 Experimental 설정에서 값을 정했으면 그 값(localStorage)을 쓰고, 정하지 않았으면
 * configuration API가 내려준 값을 기본값으로 쓴다. **저장된 값이 없는 상태를 boolean으로
 * 눌러 담지 않는다** — 아직 고르지 않은 값을 configuration으로 seed 해 두면 서버 설정이
 * 바뀌어도 먼저 접속했던 브라우저는 옛 기본값에 고정된다. 그래서 판단은 읽는 시점에 한다.
 *
 * 요청 헤더(`serviceNameFetchInterceptor`)와 화면(`useEnableServiceMap`)이 같은 값을 봐야
 * 하므로 두 경로 모두 이 함수를 지난다. 한쪽만 localStorage를 보면 화면에는 service 개념이
 * 없는데 헤더는 실려 나가는(또는 그 반대의) 어긋남이 생긴다.
 */
export const pickEnableServiceMap = (stored: unknown, configuration?: Configuration) =>
  typeof stored === 'boolean' ? stored : !!configuration?.['experimental.enableServiceMap.value'];

/**
 * 렌더 밖(fetch 인터셉터 등)에서 읽는 경로. localStorage를 그 자리에서 읽으므로 설정을 바꾼
 * 직후의 요청부터 바로 반영된다.
 */
export const getEnableServiceMap = (configuration?: Configuration) =>
  pickEnableServiceMap(
    getLocalStorageValue(EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP),
    configuration,
  );
