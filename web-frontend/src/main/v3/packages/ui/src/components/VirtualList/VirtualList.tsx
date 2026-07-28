import React from 'react';
import { Virtuoso, VirtuosoHandle } from 'react-virtuoso';
import Fuse from 'fuse.js';
import { cn } from '../../lib/utils';

export interface VirtualListProps<T> {
  list?: T[];
  className?: string;
  itemClassName?: string;
  itemChild?: React.ReactNode | ((item: T) => React.ReactNode);
  empty?: React.ReactNode;
  filterKey?: keyof T;
  filterKeyword?: string;
  focusIndex?: number; // -1일 경우 mouse hover style이 동작하지 않음, undefined일 경우 mouse hover 기본 동작, 숫자일 경우 해당 인덱스로 포커스 됨
  getFilteredList?: (filteredList: T[]) => void;
  itemAs?: React.ElementType;
  onClickItem?: (item: T) => void;
  onMouseEnter?: (idx: number, item: T) => void;
}

export const VirtualList = <T,>({
  list,
  className,
  itemClassName,
  itemChild,
  empty = "We couldn't find anything.",
  filterKey,
  filterKeyword = '',
  focusIndex,
  getFilteredList,
  onClickItem,
  onMouseEnter,
  itemAs: ListComponent = 'div',
}: VirtualListProps<T>) => {
  const virtuosoRef = React.useRef<VirtuosoHandle>(null);
  const isFirstFocusEffectRef = React.useRef(true);

  const fuzzySearch = React.useMemo(() => {
    return new Fuse(list || [], {
      keys: filterKey ? [filterKey as string] : [],
      threshold: 0.3,
    });
  }, [filterKey, list]);

  const filteredList = React.useMemo(() => {
    return filterKeyword ? fuzzySearch.search(filterKeyword).map(({ item }) => item) : list;
  }, [filterKeyword, fuzzySearch, list]);

  React.useEffect(() => {
    getFilteredList?.(filteredList || []);
  }, [filteredList]);

  React.useEffect(() => {
    const isFirst = isFirstFocusEffectRef.current;
    if (isFirst) {
      isFirstFocusEffectRef.current = false;
    }

    if (focusIndex === undefined) {
      return;
    }

    // focusIndex가 음수(-1)이면 이 리스트는 키보드 포커스 대상이 아니라는 의미이므로
    // 자동 스크롤하지 않는다. (다른 리스트가 포커스됐을 때 이 리스트가 맨 위로 튕기는 것을 방지)
    if (focusIndex < 0) {
      return;
    }

    if (isFirst && focusIndex === 0) {
      // 최초 focusIndex가 0인 경우에만 스크롤을 생략
      return;
    }

    virtuosoRef.current?.scrollToIndex({
      index: focusIndex,
      align: 'end', // "start", "end", "center" 가능
      behavior: 'smooth',
    });
  }, [focusIndex]);

  if (filteredList?.length === 0) {
    return <div className="flex items-center justify-center h-full">{empty}</div>;
  }

  return (
    <Virtuoso
      ref={virtuosoRef}
      data={filteredList}
      className={cn(className)}
      itemContent={(idx, item) => {
        return (
          <ListComponent
            key={idx}
            className={cn(
              'flex w-full gap-2 cursor-default select-none items-center rounded-sm px-2 py-1.5 text-sm outline-none transition-colors data-[disabled]:pointer-events-none data-[disabled]:opacity-50',
              {
                'hover:bg-accent hover:text-accent-foreground': focusIndex === undefined,
                'bg-accent text-accent-foreground': focusIndex === idx,
              },
              itemClassName,
            )}
            onMouseEnter={() => {
              onMouseEnter?.(idx, item);
            }}
            onClick={() => onClickItem?.(item)}
          >
            {typeof itemChild === 'function' ? itemChild(item) : itemChild}
          </ListComponent>
        );
      }}
    />
  );
};
