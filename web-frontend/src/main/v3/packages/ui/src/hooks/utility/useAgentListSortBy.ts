import { useLocalStorage } from 'usehooks-ts';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';
import { AGENT_LIST_SORT } from '../api';

export const useAgentListSortBy = (defaultSortBy: AGENT_LIST_SORT = AGENT_LIST_SORT.ID) => {
  return useLocalStorage<AGENT_LIST_SORT>(APP_SETTING_KEYS.AGENT_LIST_SORT, defaultSortBy);
};
