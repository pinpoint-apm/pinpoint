import React from 'react';
import Image from 'next/image';
import { 
  ApplicationType, 
  ApplicationIcon as AppIcon, 
  ApplicationIconProps as AppIconProps
} from '@pinpoint-fe/ui';

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
