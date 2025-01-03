import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from '../ui';

import { AGENT_LIST_SORT, useAgentListSortBy } from '@pinpoint-fe/ui/hooks';

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
        setSortBy(value as AGENT_LIST_SORT);
      }}
    >
      <SelectTrigger className={triggerClassName}>
        <SelectValue placeholder="Select language" />
      </SelectTrigger>
      <SelectContent align={align}>
        <SelectGroup>
          <SelectLabel className="text-xs">Sort List</SelectLabel>
          {Object.keys(AGENT_LIST_SORT).map((key, i) => {
            return (
              <SelectItem
                key={i}
                className="text-xs"
                value={AGENT_LIST_SORT[key as keyof typeof AGENT_LIST_SORT]}
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
