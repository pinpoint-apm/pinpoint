import React from 'react';

export interface ApplicationIconProps extends React.ImgHTMLAttributes<HTMLImageElement> {
  as?: React.ElementType,
}

const ApplicationIcon = ({
  as: Component = 'img',
  ...props
}: ApplicationIconProps) => {
  return (
    <Component
      width={23}
      height={18}
      alt={'application image'}
      {...props}
    />
  );
};

export default ApplicationIcon;