import { TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';
import { ColumnDef } from '@tanstack/react-table';
import { formatInTimeZone } from 'date-fns-tz';
import {
  FaFire,
  FaDatabase,
  FaInfoCircle,
  FaPaperPlane,
  FaExchangeAlt,
  FaExclamationTriangle,
  FaLink,
  FaListUl,
} from 'react-icons/fa';
import { LuChevronRight, LuChevronDown } from 'react-icons/lu';
import { VscDebugStepInto, VscDebugStepOut } from 'react-icons/vsc';
import { useTranslation } from 'react-i18next';
import {
  Button,
  Tooltip,
  TooltipContent,
  TooltipPortal,
  TooltipProvider,
  TooltipTrigger,
} from '../../ui';
import { cn } from '@pinpoint-fe/ui/src/lib';
import { addCommas, getTimezone } from '@pinpoint-fe/ui/src/utils';
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
  // The Attribute/SQL icons trigger the detail view; the handler infers the type from the row data.
  onClickDetailView?: (data: TransactionInfo.CallStackKeyValueMap) => void;
  mapData?: TransactionInfo.CallStackKeyValueMap[];
  // "Step in": re-roots the tree at the given row, or steps back out when given ''.
  // Omitted -> no step in/out affordance.
  onStepSpan?: (id: string) => void;
  // The row that has been stepped into; its button stays filled in as the way back out.
  steppedInSpanId?: string;
  // Where a row that threw links to (the error analysis). Omitted -> the row shows its title only.
  getErrorAnalysisHref?: (errorKey: TransactionInfo.ErrorKey) => string;
  // What an OTel "Link" row shows to reach the linked transaction. Omitted -> the row shows its
  // title only. Both are passed in because building them needs the router and React Query, which
  // a view drawn outside Pinpoint web (an MCP Apps view) does not have.
  renderOtelLink?: (otelLink: OtelLinkArguments, text: string) => React.ReactNode;
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
  onStepSpan,
  steppedInSpanId,
  getErrorAnalysisHref,
  renderOtelLink,
  steppedInTimelineAxis,
}: CallTreeTableColumnsProps & { steppedInTimelineAxis?: TimelineAxis }) => {
  const transactionTimelineAxis = React.useMemo<TimelineAxis>(
    () => ({
      durationNanos: metaData.callTreeTimelineDurationNanos,
    }),
    [metaData.callTreeTimelineDurationNanos],
  );
  // After stepping in, the bars are drawn on that span's window instead, so the subtree fills
  // the column the same way the Flame Graph rescales.
  const timelineAxis = steppedInTimelineAxis ?? transactionTimelineAxis;
  const parallelGroups = React.useMemo(() => computeParallelGroups(mapData), [mapData]);
  const defaultColumns = React.useMemo(
    () =>
      callTreeTableColumns({
        metaData,
        onClickDetailView,
        timelineAxis,
        parallelGroups,
        onStepSpan,
        steppedInSpanId,
        getErrorAnalysisHref,
        renderOtelLink,
      }),
    [
      metaData,
      onClickDetailView,
      timelineAxis,
      parallelGroups,
      onStepSpan,
      steppedInSpanId,
      getErrorAnalysisHref,
      renderOtelLink,
    ],
  );

  // Only the *selection* is state; the column definitions are always derived from the current
  // `defaultColumns`. Holding built columns in state would freeze the closures they were created
  // with, so a new transaction (or a changed timeline axis) would keep rendering with stale data.
  const [visibleColumnIds, setVisibleColumnIds] = React.useState<CallTreeTableColumnId[]>();
  const columns = React.useMemo(
    () =>
      visibleColumnIds
        ? defaultColumns.filter((column) =>
            visibleColumnIds.includes(column.id as CallTreeTableColumnId),
          )
        : defaultColumns,
    [defaultColumns, visibleColumnIds],
  );

  const updateColumns = React.useCallback((columnIds: CallTreeTableColumnId[]) => {
    setVisibleColumnIds(columnIds);
  }, []);

  return { defaultColumns, columns, updateColumns };
};

export const callTreeTableColumns = ({
  onClickDetailView,
  timelineAxis,
  parallelGroups,
  onStepSpan,
  steppedInSpanId,
  getErrorAnalysisHref,
  renderOtelLink,
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
      cellClassName: 'p-0! grow-0',
      headerClassName: 'px-0.5 grow-0 ',
    },
  },
  {
    id: 'title',
    accessorKey: 'title',
    header: 'Method',
    cell: (props) => {
      const rowData = props.row.original;
      const isSteppedIn = String(rowData.id) === steppedInSpanId;
      return (
        <div className="flex items-center" style={{ paddingLeft: `${props.row.original.tab}rem` }}>
          {props.row.getCanExpand() && (
            <button onClick={props.row.getToggleExpandedHandler()} className="pr-1 cursor-pointer">
              {props.row.getIsExpanded() ? <LuChevronDown /> : <LuChevronRight />}
            </button>
          )}
          <MethodCell
            {...{
              rowData,
              onClickDetailView,
              getErrorAnalysisHref,
              renderOtelLink,
            }}
          />
          {/* Pinned to the right edge of the Method column (`ml-auto`) so it always sits in the
              same place instead of drifting with the method name's length.

              Only method rows can be stepped into — leaves included, since a leaf here can still
              be the parent of async work in another thread. The rest (SQL, annotations, exception
              details) describe the method above them rather than being spans of their own.
              The row already stepped into keeps its button whatever it is, so a root that somehow
              is not a method row still offers the way back out instead of a dead end. */}
          {onStepSpan && (rowData.isMethod || isSteppedIn) && (
            <StepSpanButton
              steppedIn={isSteppedIn}
              onClick={() => onStepSpan(isSteppedIn ? '' : String(rowData.id))}
            />
          )}
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

      // Each row is positioned on the Call Tree timeline axis supplied by the server, or on the
      // stepped-into span's window once the user has stepped in.
      const total = timelineAxis.durationNanos;
      const axisStart = timelineAxis.startNanos ?? 0;
      if (!Number.isFinite(total) || total <= 0) {
        return null;
      }

      // gaps (empty space) and overlaps (vertically overlapping bars) become visible.
      // A zero-duration row keeps elapsed 0; the `max(width, 2px)` floor below still draws a
      // minimum-width bar so the event's position stays visible instead of vanishing.
      const rawOffset = ((start - axisStart) / total) * 100;
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
        ? Math.min(Math.max(((parallel.group.start - axisStart) / total) * 100, 0), 100)
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
      const rowData = props.row.original;
      // OTel spans carry no Java class (simpleClassName empty); surface the OTel
      // instrumentation scope ("name@version") here instead — the analogous "origin
      // component" identity. Full value shown on hover since it can exceed the column.
      const value = rowData.scope || (props.getValue() as string);
      return (
        <span className="truncate" title={value}>
          {value}
        </span>
      );
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

const getDurationNanos = (millis?: number | string | null, nanos?: number | string | null) => {
  if (nanos !== undefined && nanos !== null && nanos !== '') {
    const nanosValue = Number(nanos);
    if (Number.isFinite(nanosValue)) {
      return nanosValue;
    }
  }
  return (Number(millis) || 0) * 1_000_000;
};

const formatDurationMillis = (millis?: number | string | null, nanos?: number | string | null) => {
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

const renderDurationMillis = (millis?: number | string | null, nanos?: number | string | null) => {
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

/**
 * Steps into this row's span (redrawing the tree from it) or, on the row already stepped into,
 * back out. Revealed on row hover (and on keyboard focus) so it adds no permanent noise, except
 * on the stepped-into row, where it stays filled in and visible as the way back out.
 */
const StepSpanButton = ({ steppedIn, onClick }: { steppedIn: boolean; onClick: () => void }) => {
  const { t } = useTranslation();
  const label = steppedIn
    ? t('TRANSACTION_LIST.STEP_OUT_SPAN')
    : t('TRANSACTION_LIST.STEP_IN_SPAN');

  return (
    <TooltipProvider delayDuration={0}>
      <Tooltip>
        <TooltipTrigger asChild>
          <Button
            variant={steppedIn ? 'default' : 'outline'}
            className={cn('flex-none w-5 h-5 p-0 ml-auto rounded', {
              'opacity-0 text-muted-foreground group-hover:opacity-100 focus-visible:opacity-100 hover:text-foreground':
                !steppedIn,
            })}
            aria-label={label}
            onClick={(e) => {
              e.stopPropagation();
              onClick();
            }}
          >
            {steppedIn ? <VscDebugStepOut /> : <VscDebugStepInto />}
          </Button>
        </TooltipTrigger>
        {/* Portalled: the Method cell clips its overflow, so an inline tooltip would be cut off. */}
        <TooltipPortal>
          <TooltipContent>{label}</TooltipContent>
        </TooltipPortal>
      </Tooltip>
    </TooltipProvider>
  );
};

const MethodCell = (props: {
  rowData: TransactionInfo.CallStackKeyValueMap;
  onClickDetailView: CallTreeTableColumnsProps['onClickDetailView'];
  getErrorAnalysisHref: CallTreeTableColumnsProps['getErrorAnalysisHref'];
  renderOtelLink: CallTreeTableColumnsProps['renderOtelLink'];
}) => {
  const { rowData, onClickDetailView, getErrorAnalysisHref, renderOtelLink } = props;
  let Icon;
  const text = rowData.title;

  if (rowData.hasException) {
    Icon = <FaFire className="fill-status-fail" />;

    const errorKey: TransactionInfo.ErrorKey | null = rowData.errorKey;
    const href = errorKey ? getErrorAnalysisHref?.(errorKey) : undefined;
    if (href) {
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
        <Button className="text-xs h-4 gap-0.5 p-1" onClick={() => onClickDetailView?.(rowData)}>
          <FaDatabase /> {text}
        </Button>
      );
    } else if (text === 'Link') {
      const otelLink = parseOtelLinkArguments(rowData.arguments);
      if (otelLink && renderOtelLink) {
        return <>{renderOtelLink(otelLink, text)}</>;
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

export type OtelLinkArguments = {
  traceId: string;
  spanId: string;
  linkTraceId: string;
  linkSpanId: string;
  focusTimestamp: number;
};

const parseOtelLinkArguments = (argumentsJson: string): OtelLinkArguments | null => {
  if (!argumentsJson) return null;
  try {
    const parsed = JSON.parse(argumentsJson);
    const { traceId, spanId, linkTraceId, linkSpanId, focusTimestamp } = parsed ?? {};
    if (!traceId || spanId == null || !linkTraceId || linkSpanId == null) return null;
    return {
      traceId: String(traceId),
      spanId: String(spanId),
      linkTraceId: String(linkTraceId),
      linkSpanId: String(linkSpanId),
      focusTimestamp: typeof focusTimestamp === 'number' ? focusTimestamp : 0,
    };
  } catch {
    return null;
  }
};
