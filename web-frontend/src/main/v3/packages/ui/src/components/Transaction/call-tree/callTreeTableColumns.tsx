import {
  APP_PATH,
  BASE_PATH,
  SEARCH_PARAMETER_DATE_FORMAT,
  TransactionInfoType as TransactionInfo,
} from '@pinpoint-fe/ui/src/constants';
import { ColumnDef } from '@tanstack/react-table';
import { formatInTimeZone } from 'date-fns-tz';
import { Link } from 'react-router-dom';
import {
  FaFire,
  FaDatabase,
  FaInfoCircle,
  FaPaperPlane,
  FaExchangeAlt,
  FaExclamationTriangle,
  FaLink,
  FaListUl,
  FaPuzzlePiece,
} from 'react-icons/fa';
import { LuChevronRight, LuChevronDown } from 'react-icons/lu';
import { Button } from '../..';
import {
  addCommas,
  convertParamsToQueryString,
  getErrorAnalysisPath,
  getTimezone,
  getTransactionDetailPath,
  getTransactionDetailQueryString,
  getTransactionListPath,
} from '@pinpoint-fe/ui/src/utils';
import { useTimezone, useTransactionSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { transactionInfoCallTreeFocusId } from '@pinpoint-fe/ui/src/atoms';
import { useSetAtom } from 'jotai';
import React from 'react';
import {
  computeParallelGroups,
  getRowEndOffsetNanos,
  getRowStartOffsetNanos,
  isTimelineWorkRow,
  type TimelineAxis,
  type ParallelInfo,
} from './timeline';

export interface CallTreeTableColumnsProps {
  metaData: TransactionInfo.Response;
  // detailType distinguishes icons on the same row: the Scope icon passes 'scope';
  // the Attribute/SQL icons omit it (the handler infers the type from the row data).
  onClickDetailView?: (data: TransactionInfo.CallStackKeyValueMap, detailType?: 'scope') => void;
  mapData?: TransactionInfo.CallStackKeyValueMap[];
}

export type CallTreeTableColumnId =
  | 'index'
  | 'title'
  | 'arguments'
  | 'begin'
  | 'gap'
  | 'elapsedTime'
  | 'executionPercentage'
  | 'executionMilliseconds'
  | 'simpleClassName'
  | 'apiType'
  | 'applicationName'
  | 'agent'
  | 'agentName';

export const useCallTreeTableColumns = ({
  metaData,
  onClickDetailView,
  mapData,
}: CallTreeTableColumnsProps) => {
  const timelineAxis = React.useMemo<TimelineAxis>(
    () => ({
      durationNanos: metaData.callTreeTimelineDurationNanos,
    }),
    [metaData.callTreeTimelineDurationNanos],
  );
  const parallelGroups = React.useMemo(() => computeParallelGroups(mapData), [mapData]);
  const defaultColumns = React.useMemo(
    () => callTreeTableColumns({ metaData, onClickDetailView, timelineAxis, parallelGroups }),
    [metaData, onClickDetailView, timelineAxis, parallelGroups],
  );

  const [columns, setColumns] =
    React.useState<ColumnDef<TransactionInfo.CallStackKeyValueMap>[]>(defaultColumns);

  const updateColumns = React.useCallback(
    (columnIds: CallTreeTableColumnId[]) => {
      setColumns(
        defaultColumns.filter((column) => columnIds.includes(column.id as CallTreeTableColumnId)),
      );
    },
    [defaultColumns],
  );

  return { defaultColumns, columns, updateColumns };
};

export const callTreeTableColumns = ({
  metaData,
  onClickDetailView,
  timelineAxis,
  parallelGroups,
}: CallTreeTableColumnsProps & {
  timelineAxis: TimelineAxis;
  parallelGroups: ParallelInfo;
}): ColumnDef<TransactionInfo.CallStackKeyValueMap>[] => [
  {
    id: 'index',
    accessorKey: 'index',
    header: '',
    cell: (props) => {
      return (
        <div
          style={{ backgroundColor: calcColor(props.row.original) }}
          className="w-full h-full"
        ></div>
      );
    },
    size: 10,
    meta: {
      cellClassName: '!p-0 grow-0',
      headerClassName: 'px-0.5 grow-0 ',
    },
  },
  {
    id: 'title',
    accessorKey: 'title',
    header: 'Method',
    cell: (props) => {
      const rowData = props.row.original;
      return (
        <div className="flex items-center" style={{ paddingLeft: `${props.row.original.tab}rem` }}>
          {props.row.getCanExpand() && (
            <button onClick={props.row.getToggleExpandedHandler()} className="pr-1 cursor-pointer">
              {props.row.getIsExpanded() ? <LuChevronDown /> : <LuChevronRight />}
            </button>
          )}
          <MethodCell
            {...{
              metaData,
              rowData,
              onClickDetailView,
            }}
          />
        </div>
      );
    },
    size: 350,
  },
  {
    id: 'arguments',
    accessorKey: 'arguments',
    header: 'Arguments',
    cell: (props) => {
      return props.getValue();
    },
  },
  {
    id: 'begin',
    accessorKey: 'begin',
    header: 'StartTime',
    size: 90,
    cell: (props) => {
      const timestamp = props.getValue() as number;
      const timezone = getTimezone();

      return timestamp ? formatInTimeZone(timestamp, timezone, 'HH:mm:ss SSS') : '';
    },
    meta: {
      headerClassName: 'grow-0',
      cellClassName: 'grow-0',
    },
  },
  {
    id: 'gap',
    accessorKey: 'gap',
    header: 'Gap(ms)',
    size: 65,
    cell: (props) => {
      const rowData = props.row.original;
      const text = renderDurationMillis(rowData.gap, rowData.gapNanos);
      // parallel group member: keep the (possibly negative) gap value, add a ∥ marker
      const parallel = parallelGroups.get(String(rowData.id));
      if (parallel) {
        return (
          <span className="inline-flex items-center justify-end gap-0.5">
            {text}
            <span className="font-bold text-primary" title="parallel execution">
              ∥
            </span>
          </span>
        );
      }
      return text;
    },
    meta: {
      cellClassName: 'grow-0 text-right',
      headerClassName: 'grow-0',
    },
  },
  {
    id: 'elapsedTime',
    accessorKey: 'elapsedTime',
    header: 'Exec(ms)',
    size: 65,
    cell: (props) => {
      const rowData = props.row.original;
      return renderDurationMillis(rowData.elapsedTime, rowData.elapsedTimeNanos);
    },
    meta: {
      cellClassName: 'grow-0 text-right',
      headerClassName: 'grow-0',
    },
  },
  {
    id: 'executionPercentage',
    accessorKey: 'executionPercentage',
    header: 'Exec(%)',
    size: 220,
    cell: (props) => {
      const rowData = props.row.original;
      // Only real work rows render timeline bars. Metadata rows such as annotations and
      // exception details are displayed in the Call Tree but are not timeline work.
      if (!isTimelineWorkRow(rowData)) {
        return null;
      }

      const start = getRowStartOffsetNanos(rowData);
      const end = getRowEndOffsetNanos(rowData);
      const elapsed = Math.max(end - start, 0);

      // Each row is positioned on the Call Tree timeline axis supplied by the server.
      const total = timelineAxis.durationNanos;
      if (!Number.isFinite(total) || total <= 0) {
        return null;
      }

      // gaps (empty space) and overlaps (vertically overlapping bars) become visible.
      // A zero-duration row keeps elapsed 0; the `max(width, 2px)` floor below still draws a
      // minimum-width bar so the event's position stays visible instead of vanishing.
      const rawOffset = (start / total) * 100;
      const rawWidth = (elapsed / total) * 100;
      const offset = Math.min(Math.max(rawOffset, 0), 100); // keep the bar start within the axis
      const width = Math.max(rawWidth, 0);
      const barLeft = `min(${offset}%, calc(100% - 2px))`;

      // self time (darker segment) as a ratio of this row's own elapsed time
      const selfNanos = getDurationNanos(rowData.executionMilliseconds, rowData.executionNanos);
      const selfRatio = elapsed > 0 ? Math.min(selfNanos / elapsed, 1) * 100 : 0;

      const barColor = calcColor(rowData);

      // parallel lane: members of a concurrent sibling group share a tinted band spanning the
      // group's time window. Each member draws its own segment at the same offset; stacked rows
      // form one continuous band, making the parallel block explicit.
      const parallel = parallelGroups.get(String(rowData.id));
      const laneLeft = parallel
        ? Math.min(Math.max((parallel.group.start / total) * 100, 0), 100)
        : 0;
      const laneWidth = parallel
        ? Math.max(((parallel.group.end - parallel.group.start) / total) * 100, 0)
        : 0;

      const tooltip =
        `start: +${formatDurationMillis(null, start)}ms\n` +
        `elapsed: ${formatDurationMillis(rowData.elapsedTime, rowData.elapsedTimeNanos)}ms\n` +
        `self: ${formatDurationMillis(rowData.executionMilliseconds, rowData.executionNanos)}ms`;

      return (
        <div className="flex items-center w-full h-full">
          <div className="relative w-full h-3 overflow-hidden">
            <div className="absolute inset-0 rounded-sm bg-muted/30" />
            {parallel && (
              <div
                className="absolute top-0 h-full border-l border-r border-dashed border-primary/50 bg-primary/10"
                style={{
                  left: `${laneLeft}%`,
                  width: `max(${laneWidth}%, 2px)`,
                }}
                title={`parallel ×${parallel.group.size}`}
              />
            )}
            <div
              className="absolute h-full rounded-sm"
              style={{
                left: barLeft,
                width: `max(${width}%, 2px)`,
                backgroundColor: `${barColor}66`,
              }}
              title={tooltip}
            >
              <div
                className="h-full rounded-sm"
                style={{ width: `${selfRatio}%`, backgroundColor: barColor }}
              />
            </div>
          </div>
        </div>
      );
    },
    meta: {
      headerClassName: 'grow-0',
      cellClassName: 'grow-0',
    },
  },
  {
    id: 'executionMilliseconds',
    accessorKey: 'executionMilliseconds',
    header: 'Self(ms)',
    size: 65,
    cell: (props) => {
      const rowData = props.row.original;
      return renderDurationMillis(rowData.executionMilliseconds, rowData.executionNanos);
    },
    meta: {
      cellClassName: 'grow-0 text-right',
      headerClassName: 'grow-0',
    },
  },
  {
    id: 'simpleClassName',
    accessorKey: 'simpleClassName',
    header: 'Class',
    cell: (props) => {
      return props.getValue();
    },
    meta: {
      // cellClassName: 'max-w-[30px]',
    },
  },
  {
    id: 'apiType',
    accessorKey: 'apiType',
    header: 'API',
    cell: (props) => {
      return props.getValue();
    },
    size: 130,
    meta: {
      // cellClassName: 'max-w-[30px]',
    },
  },
  {
    id: 'applicationName',
    accessorKey: 'applicationName',
    header: 'Application',
    cell: (props) => {
      return props.getValue();
    },
    size: 130,
    meta: {
      // cellClassName: 'max-w-[30px]',
    },
  },
  {
    id: 'agent',
    accessorKey: 'agent',
    header: 'Agent Id',
    cell: (props) => {
      return props.getValue();
    },
    size: 130,
    meta: {
      // cellClassName: 'max-w-[30px]',
    },
  },
  {
    id: 'agentName',
    accessorKey: 'agentName',
    header: 'Agent Name',
    size: 30,
    cell: (props) => {
      return props.getValue();
    },
    meta: {
      // cellClassName: 'max-w-[20px]',
    },
  },
];

const calcColor = (data: TransactionInfo.CallStackKeyValueMap) => {
  let hash = 0;
  let color = '#';
  const agent = data?.agent || data.attributedAgent;

  for (let i = 0; i < agent?.length; i++) {
    hash = agent.charCodeAt(i) + ((hash << 5) - hash);
  }
  for (let i = 0; i < 3; i++) {
    color += ('00' + ((hash >> (i * 8)) & 0xff).toString(16)).slice(-2);
  }

  return color;
};

const getDurationNanos = (
  millis?: number | string | null,
  nanos?: number | string | null,
) => {
  if (nanos !== undefined && nanos !== null && nanos !== '') {
    const nanosValue = Number(nanos);
    if (Number.isFinite(nanosValue)) {
      return nanosValue;
    }
  }
  return (Number(millis) || 0) * 1_000_000;
};

const formatDurationMillis = (
  millis?: number | string | null,
  nanos?: number | string | null,
) => {
  if (nanos !== undefined && nanos !== null && nanos !== '') {
    const nanosValue = Number(nanos);
    if (Number.isFinite(nanosValue)) {
      return addDurationCommas(formatNanosToMillis(nanosValue));
    }
  }

  if (millis === undefined || millis === null || millis === '') {
    return '';
  }
  return addCommas(millis);
};

const renderDurationMillis = (
  millis?: number | string | null,
  nanos?: number | string | null,
) => {
  const value = String(formatDurationMillis(millis, nanos));
  const [integer, fraction] = value.split('.');
  if (!fraction) {
    return value;
  }

  return (
    <>
      {integer}
      <span className="text-muted-foreground">.{fraction}</span>
    </>
  );
};

const formatNanosToMillis = (nanos: number) => {
  return (nanos / 1_000_000).toFixed(6).replace(/\.?0+$/, '');
};

const addDurationCommas = (value: string) => {
  const [integer, fraction] = value.split('.');
  if (fraction) {
    return `${addCommas(integer)}.${fraction}`;
  }
  return addCommas(integer);
};

const MethodCell = (props: {
  metaData: TransactionInfo.Response;
  rowData: TransactionInfo.CallStackKeyValueMap;
  onClickDetailView: CallTreeTableColumnsProps['onClickDetailView'];
}) => {
  const { application, transactionInfo, pathname, searchParameters } =
    useTransactionSearchParameters();
  const setCallTreeFocusId = useSetAtom(transactionInfoCallTreeFocusId);
  const { metaData, rowData, onClickDetailView } = props;
  let Icon;
  const text = rowData.title;
  const [timezone] = useTimezone();

  if (rowData.hasException) {
    Icon = <FaFire className="fill-status-fail" />;

    if (rowData.exceptionChainId) {
      // The call stack's begin/end can be empty (0) for many nodes, which would
      // produce a window around epoch 0 and a negative `from` that the backend
      // (Timestamp) rejects. Center the window on the transaction's focus
      // timestamp, the same absolute time passed as `timestamp` below. Fall back
      // to the transaction's start time when focusTimestamp is missing (0), e.g.
      // when reached via an OTel link that carries no focus timestamp.
      const baseTime = transactionInfo.focusTimestamp || metaData.callStackStart;
      const from = formatInTimeZone(baseTime - 150000, timezone, SEARCH_PARAMETER_DATE_FORMAT);
      const to = formatInTimeZone(baseTime + 150000, timezone, SEARCH_PARAMETER_DATE_FORMAT);
      const href = `${BASE_PATH}${getErrorAnalysisPath(application)}?${convertParamsToQueryString({
        from,
        to,
        transactionInfo: JSON.stringify({
          applicationName: rowData.applicationName,
          agentId: rowData.agent,
          spanId: transactionInfo.spanId,
          transactionId: metaData.transactionId,
          exceptionId: rowData.exceptionChainId,
          timestamp: transactionInfo.focusTimestamp,
          uriTemplate: metaData.uri,
        }),
        openErrorDetail: true,
      })}`;

      return (
        <>
          <div className="flex-none">{Icon}</div>
          <a href={href} target="_blank" className="ml-1 truncate text-primary hover:underline">
            {text}
          </a>
        </>
      );
    }
  } else if (!rowData.isMethod) {
    if (text === 'SQL' || text === 'MONGO-JSON') {
      return (
        <Button
          className="text-xs h-[1rem] gap-0.5 p-1"
          onClick={() => onClickDetailView?.(rowData)}
        >
          <FaDatabase /> {text}
        </Button>
      );
    } else if (text === 'Link') {
      const linkPath = buildOtelLinkPath(rowData.arguments, application, {
        pathname,
        searchParameters,
      });
      if (linkPath) {
        return (
          <Button asChild className="text-xs h-[1rem] gap-0.5 p-1">
            <Link to={linkPath} onClick={() => setCallTreeFocusId('')}>
              <FaLink /> {text}
            </Link>
          </Button>
        );
      }
      Icon = <FaLink />;
    } else {
      Icon = <FaInfoCircle />;
    }
  } else {
    switch (Number(rowData.methodType)) {
      case 100:
        Icon = <FaPaperPlane />;
        break;
      case 200:
        Icon = <FaExchangeAlt />;
        break;
      case 900:
        Icon = <FaExclamationTriangle />;
        break;
    }
  }
  return (
    <>
      <div className="flex-none">{Icon}</div>
      <div className="ml-1 truncate">{text}</div>
      {rowData.attributes && (
        <Button
          className="flex-none w-4 h-4 p-0 ml-1.5 text-xs bg-slate-500 text-white hover:bg-slate-600"
          title="Attribute"
          aria-label="Attribute"
          onClick={(e) => {
            e.stopPropagation();
            onClickDetailView?.(rowData);
          }}
        >
          <FaListUl />
        </Button>
      )}
      {rowData.scope && (
        <Button
          className="flex-none w-4 h-4 p-0 ml-1.5 text-xs bg-indigo-500 text-white hover:bg-indigo-600"
          title="Scope"
          aria-label="Scope"
          onClick={(e) => {
            e.stopPropagation();
            onClickDetailView?.(rowData, 'scope');
          }}
        >
          <FaPuzzlePiece />
        </Button>
      )}
    </>
  );
};

export const getExecPercentage = (
  metaData: TransactionInfo.Response,
  rowData: TransactionInfo.CallStackKeyValueMap,
) => {
  if (!isTimelineWorkRow(rowData)) {
    return 0;
  }
  const totalExecuteTime = Number(metaData.callTreeTimelineDurationNanos);
  if (!Number.isFinite(totalExecuteTime) || totalExecuteTime <= 0) {
    return 0;
  }
  const elapsed = getRowEndOffsetNanos(rowData) - getRowStartOffsetNanos(rowData);
  return (elapsed / totalExecuteTime) * 100;
};

const buildOtelLinkPath = (
  argumentsJson: string,
  application: Parameters<typeof getTransactionDetailPath>[0],
  context: {
    pathname: string;
    searchParameters: Record<string, string>;
  },
): string | null => {
  if (!argumentsJson) return null;
  try {
    const parsed = JSON.parse(argumentsJson);
    const { traceId, spanId, linkTraceId, linkSpanId, focusTimestamp } = parsed ?? {};
    if (!traceId || spanId == null || !linkTraceId || linkSpanId == null) return null;

    const transactionInfoParams = {
      agentId: '',
      spanId: String(spanId),
      traceId: String(traceId),
      focusTimestamp: typeof focusTimestamp === 'number' ? focusTimestamp : 0,
      linkTraceId: String(linkTraceId),
      linkSpanId: String(linkSpanId),
    };

    // Stay on /transactionList when the user opened the link from the list page,
    // so the upper transaction-list panel (heatmap drag results) is preserved.
    const onTransactionListPage =
      context.pathname === APP_PATH.TRANSACTION_LIST ||
      context.pathname.startsWith(`${APP_PATH.TRANSACTION_LIST}/`);
    if (onTransactionListPage) {
      return `${getTransactionListPath(application)}?${convertParamsToQueryString({
        ...context.searchParameters,
        transactionInfo: JSON.stringify(transactionInfoParams),
      })}`;
    }

    return `${getTransactionDetailPath(application)}?${getTransactionDetailQueryString(
      transactionInfoParams,
    )}`;
  } catch {
    return null;
  }
};
