import { useAtomValue } from 'jotai';
import { serverMapDataAtom } from '@pinpoint-fe/ui/src/atoms/serverMap';
import {
  BASE_PATH,
  FilteredMapType as FilteredMap,
  GetServerMap,
} from '@pinpoint-fe/ui/src/constants';
import { getFilteredMapQueryString, getFilteredMapPath } from '@pinpoint-fe/ui/src/utils';
import { getDefaultFilters, SERVERMAP_MENU_FUNCTION_TYPE } from '@pinpoint-fe/ui/src/components';
import { Edge, Node } from '@pinpoint-fe/server-map';

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
}: {
  from: string;
  to: string;
  parsedHint?: FilteredMap.Hint; // filteredMap에서만 존재
  parsedFilters?: FilteredMap.FilterState[]; // filteredMap에서만 존재
  setFilter?: (filter: React.SetStateAction<FilteredMap.FilterState | undefined>) => void;
  setShowFilter?: (show: React.SetStateAction<boolean>) => void; // serverMap에서 사용
  setShowFilterConfig?: (show: React.SetStateAction<boolean>) => void; // filteredMap에서 사용
}) {
  const serverMapData = useAtomValue(serverMapDataAtom);

  return (type: SERVERMAP_MENU_FUNCTION_TYPE, data: Node | Edge) => {
    if (type === SERVERMAP_MENU_FUNCTION_TYPE.FILTER_WIZARD) {
      let serverInfos: Parameters<typeof getDefaultFilters>[1];
      if ('type' in data) {
        const nodeData = data as Node;
        const node = (serverMapData?.applicationMapData.nodeDataArray as T[]).find(
          (n) => n.key === nodeData.id,
        );
        serverInfos = {
          agents: node?.agents?.map((agent) => agent.id),
        };
      } else if ('source' in data) {
        const edgeData = data as Edge;
        const link = (serverMapData?.applicationMapData.linkDataArray as R[]).find(
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
      const link = (serverMapData?.applicationMapData.linkDataArray as R[]).find(
        (l) => l.key === data.id,
      );
      const addedHint =
        link?.sourceInfo.isWas && link.targetInfo.isWas
          ? {
              [link.targetInfo.applicationName]: link.filter?.outRpcList,
            }
          : // eslint-disable-next-line @typescript-eslint/no-explicit-any
            ({} as any);
      window.open(
        `${BASE_PATH}${getFilteredMapPath(
          defaultFilterState!,
          link?.sourceInfo.isWas,
        )}?from=${from}&to=${to}${getFilteredMapQueryString({
          filterStates: [...(parsedFilters || [])!, defaultFilterState!],
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
