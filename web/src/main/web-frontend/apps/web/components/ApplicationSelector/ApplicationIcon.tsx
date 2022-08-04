import React from 'react';
import Image from 'next/image';
import AppIcon, { ApplicationIconProps as AppIconProps } from '@pinpoint-fe/common/components/Application/ApplicationIcon';
import { ApplicationType } from '@pinpoint-fe/common/components/Application/types';

export interface ApplicationIconProps extends AppIconProps {
  serviceType: ApplicationType['serviceType'];
}

export const ApplicationIcon = ({
  serviceType,
  ...props
}: ApplicationIconProps) => {
  return (
    <AppIcon
      as={Image}
      src={`/assets/img/icons/${serviceType}.png`} 
      {...props}
    />
  );
};
