import { ApplicationType } from '@pinpoint-fe/constants';
import { getServerIconPath } from '@pinpoint-fe/ui/utils';
import { cn } from '../../lib';
import React from 'react';

export interface ServerIconProps extends React.ImgHTMLAttributes<HTMLImageElement> {
  application: ApplicationType;
}

export const ServerIcon = ({ application, className, ...props }: ServerIconProps) => {
  return (
    <img
      height="auto"
      className={cn('w-5', className)}
      alt={'server icon'}
      src={getServerIconPath(application)}
      onError={(e) => {
        e.currentTarget.src = getServerIconPath({});
      }}
      {...props}
    />
  );
};
