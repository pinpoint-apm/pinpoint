import React from 'react';
import useSWR from 'swr'
import { useAtom } from 'jotai';

import { 
  ApplicationType,
  ApplicationList,
  ItemProps,
} from '@pinpoint-fe/ui';
import { applicationAtom } from '../../atoms/application';
import { ApplicationIcon } from './ApplicationIcon';

export interface ApplicationListFetcherProps extends Pick<ItemProps, 'onClickFavorite'> {
  endPoint: string;
  filterKeyword?: string;
  favoriteList?: ApplicationType[];
}

const ApplicationListFetcher = ({
  endPoint,
  filterKeyword,
  favoriteList,
  onClickFavorite,
}: ApplicationListFetcherProps) => {
  const [, setApplication ] = useAtom(applicationAtom)
  const { data = [] } = useSWR<ApplicationType[]>(`${endPoint}`);

  return (
    <ApplicationList.List
      {...{data, filterKeyword}}
    >
      {(props) => 
        <ApplicationList.Item
          {...props}
          onClick={(param) => setApplication(param.application)}
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
