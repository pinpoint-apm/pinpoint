import { useTranslation } from 'react-i18next';
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

const SORT_BY_LABEL_KEYS: Record<AGENT_LIST_SORT_BY, string> = {
  [AGENT_LIST_SORT_BY.STARTTIME_DESC]: 'COMMON.SORT_STARTTIME_DESC',
  [AGENT_LIST_SORT_BY.STARTTIME_ASC]: 'COMMON.SORT_STARTTIME_ASC',
  [AGENT_LIST_SORT_BY.NAME_DESC]: 'COMMON.SORT_NAME_DESC',
  [AGENT_LIST_SORT_BY.NAME_ASC]: 'COMMON.SORT_NAME_ASC',
};

export interface AgentListSortBySelectorProps {
  align?: 'center' | 'end' | 'start' | undefined;
  triggerClassName?: string;
}

export const AgentListSortBySelector = ({
  align,
  triggerClassName,
}: AgentListSortBySelectorProps) => {
  const { t } = useTranslation();
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
          {Object.values(AGENT_LIST_SORT_BY).map((value) => {
            return (
              <SelectItem key={value} className="text-xs" value={value}>
                {t(SORT_BY_LABEL_KEYS[value])}
              </SelectItem>
            );
          })}
        </SelectGroup>
      </SelectContent>
    </Select>
  );
};
