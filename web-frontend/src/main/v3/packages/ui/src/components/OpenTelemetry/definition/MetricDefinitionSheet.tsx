import { useAtom } from 'jotai';
import { openMetricDefinitionAtom } from '@pinpoint-fe/atoms';
import { Sheet, SheetClose, SheetContent, SheetHeader, SheetTitle } from '../../ui/sheet';
import { Separator } from '../../ui/separator';
import { ScrollArea } from '../../ui/scroll-area';
import { MetricDefinitionForm } from './MetricDefinitionForm';
import { cn } from '../../../lib';
import { Cross2Icon } from '@radix-ui/react-icons';
import { PreviewChart } from '../charts';

export interface MetricDefinitionSheetProps {}

export const MetricDefinitionSheet = () => {
  const [open, setOpen] = useAtom(openMetricDefinitionAtom);
  const title = `Add Metric`; // TODO: Add or Update Metric
  return (
    <Sheet open={open} onOpenChange={setOpen}>
      <SheetContent
        className="flex flex-col w-full gap-0 p-0 px-0 md:max-w-full md:w-2/5 z-[5000]"
        overlayClassName="bg-transparent backdrop-blur-none"
        hideClose
      >
        <SheetHeader className="px-4 bg-secondary/50">
          <SheetTitle className="relative flex items-center justify-between h-16 gap-1">
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
        <ScrollArea>
          <PreviewChart />
          <Separator />
          <MetricDefinitionForm />
        </ScrollArea>
      </SheetContent>
    </Sheet>
  );
};
