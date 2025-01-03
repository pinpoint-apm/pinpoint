import { atom } from 'jotai';
import {
  FilteredMapType as FilteredMap,
  GetResponseTimeHistogram,
  GetServerMap,
  SearchApplication,
} from '@pinpoint-fe/ui/constants';
import { Node, Edge } from '@pinpoint-fe/server-map';

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

// 서버맵에서 선택된 타겟
export const serverMapCurrentTargetAtom = atom<CurrentTarget | undefined>(undefined);

export const serverMapCurrentTargetDataAtom = atom((get) => {
  const currentTarget = get(serverMapCurrentTargetAtom);
  const serverMapData = get(serverMapDataAtom);

  if (currentTarget?.serviceType === 'USER') {
    return (serverMapData?.applicationMapData?.nodeDataArray as ServerMapNodeDataArray)?.find(
      ({ category }) => category === currentTarget.serviceType,
    );
  } else if (currentTarget?.type === 'node') {
    return (serverMapData?.applicationMapData?.nodeDataArray as ServerMapNodeDataArray)?.find(
      ({ key }) =>
        key === currentTarget?.id ||
        key === `${currentTarget?.applicationName}^${currentTarget?.serviceType}`,
    );
  } else if (currentTarget?.type === 'edge') {
    return (serverMapData?.applicationMapData?.linkDataArray as ServerMapNodeLinkArray)?.find(
      ({ key }) => key === currentTarget?.id,
    );
  } else {
    return undefined;
  }
});

export const currentNodeStatisticsAtom = atom<GetResponseTimeHistogram.Response | undefined>(
  undefined,
);

// server-list 선택시
export const currentServerAtom = atom<SearchApplication.Instance | undefined>(undefined);

export const currentServerAgentIdAtom = atom<string | undefined>((get) => {
  const currentServer = get(currentServerAtom);
  return currentServer?.agentId;
});

export const realtimeDateRanage = atom<{ from: Date; to: Date } | undefined>(undefined);
