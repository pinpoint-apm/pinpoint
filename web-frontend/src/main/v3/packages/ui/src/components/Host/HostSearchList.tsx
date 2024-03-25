import { HostList, HostListProps } from '.';
import { VirtualSearchList } from '../VirtualList';
import { cn } from '../../lib/utils';

export interface HostSearchListProps {
  className?: string;
  selectedHost?: HostListProps['selectedHost'];
  onClickHost?: HostListProps['onClickHost'];
}

export const HostSearchList = ({ className, selectedHost, onClickHost }: HostSearchListProps) => {
  return (
    <div className={cn('p-5 pb-6 text-sm h-full', className)}>
      <div className="flex items-center mb-3">
        <span className="font-semibold">Host List</span>
      </div>
      <VirtualSearchList
        className="h-full max-h-[calc(100%-2rem)] [&>*:first-child]:border [&>*:first-child]:rounded-t"
        inputClassName="focus-visible:ring-0 border-none shadow-none"
        placeHolder="Input host name"
      >
        {(props) => {
          return (
            <>
              <HostList
                className="p-2 max-h-[calc(100%-2.25rem)] overflow-y-auto border border-t-0 rounded-b"
                filterKeyword={props.filterKeyword}
                selectedHost={selectedHost}
                onClickHost={(host) => onClickHost?.(host)}
              />
            </>
          );
        }}
      </VirtualSearchList>
    </div>
  );
};
