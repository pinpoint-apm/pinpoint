import { APP_SETTING_KEYS, ApplicationType } from '@pinpoint-fe/ui/src/constants';
import { useLocalStorage } from '@pinpoint-fe/ui/src/hooks';
import {
  ApplicationCombinedListForCommon,
  ApplicationCombinedListForCommonProps,
} from './ApplicationCombinedListForCommon';

export interface ApplicationCombinedListProps extends ApplicationCombinedListForCommonProps {
  triggerClassName?: string;
}

export const ApplicationCombinedList = (props: ApplicationCombinedListProps) => {
  const [favoriteList, setFavoriteList] = useLocalStorage<ApplicationType[]>(
    APP_SETTING_KEYS.FAVORLIITE_APPLICATION_LIST,
    [],
  );

  const handleClickFavorite = (newFavoriteList: ApplicationType[]) => {
    setFavoriteList(newFavoriteList);
  };

  return (
    <ApplicationCombinedListForCommon
      favoriteList={favoriteList}
      onClickFavorite={handleClickFavorite}
      {...props}
    />
  );
};
