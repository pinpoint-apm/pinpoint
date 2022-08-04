import React from 'react';
import { useAtom } from 'jotai';

import ApplicationList, { ApplicationListProps, ItemClickHandlerType, ItemProps } from '@pinpoint-fe/common/components/Application/ApplicationList';
import { ApplicationType } from '@pinpoint-fe/common/components/Application/types';
import { useLocalStorage } from '@pinpoint-fe/common/hooks/localStorage';
import { applicationAtom } from '../../atoms/application';
import { ApplicationIcon } from './ApplicationIcon';
import APP_SETTING_KEYS from '@pinpoint-fe/common/config/localStorage/appSettingKeys';

export interface ApplicationFavoriteListProps 
  extends Pick<ApplicationListProps, 'data' | 'filterKeyword'>, Pick<ItemProps, 'onClickFavorite'> {}

export const ApplicationFavoriteList = ({
  data,
  filterKeyword,
  onClickFavorite
}: ApplicationFavoriteListProps) => {
  const [, setApplication ] = useAtom(applicationAtom)

  return (
    <ApplicationList.List 
      maxHeight={150}
      displayDataCount={3}
      {...{data, filterKeyword}}
    >
      {(props) => (
        <ApplicationList.Item
          {...props}
          onClick={(param) => setApplication(param.application)}
          onClickFavorite={onClickFavorite}
          favoriteList={data}
          icon={(
            <ApplicationIcon
              serviceType={props.data[props.index].serviceType} 
            />
          )}  
        />
      )}
    </ApplicationList.List>
  );
};
