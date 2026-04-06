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

  // Extracts correct applicationName/serviceType from atom data, overriding
  // values parsed from node/edge IDs. This is necessary because serviceMap uses
  // "serviceName^applicationName^serviceType" key format which getDefaultFilters
  // cannot parse correctly.
  const getAtomApplicationInfo = (data: Node | Edge): Partial<FilteredMap.FilterState> => {
    if ('source' in data) {
      const edgeData = data as Edge;
      const link = (serverMapData?.applicationMapData?.linkDataArray as R[])?.find(
        (l) => l?.key === edgeData.id,
      );
      if (link?.sourceInfo && link?.targetInfo) {
        return {
          fromApplication: link.sourceInfo.applicationName,
          fromServiceType: link.sourceInfo.serviceType,
          toApplication: link.targetInfo.applicationName,
          toServiceType: link.targetInfo.serviceType,
        };
      }
    } else if ('type' in data) {
      const nodeData = data as Node;
      const node = (serverMapData?.applicationMapData?.nodeDataArray as T[])?.find(
        (n) => n?.key === nodeData.id,
      );
      if (node) {
        return { applicationName: node.applicationName, serviceType: node.serviceType };
      }
    }
    return {};
  };

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

      setFilter?.({
        ...getDefaultFilters(data, serverInfos),
        ...getAtomApplicationInfo(data),
      } as FilteredMap.FilterState);
      setShowFilter?.(true);
      setShowFilterConfig?.(true);
    } else if (type === SERVERMAP_MENU_FUNCTION_TYPE.FILTER_TRANSACTION) {
      const defaultFilterState = {
        ...getDefaultFilters(data),
        ...getAtomApplicationInfo(data),
      } as FilteredMap.FilterState;
      const link = (serverMapData?.applicationMapData?.linkDataArray as R[])?.find(
        (l) => l?.key === data?.id,
      );
      const addedHint =
        link?.sourceInfo?.nodeCategory === GetServerMap.NodeCategory.SERVER &&
        link?.targetInfo?.nodeCategory === GetServerMap.NodeCategory.SERVER
          ? {
              [link?.targetInfo?.applicationName]: link?.filter?.outRpcList,
            }
          : // eslint-disable-next-line @typescript-eslint/no-explicit-any
            ({} as any);
      window.open(
        `${BASE_PATH}${getFilteredMapPath(
          defaultFilterState!,
          link?.sourceInfo?.nodeCategory === GetServerMap.NodeCategory.SERVER,
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
