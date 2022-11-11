import React from 'react';
import {
  ApplicationListProps,
  ItemClickHandlerType,
  ItemProps,
  ApplicationList,
} from '@pinpoint-fe/ui';
import { ApplicationIcon } from './ApplicationIcon';

export interface ApplicationFavoriteListProps
  extends Pick<ApplicationListProps, 'data' | 'filterKeyword'>, Pick<ItemProps, 'onClickFavorite'> { 
    onClick?: ItemClickHandlerType,
  }

export const ApplicationFavoriteList = ({
  data,
  filterKeyword,
  onClickFavorite,
  onClick
}: ApplicationFavoriteListProps) => {

  const handleClickList: ItemClickHandlerType = (application) => {
    onClick?.(application);
  }

  return (
    <ApplicationList.List
      maxHeight={150}
      displayDataCount={3}
      {...{ data, filterKeyword }}
    >
      {(props) => (
        <ApplicationList.Item
          {...props}
          onClick={handleClickList}
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
