import React from 'react';
import Fuse from 'fuse.js';
import { RxMagnifyingGlass } from 'react-icons/rx';
import { useGetTraceViewerData, useTransactionSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import {
  TraceViewerData,
  TransactionInfoType as TransactionInfo,
  colors,
} from '@pinpoint-fe/ui/src/constants';
import {
  getColorByString,
  getContrastingTextColor,
  getDarkenHexColor,
} from '@pinpoint-fe/ui/src/lib/colors';
import { FlameGraph } from '../../FlameGraph';
import { cn } from '../../../lib';
import { TimelineDetail } from './TimelineDetail';
import { FlameNodeType } from '../../FlameGraph/FlameNode';
import { LuMoveDown, LuMoveUp } from 'react-icons/lu';
import { Button, Input } from '../..';
import { TimelineInfo } from './TimelineInfo';

export interface TimelineFetcherProps {
  transactionInfo?: TransactionInfo.Response;
}

export const TimelineFetcher = ({ transactionInfo }: TimelineFetcherProps) => {
  const { transactionInfo: transactionSearchParams } = useTransactionSearchParameters();
  const [selectedTrace, setSelectedTrace] = React.useState<TraceViewerData.TraceEvent>();
  const [input, setInput] = React.useState('');
  const [searchInput, setSearchInput] = React.useState('');
  const [focusedNodeId, setFocusedNodeId] = React.useState<string>();
  const [nodeFlows, setNodeFlows] = React.useState({
    // prev -> selected -> next node의 args.id 값
    prev: [] as string[],
    selected: undefined as string | undefined,
    next: [] as string[],
  });

  const { data } = useGetTraceViewerData({
    traceId: transactionInfo?.transactionId,
    spanId: `${transactionInfo?.spanId}`,
    agentId: transactionInfo?.agentId,
    focusTimestamp: transactionSearchParams?.focusTimestamp,
  });
  const flameGraphData = genFlameGraphData(data);

  React.useEffect(() => {
    setInput('');
    setSearchInput('');
    setFocusedNodeId(undefined);
    setSelectedTrace(undefined);
  }, [transactionInfo]);

  const fuzzySearch = React.useMemo(() => {
    return new Fuse(data?.traceEvents || [], {
      keys: ['name'],
      threshold: 0.3,
      shouldSort: false,
    });
  }, [data?.traceEvents]);

  const searchedList = searchInput
    ? fuzzySearch.search(searchInput).map(({ item }) => item)
    : undefined;
  const searchedListIds = searchedList?.map((item) => item.args.id);
  const focusedNodeIdIndex = searchedListIds?.findIndex((id) => id === focusedNodeId) || 0;

  const goToNextSearchIndex = () => {
    const id =
      (searchedListIds?.length || 0) > focusedNodeIdIndex + 1
        ? searchedListIds?.[focusedNodeIdIndex + 1]
        : searchedListIds?.[0];
    setFocusedNodeId(id);
    id && setSelectedTrace(getTraceDataById(id));
  };

  const backToPrevSearchIndex = () => {
    const id =
      focusedNodeIdIndex < 0
        ? searchedListIds?.[searchedListIds?.length - 1]
        : searchedListIds?.[focusedNodeIdIndex - 1];
    setFocusedNodeId(id);
    id && setSelectedTrace(getTraceDataById(id));
  };

  const getTraceDataById = (id: string) => {
    return data?.traceEvents.find((ev) => ev.args.id === id);
  };

  const findNodeFlow = (node: TraceViewerData.TraceEvent | undefined) => {
    // prev -> seledtedNode -> next 형태 flow를 찾기 위한 로직
    const selectedArgsId = node?.args?.id;
    const prevNodesArgsId: string[] = [];
    const nextNodesArgsId: string[] = [];

    // 클릭 된 node에서 시작 된 경우 (클릭 된 node와 args.id가 같고 ph가 's'인 경우)
    const startBySelectedNodeIds = data?.traceEvents
      ?.filter((ev) => {
        return ev?.args?.id === selectedArgsId && ev?.ph === 's';
      })
      ?.map((ev) => ev?.id);

    // 클릭 된 node로 끝나는 경우 (클릭 된 node와 args.id가 같고 ph가 'f'인 경우)
    const finishInSelectedNodeIds = data?.traceEvents
      ?.filter((ev) => {
        return ev?.args?.id === selectedArgsId && ev?.ph === 'f';
      })
      ?.map((ev) => ev?.id);

    data?.traceEvents?.forEach((ev) => {
      if (startBySelectedNodeIds?.includes(ev?.id) && ev?.ph === 'f') {
        // 클릭 된 node에서 시작 되어 끝나는 node 찾기
        nextNodesArgsId.push(ev?.args?.id);
      } else if (finishInSelectedNodeIds?.includes(ev?.id) && ev?.ph === 's') {
        // 클릭 된 node에서 끝나는 시작 node 찾기
        prevNodesArgsId.push(ev?.args?.id);
      }
    });

    setNodeFlows({
      prev: prevNodesArgsId,
      selected: selectedArgsId,
      next: nextNodesArgsId,
    });
  };

  return (
    <div className={cn('h-full flex relative')}>
      <div className="absolute -top-10 right-4 h-7 border flex rounded pr-0.5 w-64 placeholder">
        <Input
          className="h-full text-xs border-none shadow-none focus-visible:ring-0 placeholder:text-xs"
          placeholder="Search trace events..."
          value={input}
          onChange={(e) => setInput(e.currentTarget.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              if (e.shiftKey) {
                setSearchInput((prev) => {
                  if (prev === input) {
                    backToPrevSearchIndex();
                  }
                  return input;
                });
              } else {
                setSearchInput((prev) => {
                  if (prev === input) {
                    goToNextSearchIndex();
                  }
                  return input;
                });
              }
            } else if (e.key === 'Escape') {
              if (input) {
                setInput('');
                setSearchInput('');
              } else {
                setSelectedTrace(undefined);
                setFocusedNodeId(undefined);
              }
            }
          }}
        />
        <div className="flex items-center opacity-50">
          {searchedListIds && (
            <>
              <span className="whitespace-nowrap text-xxs">
                {searchedListIds?.findIndex((id) => id === focusedNodeId) + 1} of{' '}
                {searchedListIds?.length}
              </span>
              <Button
                variant="ghost"
                className="h-full p-0.5"
                onClick={() => backToPrevSearchIndex()}
              >
                <LuMoveUp />
              </Button>
              <Button
                variant="ghost"
                className="h-full p-0.5"
                onClick={() => goToNextSearchIndex()}
              >
                <LuMoveDown />
              </Button>
            </>
          )}
          <Button variant="ghost" className="h-full p-0.5" onClick={() => setSearchInput(input)}>
            <RxMagnifyingGlass />
          </Button>
        </div>
      </div>
      <FlameGraph<TraceViewerData.TraceEvent>
        data={flameGraphData}
        start={transactionInfo?.callStackStart}
        end={transactionInfo?.callStackEnd}
        nodeFlows={nodeFlows}
        customNodeStyle={(node, _color) => {
          const nodeApplicationName = node?.detail?.args?.['Application Name'] || '';
          const color = nodeApplicationName ? getColorByString(nodeApplicationName) : _color?.color;
          const hoverColor = getDarkenHexColor(color);

          const id = node.detail.args.id;
          const isFocused = focusedNodeId === id || selectedTrace?.args.id === id;

          return {
            fill: isFocused ? hoverColor : color,
            stroke: colors.white,
            strokeWidth: 0.5,
          };
        }}
        customTextStyle={(node, _color) => {
          const nodeApplicationName = node?.detail?.args?.['Application Name'] || '';
          const id = node.detail.args.id;
          const isFocused = focusedNodeId === id || selectedTrace?.args.id === id;
          const isHighLighted = searchedListIds?.includes(id);

          const hilightedStyle: React.CSSProperties = isHighLighted
            ? {
                stroke: colors.black,
                fill: colors.yellow[300],
                strokeWidth: 3,
                paintOrder: 'stroke',
                strokeLinejoin: 'round',
              }
            : {
                fill: getContrastingTextColor(getColorByString(nodeApplicationName)) || _color,
              };
          return {
            ...hilightedStyle,
            fontWeight: isFocused ? 'bold' : '',
            textDecoration: isFocused || isHighLighted ? 'underline' : '',
          };
        }}
        onClickNode={(node) => {
          findNodeFlow(node?.detail);
          setSelectedTrace(node.detail as TraceViewerData.TraceEvent);
        }}
      />
      {selectedTrace && (
        <TimelineDetail
          start={transactionInfo?.callStackStart || 0}
          data={selectedTrace}
          onClose={() => {
            setNodeFlows({
              prev: [],
              selected: undefined,
              next: [],
            });
            setSelectedTrace(undefined);
            setFocusedNodeId(undefined);
          }}
        />
      )}
      <TimelineInfo data={data?.traceEvents} selectedTrace={selectedTrace} />
    </div>
  );
};

const genFlameGraphData = (data?: TraceViewerData.Response) => {
  let result: FlameNodeType<TraceViewerData.TraceEvent>[][] = [];
  if (data) {
    const traceEvents = data?.traceEvents || [];
    const mapByTid: { [key: number]: TraceViewerData.TraceEvent[] } = {};

    traceEvents.forEach((item) => {
      const { tid } = item;

      if (mapByTid[tid]) {
        mapByTid[tid].push(item);
      } else {
        mapByTid[tid] = [];
        mapByTid[tid].push(item);
      }
    });

    result = Object.values(mapByTid).map((traceEventsByTid) => {
      const roots: FlameNodeType<TraceViewerData.TraceEvent>[] = [];
      const map: { [key: string]: FlameNodeType<TraceViewerData.TraceEvent> } = {};

      traceEventsByTid.forEach((item) => {
        const { name } = item;

        if (name !== 'Async Trace') {
          const { id } = item.args;
          map[id] = {
            id,
            children: [],
            start: item.ts / 1000,
            duration: item.dur / 1000,
            detail: item,
            name,
          };
        }
      });

      // find Roots
      Object.values(map).forEach((item) => {
        const { parentId } = item.detail.args;
        if (!map[parentId]) {
          roots.push(item);
        }
      });

      traceEventsByTid.forEach((item) => {
        const { id, parentId } = item.args;
        const node = map[id];

        map[parentId]?.children.push(node);
      });
      return roots;
    });
  }

  return result;
};
