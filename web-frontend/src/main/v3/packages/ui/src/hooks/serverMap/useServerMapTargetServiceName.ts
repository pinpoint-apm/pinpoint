import { useAtomValue } from 'jotai';
import { serverMapCurrentTargetDataAtom } from '@pinpoint-fe/ui/src/atoms';
import { GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { useEnableServiceMap } from '../utility/useEnableServiceMap';

/**
 * map에서 고른 노드/링크가 소속된 service. 고른 것이 없으면 undefined다.
 *
 * servicemap은 다른 service를 묶은 group 노드까지 함께 그리고, group을 펼쳐 그 안의
 * application을 고를 수 있다. 그렇게 고른 대상은 화면의 service와 다른 service에 속하므로,
 * 그 대상을 조회하는 요청은 화면의 service가 아니라 이 값으로 나가야 한다.
 *
 * 이 값은 우측 패널의 컴포넌트들에 `serviceName` prop으로 내려간다. 받은 컴포넌트는 조회 훅과
 * 다른 화면으로 넘기는 링크에 그대로 쓰고, 받지 않은 화면(filteredMap, inspector 등)은 기존대로
 * 화면의 service로 조회한다.
 *
 * 값은 백엔드가 노드마다 내려주는 `serviceName`(= `application.getService()`)이다.
 * 링크는 자기 service가 없으므로 출발지 노드의 service를 쓴다. 링크 통계의 기준 application도
 * 출발지 노드이므로(`ServerMapChartsBoardFetcher`) 같은 기준이다.
 *
 * `enableServiceMap`이 꺼져 있으면 service 개념 자체가 없으므로(백엔드가 모든 요청을 기본
 * service로 해석한다) 항상 undefined다. 이 값이 그대로 요청 헤더가 되므로, 설정이 꺼진 저장소에
 * 헤더가 새어 나가지 않도록 여기 한 곳에서 막는다.
 */
export const useServerMapTargetServiceName = () => {
  const enableServiceMap = useEnableServiceMap();
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom);

  if (!enableServiceMap || !currentTargetData) {
    return undefined;
  }

  return (
    (currentTargetData as GetServerMap.NodeData).serviceName ??
    (currentTargetData as GetServerMap.LinkData).sourceInfo?.serviceName
  );
};
