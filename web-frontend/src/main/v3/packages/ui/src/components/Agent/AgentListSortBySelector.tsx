import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from '../ui';

import { AGENT_LIST_SORT_BY, useAgentListSortBy } from '@pinpoint-fe/ui/src/hooks';

export interface AgentListSortBySelectorProps {
  align?: 'center' | 'end' | 'start' | undefined;
  triggerClassName?: string;
}

export const AgentListSortBySelector = ({
  align,
  triggerClassName,
}: AgentListSortBySelectorProps) => {
  const [sortBy, setSortBy] = useAgentListSortBy();
  return (
    <Select
      value={sortBy}
      onValueChange={(value) => {
        setSortBy(value as AGENT_LIST_SORT_BY);
      }}
    >
      <SelectTrigger className={triggerClassName}>
        <SelectValue placeholder="Select language" />
      </SelectTrigger>
      <SelectContent align={align}>
        <SelectGroup>
          <SelectLabel className="text-xs">Sort List</SelectLabel>
          {Object.keys(AGENT_LIST_SORT_BY).map((key, i) => {
            return (
              <SelectItem
                key={i}
                className="text-xs"
                value={AGENT_LIST_SORT_BY[key as keyof typeof AGENT_LIST_SORT_BY]}
              >
                {key}
              </SelectItem>
            );
          })}
        </SelectGroup>
      </SelectContent>
    </Select>
  );
};
