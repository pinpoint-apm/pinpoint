import { useEffect } from 'react';
import { useLocalStorage } from 'usehooks-ts';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';

export enum AGENT_LIST_SORT_BY {
  STARTTIME_DESC = 'STARTTIME_DESC', // 최근 순
  STARTTIME_ASC = 'STARTTIME_ASC', // 오래된 순
  NAME_DESC = 'NAME_DESC', // 이름 내림차순
  NAME_ASC = 'NAME_ASC', // 이름 오름차순
}

const VALID_SORT_BY_VALUES = Object.values(AGENT_LIST_SORT_BY);

export const useAgentListSortBy = (
  defaultSortBy: AGENT_LIST_SORT_BY = AGENT_LIST_SORT_BY.STARTTIME_DESC,
) => {
  const [sortBy, setSortBy] = useLocalStorage<AGENT_LIST_SORT_BY>(
    APP_SETTING_KEYS.AGENT_LIST_SORT,
    defaultSortBy,
  );

  const isValid = (VALID_SORT_BY_VALUES as string[]).includes(sortBy);

  useEffect(() => {
    if (!isValid) {
      setSortBy(defaultSortBy);
    }
  }, [isValid, defaultSortBy, setSortBy]);

  return [isValid ? sortBy : defaultSortBy, setSortBy] as const;
};
