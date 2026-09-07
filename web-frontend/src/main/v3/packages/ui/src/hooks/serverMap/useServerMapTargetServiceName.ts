import { GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { useEnableServiceMap } from '../utility/useEnableServiceMap';
import { useServiceNameForLink } from '../utility/useServiceNameForLink';
import { useServerMapCurrentTargetData } from './useServerMapCurrentTarget';

/**
 * 우측 패널의 조회가 나갈 service.
 *
 * 기본값은 이 화면의 service(`useServiceNameForLink` = 경로의 serviceName ?? 전역 선택값)다.
 * map에서 고른 노드/링크가 다른 service에 속하면 그 service로 갈아탄다 — servicemap은 다른
 * service를 묶은 group 노드까지 함께 그리고, group을 펼쳐 그 안의 application을 고를 수 있다.
 * 그렇게 고른 대상은 이 화면의 service에 없는 application이라 화면의 service로 조회하면 빈
 * 데이터가 온다. (이슈 #10497)
 *
 * **`enableServiceMap` 하나로 갈린다.** 그 설정이 곧 "어느 map이 보이는가"이기 때문이다 —
 * 꺼져 있으면 servermap만, 켜져 있으면 servicemap만 보이고, 감춘 쪽 URL은 렌더 전에 남은 쪽으로
 * 옮겨진다(`getHiddenMapPageRedirect`). 그래서 이 훅까지 온 노드는 켜져 있으면 언제나 servicemap이
 * 그린 것이다.
 *
 * > 두 map이 함께 보이던 시절에는 경로가 servicemap인지 따로 봐야 했다. servermap 응답에도
 * > 노드마다 `serviceName`이 실려 오는데(`NodeView`가 `application.getService()`를 무조건 쓴다)
 * > 그 값은 *application이 저장된 service*(대개 `DEFAULT`)이지 조회할 service가 아니어서, 그대로
 * > 쓰면 첫 조회는 global service로, 기준 노드가 잡힌 뒤 두 번째 조회는 `DEFAULT`로 나갔다.
 * > 헤더도 캐시 키도 갈려 같은 화면을 두 번 조회하고 두 번째 결과는 엉뚱한 service의 것이 됐다.
 * > (이슈 #10587) 이제 그 상태 자체가 만들어지지 않으므로 경로를 따로 보지 않는다.
 *
 * 설정이 꺼져 있으면 undefined다. service 개념 자체가 없고(백엔드가 모든 요청을 기본 service로
 * 해석한다) 이 값이 그대로 요청 헤더가 되므로, 설정이 꺼진 저장소에 헤더가 새어 나가지 않도록
 * 여기 한 곳에서 막는다. (`useServiceNameForLink`도 같은 규칙이다.)
 *
 * 이 값은 우측 패널의 컴포넌트들에 `serviceName` prop으로 내려간다. 받은 컴포넌트는 조회 훅과
 * 다른 화면으로 넘기는 링크에 그대로 쓰고, 받지 않은 화면(filteredMap, inspector 등)은 기존대로
 * 화면의 service로 조회한다.
 *
 * 링크(엣지)는 자기 service가 없으므로 출발지 노드의 service를 쓴다. 링크 통계의 기준
 * application도 출발지 노드이므로(`ServerMapChartsBoardFetcher`) 같은 기준이다.
 */
export const useServerMapTargetServiceName = () => {
  const enableServiceMap = useEnableServiceMap();
  const currentTargetData = useServerMapCurrentTargetData();
  const screenServiceName = useServiceNameForLink();

  if (!enableServiceMap) {
    return undefined;
  }

  return (
    (currentTargetData as GetServerMap.NodeData)?.serviceName ??
    (currentTargetData as GetServerMap.LinkData)?.sourceInfo?.serviceName ??
    screenServiceName
  );
};
