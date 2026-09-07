import { Configuration, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';
import { getConfiguration, getRequestService } from '@pinpoint-fe/ui/src/hooks';
import { getLocalStorageValue, pickEnableServiceMap } from '@pinpoint-fe/ui/src/utils';
import { getHiddenMapPageRedirect } from './hiddenMapPageRedirect';

/**
 * 라우트 로더용 — 사이드 메뉴에서 감춘 map 화면으로 **들어오는** 길을 막는다.
 * 어디로 옮기는지는 `getHiddenMapPageRedirect`가 정하고, 여기서는 설정을 읽어 넘긴다.
 *
 * **화면이 아니라 라우트 로더에서 막는다.** 화면에서 effect로 옮기면 한 박자 늦어, 감춘 쪽 화면이
 * 한 번 마운트되며 그 화면의 조회가 다 나간 뒤에 옮겨진다. 로더는 렌더 전에 돌기 때문에 감춘
 * 화면은 마운트되지 않는다.
 *
 * 이미 열려 있는 화면에서 설정이 바뀌는 경우(다른 탭에서 껐다 켰다)는 로더가 다시 돌지 않으므로
 * 여기로 걸리지 않는다. 그쪽은 `useHiddenMapPageRedirect`가 맡는다.
 */
export const resolveHiddenMapPageRedirect = async (
  requestUrl: string,
): Promise<string | undefined> => {
  const { pathname, search } = new URL(requestUrl);

  let configuration: Configuration | undefined;
  try {
    configuration = await getConfiguration<Configuration>();
  } catch {
    // 백엔드가 죽어 있으면 설정을 읽을 수 없다. 아래에서 "모르는 상태"로 다룬다.
  }

  // `getEnableServiceMap`과 같은 계산이지만, 그 함수는 "저장된 값이 없음"을 false로 접어
  // 돌려주기 때문에 아래 "모르는 상태" 분기를 만들 수 없다. 그래서 같은 규칙
  // (`pickEnableServiceMap`)을 쓰되 원본값을 직접 읽는다. **판정 규칙을 바꿀 때는
  // `utils/experimental.ts`의 두 함수와 이 파일을 함께 본다.**
  const storedEnableServiceMap = getLocalStorageValue(EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP);

  // 설정도 못 읽고 사용자가 고른 값도 없으면 **어느 쪽이 보이는 메뉴인지 알 수 없다.** 그때는
  // 화면을 옮기지 않는다. 모르는 채로 한쪽을 골라 옮기면, 백엔드가 잠깐 죽은 사이에 들어온
  // 사용자의 servicemap 링크가 servermap으로 바뀌어 버린다(설정이 돌아와도 URL은 그대로다).
  // 조회가 안 되는 상태 자체는 화면이 /apiCheck로 넘겨 알려준다.
  if (!configuration && typeof storedEnableServiceMap !== 'boolean') {
    return undefined;
  }

  return getHiddenMapPageRedirect({
    pathname,
    search,
    // 화면(`useEnableServiceMap`)과 같은 규칙(`pickEnableServiceMap`)으로 읽는다. 여기만 다른
    // 규칙을 쓰면 메뉴에는 servicemap이 보이는데 URL은 servermap으로 되돌려지는 식으로 어긋난다.
    enableServiceMap: pickEnableServiceMap(storedEnableServiceMap, configuration),
    // servermap 경로에는 serviceName이 실리지 않으므로 지금 보고 있는 service를 붙인다.
    serviceName: getRequestService(),
  });
};
