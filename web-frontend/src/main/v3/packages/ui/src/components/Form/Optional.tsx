import React from 'react';
import { cn } from '../../lib/utils';
import { useTranslation } from 'react-i18next';

export interface OptionalProps {
  children: React.ReactNode;
  className?: string;
  hide?: boolean;
}

export const Optional = ({ className, hide, children }: OptionalProps) => {
  const { t } = useTranslation();
  return (
    <>
      {children}
      <span className={cn('italic text-muted-foreground/70', { hidden: hide }, className)}>
        {' '}
        - {t('COMMON.OPTIONAL')}
      </span>
    </>
  );
};
