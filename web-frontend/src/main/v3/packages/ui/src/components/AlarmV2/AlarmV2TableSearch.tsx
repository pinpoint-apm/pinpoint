import { Input } from '@pinpoint-fe/ui/src/components/ui/input';
import { RxMagnifyingGlass } from 'react-icons/rx';

export const AlarmV2TableSearch = ({
  value,
  placeholder,
  onChange,
}: {
  value: string;
  placeholder: string;
  onChange: (value: string) => void;
}) => {
  return (
    <div className="relative w-full sm:w-72">
      <RxMagnifyingGlass className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
      <Input
        value={value}
        aria-label={placeholder}
        placeholder={placeholder}
        className="pl-9"
        onChange={({ currentTarget }) => onChange(currentTarget.value)}
      />
    </div>
  );
};
