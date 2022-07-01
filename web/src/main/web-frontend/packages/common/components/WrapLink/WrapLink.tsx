import React, { ReactNode } from 'react';
import Link from 'next/link';

export interface WrapLinkProps {
  path: string,
  children?: ReactNode,
  className?: string;
}

export const WrapLink = ({
  path,
  children,
  className,
}: WrapLinkProps) => {
  return (
    <Link href={path}>
      <a className={className}>
        {children}
      </a>
    </Link>
  )
};