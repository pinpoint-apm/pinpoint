import { ApplicationType } from '@pinpoint-fe/constants';
import { Separator } from '../../ui';
import { cn } from '../../../lib';
import { SqlFilterProps, SqlFilter } from './SqlFilter';
import { SqlGroupByOptionProps, SqlGroupByOption } from './SqlGroupByOption';

export interface SqlSidebarProps {
  className?: string;
  application?: ApplicationType;
  // SqlFilter
  checkedFilters?: SqlFilterProps['checkedFilters'];
  onFilterChange?: SqlFilterProps['onChange'];
  // SqlGroupByOption
  selectedOption?: SqlGroupByOptionProps['selectedOption'];
  onGroupByChange?: SqlGroupByOptionProps['onChange'];
}

export const SqlSidebar = ({
  className,
  checkedFilters,
  onFilterChange,
  selectedOption,
  onGroupByChange,
}: SqlSidebarProps) => {
  return (
    <div className={cn('w-60 min-w-[15rem] border-r-1 h-full', className)}>
      <SqlGroupByOption selectedOption={selectedOption} onChange={onGroupByChange} />
      <Separator />
      <SqlFilter checkedFilters={checkedFilters} onChange={onFilterChange} />
    </div>
  );
};
