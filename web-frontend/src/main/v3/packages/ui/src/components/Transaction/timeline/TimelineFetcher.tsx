import React from 'react';
import Fuse from 'fuse.js';
import { useAtom } from 'jotai';
import { useTranslation } from 'react-i18next';
import { VscDebugStepInto, VscDebugStepOut } from 'react-icons/vsc';
import { RxMagnifyingGlass } from 'react-icons/rx';
import { transactionInfoSteppedInSpanId } from '@pinpoint-fe/ui/src/atoms';
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
import { LuMoveDown, LuMoveUp } from 'react-icons/lu';
import {
  Button,
  Input,
  Tooltip,
  TooltipContent,
  TooltipPortal,
  TooltipProvider,
  TooltipTrigger,
} from '../..';
import { TimelineInfo } from './TimelineInfo';
import {
  collectSteppedInSpanIds,
  getFlameGroupsTimeRange,
  pruneFlameGroupsToSteppedIn,
} from '../stepInSpan';
import { genFlameGraphData } from './flameGraphData';

export interface TimelineFetcherProps {
  transactionInfo?: TransactionInfo.Response;
  // Flat call stack, the only view that holds the complete parent/child relation. "Step in"
  // needs it because async descendants live in their own flame graph groups.
  mapData?: TransactionInfo.CallStackKeyValueMap[];
  // Optional DOM node in the tab header to portal the toolbar into, so the tabs and the toolbar
  // share one flex-wrap row and never overlap when the panel narrows.
  toolbarSlot?: HTMLElement | null;
}

export const TimelineFetcher = ({
  transactionInfo,
  mapData,
  toolbarSlot,
}: TimelineFetcherProps) => {
  const { t } = useTranslation();
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
    linkTraceId: transactionSearchParams?.linkTraceId,
    linkSpanId: transactionSearchParams?.linkSpanId,
  });
  // "Step in" is shared with the Call Tree through the atom, and this tab can set it too.
  // `undefined` when nothing is stepped into, or when the id does not belong to this trace.
  const [steppedInSpanId, setSteppedInSpanId] = useAtom(transactionInfoSteppedInSpanId);
  const steppedInSpanIds = React.useMemo(
    () => collectSteppedInSpanIds(mapData, steppedInSpanId),
    [mapData, steppedInSpanId],
  );
  const allFlameGraphData = React.useMemo(() => genFlameGraphData(data), [data]);
  const flameGraphData = React.useMemo(
    () =>
      steppedInSpanIds
        ? pruneFlameGroupsToSteppedIn(allFlameGraphData, steppedInSpanIds)
        : allFlameGraphData,
    [allFlameGraphData, steppedInSpanIds],
  );
  const steppedInTimeRange = React.useMemo(
    () => (steppedInSpanIds ? getFlameGroupsTimeRange(flameGraphData) : undefined),
    [steppedInSpanIds, flameGraphData],
  );
  const isSteppedIn = Boolean(steppedInSpanIds);

  // Searching stays inside what is on screen, so the "n of m" counter matches the visible nodes.
  const searchTargets = React.useMemo(() => {
    const traceEvents = data?.traceEvents || [];
    return steppedInSpanIds
      ? traceEvents.filter((ev) => steppedInSpanIds.has(String(ev.args.id)))
      : traceEvents;
  }, [data?.traceEvents, steppedInSpanIds]);

  const fuzzySearch = React.useMemo(() => {
    return new Fuse(searchTargets, {
      keys: ['name'],
      threshold: 0.3,
      shouldSort: false,
    });
  }, [searchTargets]);

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
    if (id) {
      setSelectedTrace(getTraceDataById(id));
    }
  };

  const backToPrevSearchIndex = () => {
    const id =
      focusedNodeIdIndex < 0
        ? searchedListIds?.[searchedListIds?.length - 1]
        : searchedListIds?.[focusedNodeIdIndex - 1];
    setFocusedNodeId(id);
    if (id) {
      setSelectedTrace(getTraceDataById(id));
    }
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

  const clearSelection = () => {
    setNodeFlows({
      prev: [],
      selected: undefined,
      next: [],
    });
    setSelectedTrace(undefined);
    setFocusedNodeId(undefined);
  };

  // A new transaction starts with nothing selected. `nodeFlows` has to go with it: the ids in it
  // are per-trace record indices, so they would match unrelated nodes in the next trace and draw
  // arrows between them. Placed after `clearSelection` so it can reuse it.
  React.useEffect(() => {
    setInput('');
    setSearchInput('');
    clearSelection();
  }, [transactionInfo]);

  // Stepping in from the Call Tree can exclude whatever was selected here; keeping it would
  // leave the detail panel describing a node that is no longer drawn.
  React.useEffect(() => {
    if (selectedTrace && steppedInSpanIds && !steppedInSpanIds.has(String(selectedTrace.args.id))) {
      clearSelection();
    }
  }, [steppedInSpanIds, selectedTrace]);

  // Always present, and its direction follows what the click would do:
  //   stepped in, and either nothing selected or the root selected -> step back out
  //   a span selected that is not the current root                 -> step into it (drill deeper)
  //   nothing stepped into and nothing selected                    -> nothing to do, disabled
  const selectedSpanId = selectedTrace?.args?.id ? String(selectedTrace.args.id) : undefined;
  const canStepOut =
    Boolean(steppedInSpanIds) && (!selectedSpanId || selectedSpanId === steppedInSpanId);
  const canStepIn = !canStepOut && Boolean(selectedSpanId) && selectedSpanId !== steppedInSpanId;
  const stepDisabled = !canStepOut && !canStepIn;
  const stepLabel = canStepOut
    ? t('TRANSACTION_LIST.STEP_OUT_SPAN')
    : stepDisabled
      ? t('TRANSACTION_LIST.STEP_IN_SELECT_SPAN')
      : t('TRANSACTION_LIST.STEP_IN_SPAN');
  const stepButton = (
    <TooltipProvider delayDuration={0}>
      <Tooltip>
        {/* The span keeps the trigger hoverable while the button is disabled: a disabled button
            emits no pointer events, so the tooltip explaining *why* would never show. */}
        <TooltipTrigger asChild>
          <span className="inline-flex">
            <Button
              variant={canStepOut ? 'default' : 'outline'}
              size="sm"
              className="w-7 h-7 p-0"
              disabled={stepDisabled}
              aria-label={stepLabel}
              onClick={() => {
                if (canStepOut) {
                  setSteppedInSpanId('');
                } else if (selectedSpanId) {
                  setSteppedInSpanId(selectedSpanId);
                }
              }}
            >
              {canStepOut ? <VscDebugStepOut size={14} /> : <VscDebugStepInto size={14} />}
            </Button>
          </span>
        </TooltipTrigger>
        {/* Portalled like the Call Tree's button: the toolbar is portalled into the tab header,
            so an inline tooltip would be laid out inside that row instead of over the graph. */}
        <TooltipPortal>
          <TooltipContent>{stepLabel}</TooltipContent>
        </TooltipPortal>
      </Tooltip>
    </TooltipProvider>
  );

  const searchBox = (
    <div className="flex border rounded h-7 pr-0.5 w-64">
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
              clearSelection();
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
            <Button variant="ghost" className="h-full p-0.5" onClick={() => goToNextSearchIndex()}>
              <LuMoveDown />
            </Button>
          </>
        )}
        <Button variant="ghost" className="h-full p-0.5" onClick={() => setSearchInput(input)}>
          <RxMagnifyingGlass />
        </Button>
      </div>
    </div>
  );

  return (
    <div className={cn('h-full flex relative')}>
      {isSteppedIn && flameGraphData.length === 0 && (
        <div className="absolute inset-x-0 top-16 z-1 text-sm text-center text-muted-foreground">
          {t('TRANSACTION_LIST.STEP_IN_NO_SPANS')}
        </div>
      )}
      <FlameGraph<TraceViewerData.TraceEvent>
        data={flameGraphData}
        // After stepping in, the graph is rescaled to that subtree's own window.
        start={steppedInTimeRange?.start ?? transactionInfo?.callStackStart}
        end={steppedInTimeRange?.end ?? transactionInfo?.callStackEnd}
        toolbarSlot={toolbarSlot}
        toolbarStart={stepButton}
        toolbarEnd={searchBox}
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
          onClose={clearSelection}
        />
      )}
      <TimelineInfo data={data?.traceEvents} selectedTrace={selectedTrace} />
    </div>
  );
};
