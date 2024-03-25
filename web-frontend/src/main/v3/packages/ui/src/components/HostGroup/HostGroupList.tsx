import React from 'react';
import { Popover, PopoverClose, PopoverContent, PopoverTrigger, Separator } from '../ui';
import { LuChevronsUpDown } from 'react-icons/lu';
import { ErrorBoundary } from '../Error/ErrorBoundary';
import { HostGroupListFetcher } from './HostGroupListFetcher';
import { VirtualSearchList, ListItemSkeleton } from '../VirtualList';

export interface HostGroupListProps {
  open?: boolean;
  disabled?: boolean;
  selectedHostGroup?: string | null;
  selectPlaceHolder?: string;
  inputPlaceHolder?: string;
  onClickHostGroup?: (hostGroup: string) => void;
}

export const HostGroupList = ({
  open,
  disabled,
  selectedHostGroup,
  selectPlaceHolder = 'Select your host-group',
  inputPlaceHolder = 'Input host-group name',
  onClickHostGroup,
}: HostGroupListProps) => {
  const handleClickItem = (hostGroup: string) => {
    onClickHostGroup?.(hostGroup);
  };

  return (
    <Popover defaultOpen={open}>
      <PopoverTrigger className="text-sm w-80 min-w-80 disabled:opacity-50" disabled={disabled}>
        <div className="flex items-center p-1 pt-2 border-b-1">
          {selectedHostGroup ? (
            <div className="flex items-center overflow-hidden">
              <div className="truncate">{selectedHostGroup}</div>
            </div>
          ) : (
            selectPlaceHolder
          )}
          <LuChevronsUpDown className="ml-auto" />
        </div>
      </PopoverTrigger>
      <PopoverContent className="min-80 p-0 text-sm !z-[2001]">
        <VirtualSearchList
          inputClassName="focus-visible:ring-0 border-none shadow-none"
          placeHolder={inputPlaceHolder}
        >
          {(props) => {
            return (
              <div>
                <Separator />
                <div className="p-2 text-xs font-semibold">HostGroup List</div>
                <ErrorBoundary>
                  <React.Suspense
                    fallback={
                      <div className="h-48">
                        <ListItemSkeleton skeletonOption={{ viewBoxHeight: 192 }} />
                      </div>
                    }
                  >
                    <HostGroupListFetcher
                      {...props}
                      onClickItem={handleClickItem}
                      itemAs={PopoverClose}
                    />
                  </React.Suspense>
                </ErrorBoundary>
              </div>
            );
          }}
        </VirtualSearchList>
      </PopoverContent>
    </Popover>
  );
};
