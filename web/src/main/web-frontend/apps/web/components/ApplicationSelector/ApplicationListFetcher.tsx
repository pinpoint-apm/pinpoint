import React from 'react';
import useSWR from 'swr'
import { useAtom } from 'jotai';

import {
  ApplicationType,
  ApplicationList,
  ItemProps,
  ItemClickHandlerType,
} from '@pinpoint-fe/ui';
import { ApplicationIcon } from './ApplicationIcon';

export interface ApplicationListFetcherProps extends Pick<ItemProps, 'onClickFavorite'> {
  endPoint: string;
  filterKeyword?: string;
  favoriteList?: ApplicationType[];
  onClick?: ItemClickHandlerType;
}

const ApplicationListFetcher = ({
  endPoint,
  filterKeyword,
  favoriteList,
  onClickFavorite,
  onClick,
}: ApplicationListFetcherProps) => {
  const { data = [] } = useSWR<ApplicationType[]>(`${endPoint}`);

  const handleClickList: ItemClickHandlerType = (application) => {
    onClick?.(application);
  }
  
  return (
    <ApplicationList.List
      {...{ data, filterKeyword }}
    >
      {(props) =>
        <ApplicationList.Item
          {...props}
          onClick={handleClickList}
          onClickFavorite={onClickFavorite}
          favoriteList={favoriteList}
          icon={(
            <ApplicationIcon
              serviceType={props.data[props.index].serviceType}
            />
          )}
        />
      }
    </ApplicationList.List>
  );
};

export default ApplicationListFetcher;
