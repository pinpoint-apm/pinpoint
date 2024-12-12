import React from 'react';
import Fuse from 'fuse.js';
import { RxCheck } from 'react-icons/rx';
import { useGetSystemMetricHostData } from '@pinpoint-fe/ui/hooks';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '../ui';
import { cn } from '../../lib';

export interface HostListFetcherProps extends Pick<React.HTMLAttributes<HTMLDivElement>, 'style'> {
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
  style,
}: HostListFetcherProps) => {
  const { data } = useGetSystemMetricHostData();
  const fuzzySearch = React.useMemo(() => {
    return new Fuse(data || [], {
      threshold: 0.3,
    });
  }, [data]);

  const filteredList = filterKeyword
    ? fuzzySearch.search(filterKeyword).map(({ item }) => item)
    : data;

  React.useEffect(() => {
    if (selectedHost) {
      return;
    }

    onClickHost?.(data?.[0] || '');
  }, [data]);

  return (
    <div className={className} style={style}>
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
