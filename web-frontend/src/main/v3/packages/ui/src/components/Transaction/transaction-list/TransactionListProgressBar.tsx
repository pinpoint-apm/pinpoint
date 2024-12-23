import {
  ProgressBarWithControls,
  ProgressBarWithControlsProps,
} from '../../ProgressBar/ProgressBarWithControls';
import { useAtomValue } from 'jotai';
import { differenceInMinutes } from 'date-fns';
import { useTransactionSearchParameters } from '@pinpoint-fe/ui/hooks';
import { transactionListDatasAtom } from '@pinpoint-fe/ui/atoms';
import { cn } from '../../../lib';

export interface TransactionListProgressBarProps extends ProgressBarWithControlsProps {}

export const TransactionListProgressBar = ({
  className,
  ...props
}: TransactionListProgressBarProps) => {
  const { dateRange, withFilter } = useTransactionSearchParameters();
  const transactionListData = useAtomValue(transactionListDatasAtom);

  const getProgress = () => {
    if (withFilter && transactionListData) {
      return transactionListData?.complete
        ? dateRange.from.getTime()
        : transactionListData.metadata[transactionListData.metadata.length - 1]
            ?.collectorAcceptTime;
    } else {
      return transactionListData?.complete
        ? dateRange.from.getTime()
        : transactionListData?.resultFrom;
    }
  };

  return (
    <ProgressBarWithControls
      className={cn('w-full pt-4', className)}
      progress={getProgress()}
      range={[dateRange.to.getTime(), dateRange.from.getTime()]}
      tickCount={
        differenceInMinutes(dateRange.to, dateRange.from) < 4
          ? differenceInMinutes(dateRange.to, dateRange.from)
          : 4
      }
      {...props}
    >
      {({ isComplete, completeRenderer, resumeRenderer }) =>
        isComplete ? completeRenderer : resumeRenderer
      }
    </ProgressBarWithControls>
  );
};
