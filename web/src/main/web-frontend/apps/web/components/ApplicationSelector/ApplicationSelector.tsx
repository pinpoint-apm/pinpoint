import React from 'react';
import useSWR from 'swr'

import AppSelector from '@pinpoint-fe/common/components/ApplicationSelector/ApplicationSelector';
import Image from 'next/image';

export interface ApplicationSelectorProps {

}

export const ApplicationSelector = ({

}: ApplicationSelectorProps) => {
  const fetcher = (...args: any) => fetch(args as any).then(res => res.json())
  const { data, error } = useSWR(
    '/api/applications.pinpoint',
    fetcher,
  );
  
  return (
    <AppSelector>
      <AppSelector.List title={'Favorite List'}>

      </AppSelector.List>
      <AppSelector.List title={'Application List'}>
      {data?.map((app: any, i: number) => (
        <AppSelector.Item 
          key={i}
          icon={(
            <Image
              src={`/assets/img/icons/${app.serviceType}.png`} 
              width={23}
              height={18}
              alt={'application image'}
            />
          )}
          application={app} 
        />
      ))}
      </AppSelector.List>
    </AppSelector>
  );
};
