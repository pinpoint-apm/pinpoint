import React, { ReactElement } from 'react';

import { cn } from '../../lib/utils';
import { Input } from '../ui/input';
import { VirtualSearchListProps } from '../VirtualList';

export interface ApplicationSearchListProps extends VirtualSearchListProps {
  children?: React.ReactNode | (({ filterKeyword }: { filterKeyword: string }) => React.ReactNode);
  className?: string;
  inputClassName?: string;
  inputPlaceHolder?: string;
}

export const ApplicationSearchList = ({
  children,
  className,
  inputClassName,
  inputPlaceHolder = 'Input application name',
}: ApplicationSearchListProps) => {
  const [filterKeyword, setFilterKeyword] = React.useState('');

  return (
    <div className={className}>
      <Input
        className={cn(inputClassName)}
        onChange={(e) => setFilterKeyword(e.target.value)}
        placeholder={inputPlaceHolder}
      />
      {typeof children === 'function'
        ? children({ filterKeyword })
        : React.Children.map(children, (child) =>
            React.cloneElement(child as ReactElement, { filterKeyword }),
          )}
    </div>
  );
};
