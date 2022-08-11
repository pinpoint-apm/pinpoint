import React, { ReactNode } from 'react';
import Link from 'next/link';

export interface NextLinkProps {
  path: string,
  children?: ReactNode,
  className?: string;
}

export const NextLink = ({
  path,
  children,
  className,
}: NextLinkProps) => {
  return (
    <Link href={path}>
      <a className={className}>
        {children}
      </a>
    </Link>
  )
};