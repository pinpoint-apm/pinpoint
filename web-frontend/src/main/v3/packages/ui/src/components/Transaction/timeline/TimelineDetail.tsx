import React from 'react';
import { IoMdClose } from 'react-icons/io';
import { TraceViewerData } from '@pinpoint-fe/constants';
import { Separator } from '@radix-ui/react-dropdown-menu';
import { Button } from '../../../components/ui';
import { useSetAtom } from 'jotai';
import { transactionInfoCallTreeFocusId, transactionInfoCurrentTabId } from '@pinpoint-fe/ui/atoms';

export interface TimelineDetailProps {
  start: number;
  data: TraceViewerData.TraceEvent;
  onClose?: () => void;
}

export const TimelineDetail = ({ start, data, onClose }: TimelineDetailProps) => {
  const setCurrentTab = useSetAtom(transactionInfoCurrentTabId);
  const setCallTreeFocusId = useSetAtom(transactionInfoCallTreeFocusId);

  return (
    <div className="w-2/5 border-l min-w-96">
      <div className="flex items-center h-12 p-2 text-sm font-semibold border-b relativ bg-secondary/50">
        <div className="truncate">{data.name}</div>
        <div className="flex items-center ml-auto">
          <Button
            className="text-xs text-nowrap"
            variant="link"
            onClick={() => {
              setCurrentTab('callTree');
              setCallTreeFocusId(data.args.id);
            }}
          >
            View in Call Tree
          </Button>
          <Button variant={'ghost'} size={'icon'} onClick={() => onClose?.()}>
            <IoMdClose className="w-5 h-5" />
          </Button>
        </div>
      </div>
      <Separator />
      <div className="overflow-auto h-[calc(100%-3.2rem)]">
        <div className="p-2 pl-3 pb-4 text-xs [&>*:nth-child(2n-1)]:font-semibold grid grid-cols-[10rem_auto] [&>*:nth-child(2n)]:break-all gap-1">
          <div>name</div>
          <div>{data.name}</div>
          <div>Application Name</div>
          <div>{data.args['Application Name']}</div>
          <div>Category </div>
          <div>{data.cat}</div>
          <div>Start time </div>
          <div>{(data.ts - start * 1000) / 1000}ms</div>
          <div>Duration </div>
          <div>{data.dur / 1000}ms</div>
          {data?.args &&
            Object.entries(data.args).map(([key, value], i) => {
              if (key === 'Application Name') {
                return <React.Fragment key={i}></React.Fragment>;
              }
              return (
                <React.Fragment key={key}>
                  <div>{key}</div>
                  <div>{value}</div>
                </React.Fragment>
              );
            })}
          <div>track_id</div>
          <div>{data.tid}</div>
        </div>
      </div>
    </div>
  );
};
