import React from 'react';
import { Checkbox, Separator } from '../ui';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '../ui/collapsible';
import { RxChevronRight, RxChevronDown } from 'react-icons/rx';
import { cn } from '../../lib';

export interface CollapsibleFilterProps {
  title?: string;
  filterOptions?: { id: string; name: string }[];
  disabled?: boolean;
  className?: string;
  contentWrapperClassName?: string;
  checkedIds?: string[];
  onChange?: (checkedIds: string[]) => void;
}

export const CollapsibleFilter = ({
  title,
  filterOptions,
  disabled,
  className,
  contentWrapperClassName,
  checkedIds = [],
  onChange,
}: CollapsibleFilterProps) => {
  const [isOpen, setIsOpen] = React.useState(true);

  return (
    <Collapsible
      open={isOpen}
      onOpenChange={setIsOpen}
      className={cn('border border-input rounded-md shadow-sm', className)}
      disabled={disabled}
    >
      <CollapsibleTrigger
        className={'p-3 flex items-center gap-2 disabled:opacity-50 cursor-pointer'}
        asChild
      >
        <div>
          {isOpen ? <RxChevronDown /> : <RxChevronRight />}
          <div className="text-sm font-semibold">{title}</div>
        </div>
      </CollapsibleTrigger>
      {isOpen && <Separator />}
      {filterOptions?.length && filterOptions?.length > 0 && (
        <CollapsibleContent className={cn('py-2', contentWrapperClassName)}>
          {filterOptions.map((option) => {
            return (
              <div key={option.id} className="flex items-center gap-2 px-3 py-2 text-xs truncate">
                <Checkbox
                  id={option.id}
                  defaultChecked={checkedIds?.includes(option.id)}
                  onCheckedChange={(checked) => {
                    if (checked) {
                      onChange?.([...checkedIds, option.id]);
                    } else {
                      onChange?.(checkedIds.filter((id) => id !== option.id));
                    }
                  }}
                />
                <label htmlFor={option.id} className="truncate">
                  {option.name}
                </label>
              </div>
            );
          })}
        </CollapsibleContent>
      )}
    </Collapsible>
  );
};
