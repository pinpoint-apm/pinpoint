import { atom } from 'jotai';
import {
  FilteredMapType as FilteredMap,
  GetServerMap,
  AgentOverview,
} from '@pinpoint-fe/ui/src/constants';
import { Node, Edge } from '@pinpoint-fe/server-map';
import { isNodeOfApplication } from '@pinpoint-fe/ui/src/utils/helper/serverMap';
import { getCurrentRouterPath } from '@pinpoint-fe/ui/src/utils/helper/route';

export type CurrentTarget = {
  id?: string;
  type?: 'node' | 'edge';
  applicationName?: string;
  imgPath?: string;
  serviceType?: string;
  nodes?: Node[];
  source?: string;
  target?: string;
  edges?: Edge[];
  hint?: {
    [key: string]: FilteredMap.FilterTargetRpcList;
  };
};

type ServerMapNodeDataArray = (GetServerMap.NodeData | FilteredMap.NodeData)[];
type ServerMapNodeLinkArray = (GetServerMap.LinkData | FilteredMap.LinkData)[];

export const serverMapDataAtom = atom<GetServerMap.Response | FilteredMap.Response | undefined>(
  undefined,
);

/**
 * map이 한 번 가운데로 옮겨 보여줄 노드의 id. 옮기고 나면 map이 비운다.
 *
 * 선택(`serverMapCurrentTargetAtom`)과 따로 두는 이유: 사용자가 노드를 클릭할 때마다 화면이
 * 움직이면 안 된다. 기준 노드가 없는 map(비DEFAULT servicemap)에 링크로 들어온 application처럼
 * **페이지가 대신 고른 선택**만 그 노드를 보여주도록 옮긴다(`ServerMapPage`, 이슈 #11280).
 * 기준 노드가 있는 map은 기준 노드로 센터링하므로 이 값을 쓰지 않는다.
 */
export const serverMapFocusNodeIdAtom = atom<string | undefined>(undefined);

/**
 * 선택된 타겟과 **그것을 고른 경로**. 경로를 함께 담는 이유는 아래 `serverMapCurrentTargetAtom`
 * 주석 참고.
 */
type StampedCurrentTarget = {
  /** 선택이 이루어진 시점의 라우터 경로(basename 제외). */
  path: string;
  target: CurrentTarget | undefined;
};

const serverMapCurrentTargetStampedAtom = atom<StampedCurrentTarget | undefined>(undefined);

/**
 * 서버맵에서 선택된 타겟.
 *
 * **선택은 그것을 고른 경로에 묶인다.** 경로가 바뀌면(application 변경, 뒤로/앞으로 가기) 이전
 * 경로의 선택은 그 즉시 남의 것이 되므로, 쓸 때 지금 경로를 함께 도장 찍어 둔다. 읽는 쪽은
 * `useServerMapCurrentTarget`/`useServerMapCurrentTargetData`로 지금 경로의 선택만 본다.
 *
 * 페이지들이 경로 변경 effect에서 이 값을 비우지만 그것은 한 박자 늦다. 그 사이 우측 패널이
 * **(새 application, 이전 경로에서 고른 노드)** 라는 있지도 않은 짝으로 한 번 렌더되고, 조회
 * 훅들이 그 짝으로 파라미터를 갱신해 화면을 떠난 대상 조회가 한 번 더 나갔다. (이슈 #10587)
 *
 * 도장은 여기 한 곳에서 찍는다. 값을 넣는 곳이 여러 군데(map fetcher들, 페이지, 병합 노드 목록)
 * 라서 호출부마다 경로를 넘기게 하면 한 곳만 빠뜨려도 조용히 어긋난다.
 */
export const serverMapCurrentTargetAtom = atom(
  (get) => get(serverMapCurrentTargetStampedAtom)?.target,
  (_get, set, target: CurrentTarget | undefined) => {
    set(serverMapCurrentTargetStampedAtom, {
      path: getCurrentRouterPath(),
      target,
    });
  },
);

/** 선택이 이루어진 경로. `useServerMapCurrentTarget`이 지금 경로와 비교하는 데 쓴다. */
export const serverMapCurrentTargetPathAtom = atom(
  (get) => get(serverMapCurrentTargetStampedAtom)?.path,
);

export const serverMapCurrentTargetDataAtom = atom((get) => {
  const currentTarget = get(serverMapCurrentTargetAtom);
  const serverMapData = get(serverMapDataAtom);

  // service group 노드는 그래프상 단일 노드로 그리지만, 팝업에서 자식(subNodes)을 클릭하면
  // currentTarget이 자식 노드 key를 가리키므로 lookup 대상에 자식까지 포함한다.
  const getFlattenedNodes = () =>
    ((serverMapData?.applicationMapData?.nodeDataArray as ServerMapNodeDataArray) || []).flatMap(
      (node) => {
        const subNodes = (node as GetServerMap.NodeData).subNodes;
        return Array.isArray(subNodes) ? [node, ...subNodes] : [node];
      },
    );

  if (currentTarget?.serviceType === 'USER') {
    // USER 노드는 applicationName·serviceType이 모두 'USER'라 이름으로는 구별되지 않는다.
    // USER가 여럿이면(호출받는 WAS마다 하나씩 생긴다) 고른 노드의 key로 찾아야 한다.
    // 이름으로만 찾으면 늘 첫 USER로 고정돼, 다른 USER를 골라도 그 USER의 정보가 나온다.
    // key로 못 찾을 때만(id 없이 세운 target) 첫 USER로 폴백한다.
    const userNodes = getFlattenedNodes().filter(
      ({ serviceType }) => serviceType === currentTarget.serviceType,
    );
    const targetId = currentTarget.id;
    const selectedUserNode = targetId
      ? userNodes.find(
          (node) => node.key === targetId || (node as GetServerMap.NodeData).nodeKey === targetId,
        )
      : undefined;
    return selectedUserNode ?? userNodes[0];
  } else if (currentTarget?.type === 'node') {
    const fallbackKey = `${currentTarget?.applicationName}^${currentTarget?.serviceType}`;
    return getFlattenedNodes().find((node) => {
      const nodeKey = (node as GetServerMap.NodeData).nodeKey;
      return (
        node.key === currentTarget?.id ||
        node.key === fallbackKey ||
        nodeKey === currentTarget?.id ||
        nodeKey === fallbackKey ||
        // 기준 노드(base node)로 세운 target은 id가 없어 이름으로만 찾을 수 있다. key 형식이
        // map API마다 다르므로(servermap 2단, servicemap 3단) 형식에 무관하게 비교한다.
        // `nodeKey`(2단)의 serviceType은 `getName()` 형식이라 여기서 만든 desc 형식과
        // 어긋나는 타입이 있어(UNKNOWN_DB_EXECUTE_QUERY→UNKNOWN_DB) 그것만으로는 부족하다.
        isNodeOfApplication(node as GetServerMap.NodeData, currentTarget)
      );
    });
  } else if (currentTarget?.type === 'edge') {
    const allLinks =
      (serverMapData?.applicationMapData?.linkDataArray as ServerMapNodeLinkArray) || [];
    // service group 링크는 그래프상 단일 엣지로 그리지만, 팝업에서 자식(subLinks)을 클릭하면
    // currentTarget이 자식 링크 key를 가리키므로 lookup 대상에 자식까지 포함한다.
    const flattenedLinks = allLinks.flatMap((link) => {
      const subLinks = (link as GetServerMap.LinkData).subLinks;
      return Array.isArray(subLinks) ? [link, ...subLinks] : [link];
    });
    return flattenedLinks.find(
      (link) =>
        link.key === currentTarget?.id ||
        (link as GetServerMap.LinkData).linkKey === currentTarget?.id,
    );
  } else {
    return undefined;
  }
});

/**
 * 실시간 activeThreadCount(WebSocket)의 조회 대상.
 *
 * 핀(잠금)이 걸려 있으면 map에서 다른 노드를 골라도 이 대상은 그대로다. 컴포넌트 안에 두면
 * 다른 화면에 다녀오는 것만으로 사라져(브라우저 탭 전환도 포함된다 — `useTabFocus`가 패널을
 * 통째로 내린다), 돌아왔을 때 핀은 그대로 꽂혀 있는데 대상만 지금 고른 노드로 바뀐다.
 * 소켓이 다시 연결되면 떠나기 전에 보고 있던 application을 그대로 이어 보라고 아톰에 둔다.
 *
 * `path`는 이 대상을 고른 화면의 경로다. 경로에는 service와 기준 application이 실려 있으므로,
 * 경로가 다르면 다른 화면을 보고 있는 것이라 고정해 둔 대상도 무효다. (아톰은 전역이라
 * 화면 remount로 지워지지 않는다. 이 비교가 없으면 다른 application의 실시간 화면에 들어가도
 * 이전 application의 수치를 보여준다.)
 */
export type ActiveThreadTarget = {
  path: string;
  applicationName: string;
  serviceName?: string;
  serviceType?: string;
};

export const activeThreadTargetAtom = atom<ActiveThreadTarget | undefined>(undefined);

// server-list 선택시
export const currentServerAtom = atom<AgentOverview.Instance | undefined>(undefined);

export const currentServerAgentIdAtom = atom<string | undefined>((get) => {
  const currentServer = get(currentServerAtom);
  return currentServer?.agentId;
});

export const serverMapChartTypeAtom = atom<'scatter' | 'heatmap'>('heatmap');
