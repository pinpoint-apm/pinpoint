import { useGetSqlStatGroupsData } from '@pinpoint-fe/hooks';
import { RadioGroup, RadioGroupItem } from '../../ui';
import { cn } from '../../../lib';

export interface SqlGroupByOptionFetcherProps {
  className?: string;
  selectedOption?: string;
  emptyMessage?: React.ReactNode;
  onChange?: (value: string) => void;
}

export const SqlGroupByOptionFetcher = ({
  className,
  selectedOption = 'query',
  emptyMessage = 'No Options',
  onChange,
}: SqlGroupByOptionFetcherProps) => {
  const { data } = useGetSqlStatGroupsData();
  const options = data && data.length > 0 ? [...data] : [];

  return (
    <div className={cn('p-5 pb-6', className)}>
      <div className="font-semibold mb-3 text-sm">Group By</div>
      <RadioGroup
        defaultValue={selectedOption ? selectedOption : 'query'}
        onValueChange={(value) => {
          onChange?.(value === 'none' ? '' : value);
        }}
      >
        {data && data.length > 0 ? (
          options.map((option) => {
            return (
              <div key={option} className="flex items-center space-x-2 text-xs truncate">
                <RadioGroupItem id={option} value={option} />
                <label className="truncate" htmlFor={option}>
                  {option}
                </label>
              </div>
            );
          })
        ) : (
          <div className="flex justify-center opacity-50">{emptyMessage}</div>
        )}
      </RadioGroup>
    </div>
  );
};
