import { HostList, HostListProps } from '.';
import { VirtualSearchList } from '../VirtualList';
import { cn } from '../../lib/utils';
import { Badge } from '../../components/ui/badge';
import React from 'react';
import { useHeightToBottom } from '@pinpoint-fe/ui/hooks';

export interface HostSearchListProps {
  className?: string;
  selectedHost?: HostListProps['selectedHost'];
  onClickHost?: HostListProps['onClickHost'];
}

export const HostSearchList = ({ className, selectedHost, onClickHost }: HostSearchListProps) => {
  const listContainerRef = React.useRef(null);
  const height = useHeightToBottom({ ref: listContainerRef, offset: 0 });

  return (
    <div className={cn('p-5 space-y-2 text-sm h-full', className)}>
      <div className="flex items-center mb-3">
        <span className="font-semibold">Host List</span>
      </div>
      {selectedHost && (
        <Badge
          variant={'outline'}
          className="flex items-center justify-between gap-2 py-1 font-normal font-semibold truncate bg-secondary"
        >
          <div className="truncate">{selectedHost}</div>
        </Badge>
      )}
      <VirtualSearchList
        className="[&>*:first-child]:border [&>*:first-child]:rounded-t"
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
                className="p-2 overflow-y-auto border border-t-0 rounded-b"
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
