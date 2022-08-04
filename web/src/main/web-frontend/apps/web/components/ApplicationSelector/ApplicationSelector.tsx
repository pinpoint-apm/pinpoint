import React, { useEffect } from 'react';
import dynamic from 'next/dynamic';
import { useSWRConfig } from 'swr';
import { useAtom } from 'jotai';

import AppSelector from '@pinpoint-fe/common/components/Application/ApplicationSelector';
import ApplicationList, { ItemClickHandlerType } from '@pinpoint-fe/common/components/Application/ApplicationList';
import ListItemSkeleton from '@pinpoint-fe/common/components/Application/ListItemSkeleton';
import { APPLICATION_LIST } from '@pinpoint-fe/common/config/api/endPoints';
import ErrorBoundary from '../Error/ErrorBoundary';
import { applicationAtom } from '../../atoms/application';
import { ApplicationIcon } from './ApplicationIcon';
import { useLocalStorage } from '@pinpoint-fe/common/hooks/localStorage';
import { ApplicationType } from '@pinpoint-fe/common/components/Application/types';
import APP_SETTING_KEYS from '@pinpoint-fe/common/config/localStorage/appSettingKeys';
import { ApplicationFavoriteList } from './ApplicationFavoriteList';

const ApplicationListFetcher = dynamic(() => 
  import('./ApplicationListFetcher'), 
  { 
    ssr: false,
  }
);

export interface ApplicationSelectorProps {

}

export const ApplicationSelector = ({

}: ApplicationSelectorProps) => {
  const [ filterKeyword, setFilterKeyword ] = React.useState('');
  const [ application ] = useAtom(applicationAtom);
  const { mutate } = useSWRConfig();
  const [ favoriteList, setFavoriteList ] 
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
  
  function handleReload () {
    mutate(APPLICATION_LIST);
  }

  function handleInputChange({ input }: { input: string }) {
    setFilterKeyword(input);
  }

  return (
    <AppSelector
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
        />
      </ApplicationList.Container>
      <ApplicationList.Container title={'Application List'}>
        <React.Suspense fallback={<ListItemSkeleton />}>
          <ErrorBoundary fallback={'Something went wrong'}>
            <ApplicationListFetcher
              endPoint={APPLICATION_LIST} 
              filterKeyword={filterKeyword}
              favoriteList={favoriteList}
              onClickFavorite={handleClickFavorite} 
            />
          </ErrorBoundary>
        </React.Suspense>
      </ApplicationList.Container>
    </AppSelector>
  );
};

