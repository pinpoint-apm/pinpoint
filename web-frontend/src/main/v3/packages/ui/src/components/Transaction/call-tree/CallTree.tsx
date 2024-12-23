import React from 'react';
import { format } from 'date-fns';
import { usePostBind } from '@pinpoint-fe/ui/hooks';
import { useUpdateEffect } from 'usehooks-ts';
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
} from '../..';
import { TransactionInfo } from '@pinpoint-fe/constants';
import { RxMagnifyingGlass } from 'react-icons/rx';
import { HighLightCode } from '../../HighLightCode';
import { useAtomValue } from 'jotai';
import { transactionInfoCallTreeFocusId } from '@pinpoint-fe/ui/atoms';

export interface CallTreeProps {
  data: TransactionInfo.CallStackKeyValueMap[];
  mapData: TransactionInfo.CallStackKeyValueMap[];
  metaData: TransactionInfo.Response;
}

const filterList = [
  { id: 'executionMilliseconds', display: 'Self >=' },
  { id: 'all', display: 'All' },
  { id: 'hasException', display: 'Exception' },
  { id: 'arguments', display: 'Argument' },
];

export const CallTree = ({ data, mapData, metaData }: CallTreeProps) => {
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
  const focusRowIdIndex = filteredListIds?.findIndex((id) => id === focusRowId) || 0;
  const { mutate } = usePostBind({
    onSuccess: (result) => {
      setSqlDetail((prev) => {
        return { ...prev, bindedSql: result.bindedQuery };
      });
    },
  });
  const focusIdFromTimeline = useAtomValue(transactionInfoCallTreeFocusId);

  React.useEffect(() => {
    setFocusRowId(undefined);
  }, [data]);

  React.useEffect(() => {
    setFocusRowId(focusIdFromTimeline);
  }, [focusIdFromTimeline]);

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
      filteredList = mapData.filter((d) => d[filter]);
      const indexLists = filteredList.map((item) => item.id);
      setFilteredListIds(indexLists);
      setFocusRowId(indexLists[0]);
    } else if (filterInput) {
      let filteredList: TransactionInfo.CallStackKeyValueMap[] = [];

      if (filter === 'all') {
        filteredList = mapData.filter((d) =>
          Object.values(d).some((value) => `${value}`.toLowerCase().includes(filterInput)),
        );
      } else if (filter === 'executionMilliseconds') {
        filteredList = mapData.filter((d) => d[filter] >= Number(filterInput));
      } else if (filter === 'arguments') {
        filteredList = mapData.filter((d) =>
          d[filter as keyof typeof d].toLowerCase().includes(filterInput),
        );
      }
      const indexLists = filteredList.map((item) => item.id);
      setFilteredListIds(indexLists);
      setFocusRowId(indexLists[0]);
    } else {
      setFilteredListIds(undefined);
      setFocusRowId(undefined);
    }
  }, [filterInput]);

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

  return (
    <div className="relative h-full">
      <div className="absolute flex gap-1 rounded -top-10 right-4 h-7">
        <Select value={filter} onValueChange={(value) => setFilter(value)}>
          <SelectTrigger className="w-24 h-full text-xs">
            <SelectValue placeholder="Theme" />
          </SelectTrigger>
          <SelectContent>
            {filterList.map((filter) => (
              <SelectItem className="text-xs" value={filter.id} key={filter.id}>
                {filter.display}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <div className="border flex rounded pr-0.5 w-64">
          <Input
            className="h-full text-xs border-none shadow-none focus-visible:ring-0 placeholder:text-xs"
            placeholder="Filter call tree..."
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
            {filteredListIds && (
              <>
                <span className="whitespace-nowrap text-xxs">
                  {filteredListIds?.findIndex((id) => id === focusRowId) + 1} of{' '}
                  {filteredListIds?.length}
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
      <CallTreeTable
        data={data}
        metaData={metaData}
        // scrollToIndex={(row) => row.findIndex((r) => r.original.id === callTreeFocusId)}
        focusRowIndex={Number(focusRowId) - 1}
        filteredRowIds={filteredListIds}
        onDoubleClickCell={(cell) => {
          let content = `${cell.getValue()}`;
          const originalData = cell.getContext().row.original;

          if (cell.column.id === 'excutionPercentage') {
            content = `${getExecPercentage(metaData, originalData).toFixed(0)}`;
          } else if (cell.column.id === 'begin') {
            content = originalData.begin
              ? `${format(originalData.begin, 'HH:mm:ss SSS')} (${originalData.begin})`
              : '';
          }
          setContent(content);
          setDialogOpen(true);
        }}
        onClickDetailView={(callStackData) => {
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
        }}
      />
      <Sheet open={openSheet} onOpenChange={setSheetOpen}>
        <SheetContent
          className="flex flex-col gap-0 w-3/5 sm:max-w-full z-[5000] px-0 py-4"
          overlayClassName="bg-transparent backdrop-blur-none"
          hideClose={true}
        >
          <SheetHeader className="px-5 pb-4">
            <SheetTitle className="flex items-center">
              SQL Detail
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
