import { ApplicationType } from '@pinpoint-fe/constants';
import { getServerImagePath } from '@pinpoint-fe/utils';
import { cn } from '../../lib';
import React from 'react';

export interface ServerImageProps extends React.ImgHTMLAttributes<HTMLImageElement> {
  application: ApplicationType;
}

export const ServerImage = ({ application, className, ...props }: ServerImageProps) => {
  return (
    <img
      height="auto"
      className={cn('w-13', className)}
      alt={'server image'}
      src={getServerImagePath(application)}
      onError={(e) => {
        e.currentTarget.src = getServerImagePath({});
      }}
      {...props}
    />
  );
};
