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
  const [isOpen, setIsOpen] = React.useState(open);
  const [focusIndex, setFocusIndex] = React.useState<number>(0);
  const [hostGroupList, setHostGroupList] = React.useState<string[]>([]);

  React.useEffect(() => {
    setFocusIndex(0);
  }, [hostGroupList, isOpen]);

  const handleClickItem = (hostGroup: string) => {
    onClickHostGroup?.(hostGroup);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setFocusIndex((prev) => {
        if (prev === hostGroupList.length - 1) {
          return 0;
        }
        return prev + 1;
      });
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setFocusIndex((prev) => {
        if (prev === 0) {
          return hostGroupList.length - 1;
        }
        return prev - 1;
      });
    } else if (e.key === 'Enter') {
      e.preventDefault();
      const clickedItem = hostGroupList[focusIndex];
      if (clickedItem) {
        handleClickItem(clickedItem);
        setIsOpen(false);
      }
    }
  };

  const handleMouseEnter = (idx: number) => {
    setFocusIndex(idx);
  };

  return (
    <Popover defaultOpen={open} open={isOpen} onOpenChange={setIsOpen}>
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
      <PopoverContent className="min-80 p-0 text-sm !z-[2001]" onKeyDown={handleKeyDown}>
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
                      focusIndex={focusIndex}
                      getFilteredList={setHostGroupList}
                      onMouseEnter={handleMouseEnter}
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
