import React from 'react';
import dynamic from 'next/dynamic';
import { useSWRConfig } from 'swr';

import { APPLICATION_LIST, APP_SETTING_KEYS } from '@pinpoint-fe/constants';
import ErrorBoundary from '../Error/ErrorBoundary';
import { ApplicationIcon } from './ApplicationIcon';
import { useLocalStorage } from '@pinpoint-fe/utils';
import {
  ApplicationSelector as AppSelector,
  ApplicationList,
  ItemClickHandlerType,
  ListItemSkeleton,
  ApplicationType,
} from '@pinpoint-fe/ui';
import { ApplicationFavoriteList } from './ApplicationFavoriteList';

const ApplicationListFetcher = dynamic(() =>
  import('./ApplicationListFetcher'),
  {
    ssr: false,
  }
);

export interface ApplicationSelectorProps {
  application?: ApplicationType | null;
  onClick?: ItemClickHandlerType;
}

export const ApplicationSelector = ({
  application,
  onClick,
}: ApplicationSelectorProps) => {
  const [filterKeyword, setFilterKeyword] = React.useState('');
  const { mutate } = useSWRConfig();
  const [favoriteList, setFavoriteList]
    = useLocalStorage<ApplicationType[]>(APP_SETTING_KEYS.FAVORLIITE_APPLICATION_LIST, []);

  function handleClickFavorite({ application }: Parameters<ItemClickHandlerType>[0]) {
    const isExist = favoriteList.find((favoriteApp: ApplicationType) => {
      return favoriteApp.applicationName === application.applicationName;
    });

    if (isExist) {
      setFavoriteList(
        favoriteList.filter((favoriteApp: ApplicationType) =>
          favoriteApp.applicationName !== application.applicationName
        )
      );
    } else {
      setFavoriteList([...favoriteList, application].sort((a, b) => a.applicationName.localeCompare(b.applicationName)));
    }
  }

  function handleReload() {
    mutate(APPLICATION_LIST);
  }

  function handleInputChange({ input }: { input: string }) {
    setFilterKeyword(input);
  }

  return (
    <AppSelector
      open={!application}
      selectedApplication={application && (
        <>
          <ApplicationIcon serviceType={application.serviceType} />
          {application.applicationName}
        </>
      )}
      onClickReload={handleReload}
      onChangeInput={handleInputChange}
    >
      <ApplicationList.Container title={'Favorite List'}>
        <ApplicationFavoriteList
          data={favoriteList}
          filterKeyword={filterKeyword}
          onClickFavorite={handleClickFavorite}
          onClick={onClick}
        />
      </ApplicationList.Container>
      <ApplicationList.Container title={'Application List'}>
        <ErrorBoundary fallback={'Something went wrong'}>
          <React.Suspense fallback={<ListItemSkeleton />}>
            <ApplicationListFetcher
              onClick={onClick}
              endPoint={APPLICATION_LIST}
              filterKeyword={filterKeyword}
              favoriteList={favoriteList}
              onClickFavorite={handleClickFavorite}
            />
          </React.Suspense>
        </ErrorBoundary>
      </ApplicationList.Container>
    </AppSelector>
  );
};

