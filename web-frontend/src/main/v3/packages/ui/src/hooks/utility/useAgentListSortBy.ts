import { useLocalStorage } from 'usehooks-ts';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';

export enum AGENT_LIST_SORT_BY {
  ID = 'AGENT_ID_ASC',
  NAME = 'AGENT_NAME_ASC',
  RECENT = 'RECENT',
}

export const useAgentListSortBy = (defaultSortBy: AGENT_LIST_SORT_BY = AGENT_LIST_SORT_BY.ID) => {
  return useLocalStorage<AGENT_LIST_SORT_BY>(APP_SETTING_KEYS.AGENT_LIST_SORT, defaultSortBy);
};
