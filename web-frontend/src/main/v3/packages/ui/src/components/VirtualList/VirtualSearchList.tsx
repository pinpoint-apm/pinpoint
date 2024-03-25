import React, { ReactElement } from 'react';
import { RxMagnifyingGlass } from 'react-icons/rx';
import { cn } from '../../lib/utils';
import { Input } from '../ui/input';

export interface VirtualSearchListProps {
  children?: React.ReactNode | (({ filterKeyword }: { filterKeyword: string }) => React.ReactNode);
  className?: string;
  inputClassName?: string;
  inputContainerClassName?: string;
  placeHolder?: string;
}

export const VirtualSearchList = ({
  children,
  className,
  inputClassName,
  inputContainerClassName,
  placeHolder = 'Input keyword...',
}: VirtualSearchListProps) => {
  const [filterKeyword, setFilterKeyword] = React.useState('');

  return (
    <div className={className}>
      <div className={cn('flex', inputContainerClassName)}>
        <div className="flex items-center pl-3 opacity-50">
          <RxMagnifyingGlass />
        </div>
        <Input
          className={cn(inputClassName)}
          onChange={(e) => setFilterKeyword(e.target.value)}
          placeholder={placeHolder}
        />
      </div>
      {typeof children === 'function'
        ? children({ filterKeyword })
        : React.Children.map(children, (child) =>
            React.cloneElement(child as ReactElement, { filterKeyword }),
          )}
    </div>
  );
};
