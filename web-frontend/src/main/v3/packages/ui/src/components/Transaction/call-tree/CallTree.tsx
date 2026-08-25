import React from 'react';
import { createPortal } from 'react-dom';
import { useTranslation } from 'react-i18next';
import { formatInTimeZone } from 'date-fns-tz';
import { usePostBind, useTimezone, useUpdateEffect } from '@pinpoint-fe/ui/src/hooks';
import { IoMdClose } from 'react-icons/io';
import { LuMoveUp, LuMoveDown } from 'react-icons/lu';
import { Sheet, SheetContent, SheetHeader, SheetTitle } from '../../ui/sheet';
import { Button, Separator } from '../../..';
import {
  CallTreeTable,
  CollapsibleCodeViewer,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  Input,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  getExecPercentage,
  useCallTreeTableColumns,
} from '../..';
import { TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';
import { addCommas } from '@pinpoint-fe/ui/src/utils';
import { RxMagnifyingGlass } from 'react-icons/rx';
import { HighLightCode } from '../../HighLightCode';
import { useAtom, useAtomValue } from 'jotai';
import {
  transactionInfoCallTreeFocusId,
  transactionInfoSteppedInSpanId,
} from '@pinpoint-fe/ui/src/atoms';
import { CallTreeTableColumnsSetting } from './CallTreeTableColumnsSetting';
import {
  collectSteppedInSpanIds,
  findSteppedInTreeNode,
  flattenTreeRows,
  rebaseTreeIndent,
} from '../stepInSpan';
import { getSteppedInTimelineAxis } from './timeline';

export interface CallTreeProps {
  data: TransactionInfo.CallStackKeyValueMap[];
  mapData: TransactionInfo.CallStackKeyValueMap[];
  metaData: TransactionInfo.Response;
  // Optional DOM node in the tab header to portal the toolbar into, so the tabs and the
  // toolbar share one flex-wrap row and never overlap when the panel narrows.
  toolbarSlot?: HTMLElement | null;
}

const filterList = [
  { id: 'executionMilliseconds', display: 'Self >=' },
  { id: 'all', display: 'All' },
  { id: 'hasException', display: 'Exception' },
  { id: 'arguments', display: 'Argument' },
];

// Pretty-print compact JSON from the backend for readability; keep the raw string if not JSON.
const prettyJson = (raw: string) => {
  try {
    return JSON.stringify(JSON.parse(raw), null, 2);
  } catch {
    return raw;
  }
};

export const CallTree = ({ data, mapData, metaData, toolbarSlot }: CallTreeProps) => {
  const { t } = useTranslation();
  const [openSheet, setSheetOpen] = React.useState<boolean>(false);
  const [openDialog, setDialogOpen] = React.useState<boolean>(false);
  const [content, setContent] = React.useState<string>('');
  const [input, setInput] = React.useState('');
  const [filter, setFilter] = React.useState(filterList[0].id);
  const [filterInput, setFilterInput] = React.useState('');
  const [filteredListIds, setFilteredListIds] = React.useState<string[]>();
  const [focusRowId, setFocusRowId] = React.useState<string>();
  const [sqlDetail, setSqlDetail] = React.useState<{
    originalSql?: string;
    bindedSql?: string;
    bindValue?: string;
  }>();
  const [detailType, setDetailType] = React.useState<'sql' | 'attribute'>('sql');
  const [jsonDetail, setJsonDetail] = React.useState<string>('');
  const { mutate } = usePostBind({
    onSuccess: (result) => {
      setSqlDetail((prev) => {
        return { ...prev, bindedSql: result.bindedQuery };
      });
    },
  });
  const onClickDetailView = React.useCallback(
    (callStackData: TransactionInfo.CallStackKeyValueMap) => {
      if (callStackData.attributes) {
        setJsonDetail(prettyJson(callStackData.attributes));
        setDetailType('attribute');
        setSheetOpen(true);
        return;
      }

      setDetailType('sql');
      const nextItem = mapData?.find((d) => Number(d.id) === Number(callStackData.id) + 1);
      if (nextItem?.title === 'SQL-BindValue' || nextItem?.title === 'MONGO-JSON-BindValue') {
        const formData = new FormData();
        formData.append('type', callStackData.title === 'SQL' ? 'sql' : 'mongoJson');
        formData.append('metaData', callStackData.arguments);
        formData.append('bind', nextItem.arguments);
        mutate(formData);

        setSqlDetail({
          originalSql: callStackData.arguments,
          bindValue: nextItem.arguments,
        });
      } else {
        setSqlDetail({
          originalSql: callStackData.arguments,
          bindedSql: undefined,
          bindValue: undefined,
        });
      }
      setSheetOpen(true);
    },
    [mapData, mutate],
  );
  const focusRowIdIndex = Math.max(
    focusRowId === undefined ? 0 : (filteredListIds?.indexOf(focusRowId) ?? 0),
    0,
  );
  const hasFilteredList = Boolean(filteredListIds?.length);

  // "Step in": re-root the tree at one row so only that row and its descendants show.
  const [steppedInSpanId, setSteppedInSpanId] = useAtom(transactionInfoSteppedInSpanId);
  const steppedInSpanIds = React.useMemo(
    () => collectSteppedInSpanIds(mapData, steppedInSpanId),
    [mapData, steppedInSpanId],
  );
  const steppedInData = React.useMemo(() => {
    if (!steppedInSpanIds) {
      return data;
    }
    const steppedInRoot = findSteppedInTreeNode(data, steppedInSpanId);
    // The stepped-into row can be one the tree does not render (Attribute/Scope rows are lifted
    // onto their parent). Falling back to the whole tree beats showing nothing.
    return steppedInRoot ? [rebaseTreeIndent(steppedInRoot)] : data;
  }, [data, steppedInSpanId, steppedInSpanIds]);
  const steppedInTimelineAxis = React.useMemo(
    () => getSteppedInTimelineAxis(mapData, steppedInSpanIds),
    [mapData, steppedInSpanIds],
  );
  // The rows the table actually draws, in render order. Everything that has to agree with what
  // the user sees is derived from this one list: which rows the search may match (so the "n of m"
  // counter cannot count rows that are not there), and which position to scroll to. Reading it
  // from the rendered tree rather than the flat call stack also drops the Attribute/Scope rows,
  // which the tree lifts onto their parent instead of drawing.
  const visibleRows = React.useMemo(() => flattenTreeRows(steppedInData), [steppedInData]);
  const visibleRowIds = React.useMemo(
    () => visibleRows.map((row) => String(row.id)),
    [visibleRows],
  );
  const searchScope = visibleRows;
  const scrollRowIndex = focusRowId === undefined ? -1 : visibleRowIds.indexOf(String(focusRowId));

  const { defaultColumns, columns, updateColumns } = useCallTreeTableColumns({
    metaData,
    mapData,
    onClickDetailView,
    onStepSpan: setSteppedInSpanId,
    steppedInSpanId,
    steppedInTimelineAxis,
  });
  const focusIdFromTimeline = useAtomValue(transactionInfoCallTreeFocusId);
  const [timezone] = useTimezone();

  React.useEffect(() => {
    if (focusIdFromTimeline) {
      setFocusRowId(focusIdFromTimeline);
    } else {
      setFocusRowId(String(metaData?.focusCallStackId) || undefined);
    }
  }, [data, focusIdFromTimeline]);

  useUpdateEffect(() => {
    if (filter === 'hasException') {
      setFilterInput('hasException');
    } else {
      setFilterInput('');
    }
  }, [filter]);

  useUpdateEffect(() => {
    let filteredList: TransactionInfo.CallStackKeyValueMap[] = [];
    if (filter === 'hasException') {
      filteredList = searchScope.filter((d) => d[filter]);
      const indexLists = filteredList.map((item) => String(item.id));
      setFilteredListIds(indexLists);
      setFocusRowId(indexLists[0]);
    } else if (filterInput) {
      let filteredList: TransactionInfo.CallStackKeyValueMap[] = [];

      if (filter === 'all') {
        filteredList = searchScope.filter((d) =>
          // `subRows` holds the children themselves; stringifying them would match on nothing
          // meaningful. Every other field, including the lifted `attributes`/`scope`, is fair game.
          Object.entries(d).some(
            ([key, value]) => key !== 'subRows' && `${value}`.toLowerCase().includes(filterInput),
          ),
        );
      } else if (filter === 'executionMilliseconds') {
        filteredList = searchScope.filter(
          (d) => getExecutionMilliseconds(d) >= Number(filterInput),
        );
      } else if (filter === 'arguments') {
        filteredList = searchScope.filter((d) =>
          d[filter as keyof typeof d].toLowerCase().includes(filterInput),
        );
      }
      const indexLists = filteredList.map((item) => String(item.id));
      setFilteredListIds(indexLists);
      setFocusRowId(indexLists[0]);
    } else {
      setFilteredListIds(undefined);
      setFocusRowId(undefined);
    }
    // Re-runs on a step in/out so the match list never points outside the visible rows.
  }, [filterInput, searchScope]);

  // Stepping into a span marks it, the same way loading the page marks `focusCallStackId`. From
  // then on the mark belongs to the search: it moves with the hits and clears when the search is
  // cancelled, exactly as it does with nothing stepped into. Declared after the search effect so
  // it wins on the render where both fire (a step in/out also re-runs the search).
  useUpdateEffect(() => {
    setFocusRowId(steppedInSpanId || undefined);
  }, [steppedInSpanId]);

  const goToNextSearchIndex = () => {
    if ((filteredListIds?.length || 0) > focusRowIdIndex + 1) {
      setFocusRowId(filteredListIds?.[focusRowIdIndex + 1]);
    } else {
      setFocusRowId(filteredListIds?.[0]);
    }
  };

  const backToPrevSearchIndex = () => {
    if (focusRowIdIndex < 1) {
      setFocusRowId(filteredListIds?.[filteredListIds?.length - 1]);
    } else {
      setFocusRowId(filteredListIds?.[focusRowIdIndex - 1]);
    }
  };

  const toolbar = (
    <div className="flex flex-wrap items-center justify-end gap-1 gap-y-1">
      <CallTreeTableColumnsSetting defaultColumns={defaultColumns} updateColumns={updateColumns} />
      <Select value={filter} onValueChange={(value) => setFilter(value)}>
        <SelectTrigger className="w-24 text-xs h-7">
          <SelectValue placeholder={t('TRANSACTION_LIST.CALL_TREE_FILTER')} />
        </SelectTrigger>
        <SelectContent>
          {filterList.map((filter) => (
            <SelectItem className="text-xs" value={filter.id} key={filter.id}>
              {filter.display}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      <div className="border flex rounded pr-0.5 w-64 h-7">
        <Input
          className="h-full text-xs border-none shadow-none focus-visible:ring-0 placeholder:text-xs"
          placeholder={t('TRANSACTION_LIST.CALL_TREE_FILTER_PLACEHOLDER')}
          value={input}
          onChange={(e) => setInput(e.currentTarget.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              if (e.shiftKey) {
                setFilterInput((prev) => {
                  if (prev === input) {
                    backToPrevSearchIndex();
                  }
                  return input;
                });
              } else {
                setFilterInput((prev) => {
                  if (prev === input) {
                    goToNextSearchIndex();
                  }
                  return input;
                });
              }
            } else if (e.key === 'Escape') {
              setInput('');
              setFilterInput('');
            }
          }}
        />
        <div className="flex items-center opacity-50">
          {hasFilteredList && (
            <>
              <span className="whitespace-nowrap text-xxs">
                {focusRowIdIndex + 1} of {filteredListIds?.length}
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
          <Button variant="ghost" className="h-full p-0.5" onClick={() => setFilterInput(input)}>
            <RxMagnifyingGlass />
          </Button>
        </div>
      </div>
    </div>
  );

  return (
    <div className="relative flex flex-col h-full">
      {toolbarSlot ? (
        createPortal(toolbar, toolbarSlot)
      ) : (
        <div className="flex justify-end px-4 py-2">{toolbar}</div>
      )}
      <div className="flex-1 min-h-0">
        <CallTreeTable
          columns={columns || defaultColumns || []}
          data={steppedInData}
          metaData={metaData}
          // Index within the rendered rows: the row ids no longer equal their position once the
          // tree is re-rooted (and Attribute/Scope rows consume ids without taking a row).
          focusRowIndex={scrollRowIndex}
          highlightRowId={focusRowId}
          filteredRowIds={filteredListIds}
          onDoubleClickCell={(cell) => {
            const originalData = cell.getContext().row.original;
            const durationContent = getDurationContent(cell.column.id, originalData);
            let content = durationContent ?? `${cell.getValue()}`;

            if (cell.column.id === 'executionPercentage') {
              content = `${getExecPercentage(metaData, originalData).toFixed(0)}`;
            } else if (cell.column.id === 'begin') {
              content = originalData.begin
                ? `${formatInTimeZone(originalData.begin, timezone, 'HH:mm:ss SSS')} (${originalData.begin})`
                : '';
            }
            setContent(content);
            setDialogOpen(true);
          }}
        />
      </div>
      <Sheet open={openSheet} onOpenChange={setSheetOpen}>
        <SheetContent
          className="flex flex-col gap-0 w-3/5 sm:max-w-full z-[5000] px-0 py-4"
          overlayClassName="bg-transparent backdrop-blur-none"
          hideClose={true}
        >
          <SheetHeader className="px-5 pb-4">
            <SheetTitle className="flex items-center">
              {detailType === 'attribute' ? 'Attribute Detail' : 'SQL Detail'}
              <Button
                variant="outline"
                size="icon"
                className="ml-auto border-none shadow-none"
                onClick={() => setSheetOpen(!open)}
              >
                <IoMdClose className="w-5 h-5" />
              </Button>
            </SheetTitle>
          </SheetHeader>
          <Separator className="" />
          <div className="p-4 space-y-4 overflow-auto">
            {detailType === 'attribute' ? (
              <div className="relative space-y-2">
                <CollapsibleCodeViewer title="Attribute" code={jsonDetail} language="json" wrap />
              </div>
            ) : (
              <>
                {sqlDetail?.bindedSql && (
                  <div className="relative space-y-2">
                    <CollapsibleCodeViewer
                      title="Binded SQL"
                      code={sqlDetail?.bindedSql}
                      language="sql"
                    />
                  </div>
                )}
                <div className="relative space-y-2">
                  <CollapsibleCodeViewer
                    title="Original SQL"
                    code={sqlDetail?.originalSql || ''}
                    language="sql"
                  />
                  {sqlDetail?.bindedSql && (
                    <div className="relative space-y-2">
                      <CollapsibleCodeViewer
                        title="SQL Bind Value"
                        code={sqlDetail?.bindValue || ''}
                        language="sql"
                      />
                    </div>
                  )}
                </div>
              </>
            )}
          </div>
        </SheetContent>
      </Sheet>
      <Dialog open={openDialog} onOpenChange={setDialogOpen}>
        <DialogContent className="max-h-[90%] overflow-auto max-w-xl">
          <DialogHeader>
            <DialogTitle className="text-base">Content</DialogTitle>
          </DialogHeader>
          <HighLightCode className="p-2 text-xs min-h-20" code={content} />
        </DialogContent>
      </Dialog>
    </div>
  );
};

const getExecutionMilliseconds = (data: TransactionInfo.CallStackKeyValueMap) => {
  if (data.executionNanos !== undefined && data.executionNanos !== null) {
    return Number(data.executionNanos) / 1_000_000;
  }
  return Number(data.executionMilliseconds) || 0;
};

const getDurationContent = (
  columnId: string,
  data: TransactionInfo.CallStackKeyValueMap,
): string | undefined => {
  if (columnId === 'gap') {
    return formatDurationMillis(data.gap, data.gapNanos);
  }
  if (columnId === 'elapsedTime') {
    return formatDurationMillis(data.elapsedTime, data.elapsedTimeNanos);
  }
  if (columnId === 'executionMilliseconds') {
    return formatDurationMillis(data.executionMilliseconds, data.executionNanos);
  }
  return undefined;
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
