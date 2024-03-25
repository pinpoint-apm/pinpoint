import {
  Button,
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuCheckboxItem,
} from '../ui';

export interface DataTableCountOfRowsProps {
  counts?: number[];
  selectedCount?: number;
  triggerClassName?: string;
  onChange?: (count: number) => void;
}

export const DataTableCountOfRows = ({
  counts = [50, 100, 150, 200],
  selectedCount = 50,
  triggerClassName,
  onChange,
}: DataTableCountOfRowsProps) => {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild className={triggerClassName}>
        <Button variant="outline" size="sm" className="ml-auto h-8 flex">
          View {selectedCount}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-36">
        <DropdownMenuLabel>Count of rows</DropdownMenuLabel>
        <DropdownMenuSeparator />
        {counts.map((count, i) => {
          return (
            <DropdownMenuCheckboxItem
              key={i}
              checked={selectedCount === count}
              onCheckedChange={() => onChange?.(count)}
            >
              {count}
            </DropdownMenuCheckboxItem>
          );
        })}
      </DropdownMenuContent>
    </DropdownMenu>
  );
};
