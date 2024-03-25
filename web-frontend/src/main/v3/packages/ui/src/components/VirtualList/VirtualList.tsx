import { Virtuoso } from 'react-virtuoso';
import { cn } from '../../lib/utils';

export interface VirtualListProps<T> {
  list?: T[];
  className?: string;
  itemClassName?: string;
  itemChild?: React.ReactNode | ((item: T) => React.ReactNode);
  empty?: React.ReactNode;
  filterKey?: keyof T;
  filterKeyword?: string;
  itemAs?: React.ElementType;
  onClickItem?: (item: T) => void;
}

export const VirtualList = <T,>({
  list,
  className,
  itemClassName,
  itemChild,
  empty = "We couldn't find anything.",
  filterKey,
  filterKeyword = '',
  onClickItem,
  itemAs: ListComponent = 'div',
}: VirtualListProps<T>) => {
  const filteredList = list?.filter((l) => {
    return new RegExp(filterKeyword, 'i').test(`${filterKey ? l[filterKey] : l}`);
  });

  if (filteredList?.length === 0) {
    return <div className="flex items-center justify-center h-full">{empty}</div>;
  }

  return (
    <Virtuoso
      data={filteredList}
      className={cn(className)}
      itemContent={(idx, item) => {
        return (
          <ListComponent
            key={idx}
            className={cn(
              'flex w-full gap-2 cursor-default select-none items-center rounded-sm px-2 py-1.5 text-sm outline-none transition-colors hover:bg-accent hover:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50',
              itemClassName,
            )}
            onClick={() => onClickItem?.(item)}
          >
            {typeof itemChild === 'function' ? itemChild(item) : itemChild}
          </ListComponent>
        );
      }}
    />
  );
};
