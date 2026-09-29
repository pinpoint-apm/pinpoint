import { useAtomValue } from 'jotai';
import { serverMapDataAtom } from '@pinpoint-fe/ui/src/atoms/serverMap';
import {
  BASE_PATH,
  FilteredMapType as FilteredMap,
  GetServerMap,
} from '@pinpoint-fe/ui/src/constants';
import {
  getFilteredMapQueryString,
  getFilteredMapPath,
  getFilterTargetApplication,
} from '@pinpoint-fe/ui/src/utils';
import { getDefaultFilters, SERVERMAP_MENU_FUNCTION_TYPE } from '@pinpoint-fe/ui/src/components';
import { Edge, Node } from '@pinpoint-fe/server-map';

/**
 * 응답의 노드·링크에 service group의 자식(`subNodes`/`subLinks`)까지 펼쳐 담는다.
 *
 * servicemap에서 group을 펼치면 자식 노드·링크가 그래프에 직접 그려지고 필터 메뉴도 열린다.
 * 그런데 응답에서 자식은 group 안에만 있어서, 최상위 배열만 찾으면 agent 목록과 sourceInfo를
 * 못 찾는다(그러면 WAS→WAS 링크도 출발지가 아닌 도착지를 기준으로 열린다).
 * `serverMapCurrentTargetDataAtom`과 같은 방식이다.
 */
const withGroupChildren = <E>(entries: E[] | undefined): E[] =>
  (entries ?? []).flatMap((entry) => {
    // filteredMap 응답 타입에는 이 필드가 없다(servicemap 응답에만 실린다).
    const { subNodes, subLinks } = entry as { subNodes?: E[]; subLinks?: E[] };
    return [entry, ...(subNodes ?? subLinks ?? [])];
  });

export function useServerMapOnClickMenuItem<
  T extends GetServerMap.NodeData | FilteredMap.NodeData,
  R extends GetServerMap.LinkData | FilteredMap.LinkData,
>({
  from,
  to,
  parsedHint,
  parsedFilters,
  setFilter,
  setShowFilter,
  setShowFilterConfig,
  serviceName,
}: {
  from: string;
  to: string;
  parsedHint?: FilteredMap.Hint; // filteredMap에서만 존재
  parsedFilters?: FilteredMap.FilterState[]; // filteredMap에서만 존재
  setFilter?: (filter: React.SetStateAction<FilteredMap.FilterState | undefined>) => void;
  setShowFilter?: (show: React.SetStateAction<boolean>) => void; // serverMap에서 사용
  setShowFilterConfig?: (show: React.SetStateAction<boolean>) => void; // filteredMap에서 사용
  /**
   * filteredMap 경로에 실을 service 이름. servicemap 계열 화면에서만 주어진다.
   * (filteredMap은 새 탭으로 열리므로 어떤 service를 보던 중이었는지 URL에 남아야 한다.)
   */
  serviceName?: string;
}) {
  const serverMapData = useAtomValue(serverMapDataAtom);

  return (type: SERVERMAP_MENU_FUNCTION_TYPE, data: Node | Edge) => {
    if (type === SERVERMAP_MENU_FUNCTION_TYPE.FILTER_WIZARD) {
      let serverInfos: Parameters<typeof getDefaultFilters>[1];
      if ('type' in data) {
        const nodeData = data as Node;
        const node = withGroupChildren(serverMapData?.applicationMapData.nodeDataArray as T[]).find(
          (n) => n.key === nodeData.id,
        );
        serverInfos = {
          agents: node?.agents?.map((agent) => agent.id),
        };
      } else if ('source' in data) {
        const edgeData = data as Edge;
        const link = withGroupChildren(serverMapData?.applicationMapData.linkDataArray as R[]).find(
          (l) => l.key === edgeData.id,
        );
        serverInfos = {
          fromAgents: link?.fromAgents?.map((agent) => agent.id),
          toAgents: link?.toAgents?.map((agent) => agent.id),
        };
      }

      setFilter?.(getDefaultFilters(data, serverInfos));
      setShowFilter?.(true);
      setShowFilterConfig?.(true);
    } else if (type === SERVERMAP_MENU_FUNCTION_TYPE.FILTER_TRANSACTION) {
      const defaultFilterState = getDefaultFilters(data);
      const link = withGroupChildren(serverMapData?.applicationMapData?.linkDataArray as R[]).find(
        (l) => l?.key === data?.id,
      );
      const sourceIsWas = link?.sourceInfo?.nodeCategory === GetServerMap.NodeCategory.SERVER;

      // 기준 application이 없으면 filteredMap은 조회 자체를 못 한다. 빈 화면을 새 탭으로
      // 열어 보여주는 대신 아무 것도 하지 않는다. (servicemap의 service group 링크)
      if (!defaultFilterState || !getFilterTargetApplication(defaultFilterState, sourceIsWas)) {
        return;
      }

      const addedHint =
        sourceIsWas && link?.targetInfo?.nodeCategory === GetServerMap.NodeCategory.SERVER
          ? {
              [link?.targetInfo?.applicationName]: link?.filter?.outRpcList,
            }
          : // eslint-disable-next-line @typescript-eslint/no-explicit-any
            ({} as any);
      window.open(
        `${BASE_PATH}${getFilteredMapPath(
          defaultFilterState,
          sourceIsWas,
          serviceName,
        )}?from=${from}&to=${to}${getFilteredMapQueryString({
          filterStates: [...(parsedFilters || [])!, defaultFilterState],
          hint: {
            currHint: parsedHint || {},
            addedHint,
          },
        })}`,
        '_blank',
      );
    }
  };
}
