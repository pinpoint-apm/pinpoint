import React from 'react';
import { useAtom } from 'jotai';

import {
  ApplicationType, 
  ApplicationListProps, 
  ItemClickHandlerType, 
  ItemProps, 
  ApplicationList,
} from '@pinpoint-fe/ui';
import { ApplicationIcon } from './ApplicationIcon';
import { applicationAtom } from '../../atoms/application';

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
