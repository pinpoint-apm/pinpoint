import * as SheetPrimitive from '@radix-ui/react-dialog';
import { Sheet, SheetClose, SheetContent, SheetHeader, SheetTitle } from '../../ui/sheet';
import { Separator } from '../../ui/separator';
import { MetricDefinitionForm } from './MetricDefinitionForm';
import { cn } from '../../../lib';
import { Cross2Icon } from '@radix-ui/react-icons';
import { PreviewChart } from '../charts';
import { OtlpMetricDefUserDefined } from '@pinpoint-fe/constants';

export interface MetricDefinitionSheetProps extends SheetPrimitive.DialogProps {
  metric?: OtlpMetricDefUserDefined.Metric;
  onCancel?: () => void;
}

export const MetricDefinitionSheet = ({
  metric,
  onCancel,
  ...props
}: MetricDefinitionSheetProps) => {
  const title = metric?.id ? `Edit ${metric.title}` : `Add Metric`; // TODO: Add or Update Metric

  return (
    <Sheet {...props}>
      <SheetContent
        className="flex flex-col w-full gap-0 p-0 px-0 sm:max-w-full md:w-2/5 md:min-w-160 z-[5000]"
        overlayClassName="bg-transparent backdrop-blur-none"
        hideClose
      >
        <SheetHeader className="px-4 bg-secondary/50">
          <SheetTitle className="relative flex items-center justify-between h-16 gap-1 font-medium">
            {title}
            <SheetClose
              className={cn({
                'absolute left-0 top-1 text-muted-foreground': false,
              })}
            >
              <Cross2Icon className="w-4 h-4" />
            </SheetClose>
          </SheetTitle>
        </SheetHeader>
        <Separator />
        <PreviewChart />
        <Separator />
        <MetricDefinitionForm
          metric={metric}
          onComplete={() => {
            onCancel?.();
          }}
          onClickCancel={() => {
            onCancel?.();
          }}
        />
      </SheetContent>
    </Sheet>
  );
};
