import React, { ReactNode } from 'react';
import Link, { LinkProps } from 'next/link';

export interface NextLinkProps extends LinkProps {
  children?: ReactNode,
  className?: string;
}

export const NextLink = ({
  children,
  className,
  ...props
}: NextLinkProps) => {
  return (
    <Link {...props}>
      <a className={className}>
        {children}
      </a>
    </Link>
  )
};