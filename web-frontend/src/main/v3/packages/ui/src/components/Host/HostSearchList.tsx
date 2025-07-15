import { HostList, HostListProps } from '.';
import { VirtualSearchList } from '../VirtualList';
import { cn } from '../../lib/utils';
import { Badge } from '../../components/ui/badge';
import React from 'react';
import { useHeightToBottom } from '@pinpoint-fe/ui/src/hooks';

export interface HostSearchListProps {
  className?: string;
  selectedHost?: HostListProps['selectedHost'];
  onClickHost?: HostListProps['onClickHost'];
}

export const HostSearchList = ({ className, selectedHost, onClickHost }: HostSearchListProps) => {
  const listContainerRef = React.useRef(null);
  const height = useHeightToBottom({ ref: listContainerRef, offset: 0 });

  return (
    <div className={cn('text-sm space-y-2 py-5 h-full', className)}>
      <div className="flex items-center px-3">
        <span className="font-semibold">Host List</span>
      </div>
      {selectedHost && (
        <Badge
          variant={'outline'}
          className="flex items-center justify-between gap-2 py-1 mx-2 font-semibold truncate cursor-pointer bg-secondary"
        >
          <div className="truncate">{selectedHost}</div>
        </Badge>
      )}
      <VirtualSearchList
        inputContainerClassName="border rounded mx-2"
        inputClassName="focus-visible:ring-0 border-none shadow-none"
        placeHolder="Input host name"
      >
        {(props) => {
          return (
            <div ref={listContainerRef}>
              <HostList
                style={{
                  maxHeight: selectedHost ? `calc(${height}px - 2.25rem)` : `${height}px`,
                }}
                className="p-2 overflow-y-auto"
                filterKeyword={props.filterKeyword}
                selectedHost={selectedHost}
                onClickHost={(host) => onClickHost?.(host)}
              />
            </div>
          );
        }}
      </VirtualSearchList>
    </div>
  );
};
