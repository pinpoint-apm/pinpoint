import React from 'react';
import { RxCheck } from 'react-icons/rx';
import { useGetSystemMetricHostData } from '@pinpoint-fe/hooks';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '../ui';
import { cn } from '../../lib';

export interface HostListFetcherProps {
  className?: string;
  filterKeyword?: string;
  selectedHost?: string;
  emptyMessage?: React.ReactNode;
  onClickHost?: (host: string) => void;
}

export const HostListFetcher = ({
  className,
  selectedHost = '',
  filterKeyword = '',
  emptyMessage = 'No hosts',
  onClickHost,
}: HostListFetcherProps) => {
  const { data } = useGetSystemMetricHostData();
  const filteredList = React.useMemo(
    () => data?.filter((host) => host.toLowerCase().includes(filterKeyword.toLowerCase())),
    [data, filterKeyword],
  );

  React.useEffect(() => {
    if (selectedHost) {
      return;
    }

    onClickHost?.(data?.[0] || '');
  }, [data]);

  return (
    <div className={className}>
      {filteredList && filteredList.length > 0 ? (
        <TooltipProvider>
          {filteredList?.map((host, i) => {
            return (
              <Tooltip key={i}>
                <TooltipTrigger className="w-full">
                  <div
                    className={cn(
                      'flex items-center gap-2 p-1 px-2 text-xs rounded cursor-pointer hover:bg-accent h-7',
                      {
                        'bg-accent': selectedHost === host,
                      },
                    )}
                    onClick={() => onClickHost?.(host)}
                  >
                    <div className="grid grid-cols-[1.25rem_auto] items-center">
                      {selectedHost === host ? <RxCheck /> : <span />}
                      <div className="truncate">{host}</div>
                    </div>
                  </div>
                </TooltipTrigger>
                <TooltipContent>{host}</TooltipContent>
              </Tooltip>
            );
          })}
        </TooltipProvider>
      ) : (
        <div className="flex items-center justify-center p-3 opacity-50">{emptyMessage}</div>
      )}
    </div>
  );
};
