import React from 'react';
import { useTranslation } from 'react-i18next';
import { Separator } from '@pinpoint-fe/ui/src/components/ui/separator';
import { Link } from 'react-router';
import { AlarmV2EmptyState } from './AlarmV2EmptyState';
import { DataTableSkeleton } from '@pinpoint-fe/ui/src/components/DataTable/DataTableSkeleton';

export const AlarmV2ViewFallback = ({ loaded }: { loaded: boolean }) => {
  const { t } = useTranslation();
  return loaded ? (
    <AlarmV2EmptyState>{t('COMMON.NO_DATA')}</AlarmV2EmptyState>
  ) : (
    <DataTableSkeleton hideRowBox />
  );
};
export const AlarmV2FullPage = ({
  title,
  meta,
  onBack,
  children,
}: {
  title: React.ReactNode;
  /** The page shows this content below the breadcrumb, at the left edge of the page. */
  meta?: React.ReactNode;
  onBack: () => void;
  children: React.ReactNode;
}) => {
  const { t } = useTranslation();
  return (
    <div className="space-y-6">
      <div className="space-y-3">
        <div className="flex items-start gap-2 text-lg font-semibold">
          {/* onBack navigates itself; without preventDefault the Link would navigate a
              second time and leave two history entries to step back through. */}
          <Link
            to=""
            className="text-muted-foreground hover:underline"
            onClick={(event) => {
              event.preventDefault();
              onBack();
            }}
          >
            {t('CONFIGURATION.ALARM_V2.NAME')}
          </Link>
          <span className="text-muted-foreground">›</span>
          {title}
        </div>
        {meta}
      </div>
      <Separator />
      {/* Below this width the two-column rows overlap; the body scrolls instead. */}
      <div className="overflow-x-auto">
        <div className="min-w-[28rem]">{children}</div>
      </div>
    </div>
  );
};
