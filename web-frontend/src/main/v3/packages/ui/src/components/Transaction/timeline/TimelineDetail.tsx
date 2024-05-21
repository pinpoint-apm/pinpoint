import React from 'react';
import { IoMdClose } from 'react-icons/io';
import { TraceViewerData } from '@pinpoint-fe/constants';
import { Separator } from '@radix-ui/react-dropdown-menu';
import { FlameNode } from './FlameGraph';
import { Button } from '../../../components/ui';

export interface TimelineDetailProps {
  node: FlameNode<TraceViewerData.TraceEvent>;
  onClose?: () => void;
}

export const TimelineDetail = ({ node, onClose }: TimelineDetailProps) => {
  return (
    <div className="w-2/5 border-l min-w-96">
      <div className="flex items-center h-12 p-2 text-sm font-semibold border-b relativ bg-secondary/50">
        Timeline detail
        <Button className="ml-auto" variant={'ghost'} size={'icon'} onClick={() => onClose?.()}>
          <IoMdClose className="w-5 h-5" />
        </Button>
      </div>
      <Separator />
      <div className="overflow-auto h-[calc(100%-3.2rem)]">
        <div className="p-2 pl-3 text-xs [&>*:nth-child(2n-1)]:font-semibold grid grid-cols-[10rem_auto] [&>*:nth-child(2n)]:break-all gap-1">
          <div>Name </div>
          <div>{node.name}</div>
          <div>Category </div>
          <div>{node.detail.cat}</div>
          <div>Start time </div>
          <div>{node.detail.ts}</div>
          <div>Duration </div>
          <div>{node.duration}</div>
          {node.detail?.args &&
            Object.entries(node.detail.args).map(([key, value]) => {
              return (
                <React.Fragment key={key}>
                  <div>{key}</div>
                  <div>{value}</div>
                </React.Fragment>
              );
            })}
          <div>track_id</div>
          <div>{node.detail.tid}</div>
        </div>
      </div>
    </div>
  );
};
