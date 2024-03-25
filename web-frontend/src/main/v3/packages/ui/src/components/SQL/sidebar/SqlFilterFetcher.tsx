import { useGetSqlStatFilterOptionsData } from '@pinpoint-fe/hooks';
import { CollapsibleFilter } from '../../CollapsibleFilter';
import { cn } from '../../../lib';

type CheckedFilterType = { [key: string]: string[] };
export interface SqlFilterFetcherProps {
  className?: string;
  emptyMessage?: React.ReactNode;
  checkedFilters?: CheckedFilterType;
  onChange?: (checkedFilters: CheckedFilterType) => void;
}

export const SqlFilterFetcher = ({
  className,
  emptyMessage = 'No Filters',
  checkedFilters,
  onChange,
}: SqlFilterFetcherProps) => {
  const { data } = useGetSqlStatFilterOptionsData();

  return (
    <div className={cn('p-5', className)}>
      <div className="font-semibold mb-3 text-sm">Filter</div>
      <div className="flex flex-col gap-4">
        {data && data.length > 0 ? (
          data.map((filter, i) => {
            const filterOptions = filter.options.map((option) => ({
              id: option,
              name: option,
            }));

            return (
              <CollapsibleFilter
                key={i}
                title={filter.groupName}
                contentWrapperClassName="max-h-[24rem] overflow-y-auto"
                filterOptions={filterOptions}
                checkedIds={checkedFilters?.[filter.groupName] || []}
                onChange={(ids) => {
                  onChange?.({
                    ...checkedFilters,
                    [filter.groupName]: ids,
                  });
                }}
              />
            );
          })
        ) : (
          <div className="flex justify-center opacity-50 text-sm">{emptyMessage}</div>
        )}
      </div>
    </div>
  );
};
