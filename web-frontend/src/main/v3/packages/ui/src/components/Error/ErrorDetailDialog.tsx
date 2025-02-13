import React from 'react';
import {
  Button,
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
  Dialog,
  DialogContent,
  DialogTrigger,
  Separator,
} from '../ui';
import * as PopoverPrimitive from '@radix-ui/react-popover';
import { ErrorDetailResponse } from '@pinpoint-fe/ui/src/constants';
import { cn } from '../../lib';
import { HighLightCode } from '../HighLightCode';
import { RxChevronDown, RxChevronUp } from 'react-icons/rx';

export interface ErrorDetailDialogProps {
  error: ErrorDetailResponse;
  contentOption?: PopoverPrimitive.PopoverContentProps;
  contentClassName?: string;
}

export const ErrorDetailDialog = ({
  error,
  contentOption,
  contentClassName,
}: ErrorDetailDialogProps) => {
  const [headerOpen, setHeaderOpen] = React.useState(false);
  const [paremetersOpen, setParametersOpen] = React.useState(false);
  const [stackTraceOpen, setStackTraceOpen] = React.useState(true);
  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button className="p-0 text-xs" variant="link" onClick={(e) => e.stopPropagation()}>
          Show details...
        </Button>
      </DialogTrigger>
      <DialogContent
        className={cn('max-h-[90%] overflow-auto max-w-5xl', contentClassName)}
        collisionPadding={16}
        onMouseDown={(e) => e.stopPropagation()}
        {...contentOption}
      >
        <div className="flex flex-col gap-4">
          <div className="space-y-2">
            <h4 className="flex items-center gap-1 font-medium">
              <div className="w-1 h-4 rounded-sm bg-status-fail" />
              Error Details
            </h4>
            <div className="flex items-center gap-1">
              <a
                className="text-sm font-semibold text-primary hover:underline"
                href={error?.url}
                target="_blank"
              >
                {error?.instance}
              </a>
            </div>
            <p className="text-sm break-all text-muted-foreground">{error?.message}</p>
          </div>
          <Separator />
          {error?.data && (
            <div className="grid gap-2 text-sm scrollbar-hide">
              <div className="grid grid-cols-[7rem_auto] gap-2">
                <div className="text-muted-foreground">Method</div>
                <div>{error.data.requestInfo?.method}</div>
              </div>
              <Collapsible className="space-y-2" open={headerOpen} onOpenChange={setHeaderOpen}>
                <CollapsibleTrigger
                  className="flex items-center gap-1 text-muted-foreground hover:text-foreground"
                  onClick={(e) => e.stopPropagation()}
                >
                  Header {headerOpen ? <RxChevronUp /> : <RxChevronDown />}
                </CollapsibleTrigger>
                <CollapsibleContent>
                  <div className="grid grid-cols-[7rem_auto] gap-2 text-xs p-2">
                    {error.data.requestInfo?.headers &&
                      Object.keys(error.data.requestInfo.headers)
                        .sort()
                        .map((key) => {
                          return (
                            <React.Fragment key={key}>
                              <div className="text-muted-foreground">{key}</div>
                              <div className="break-all">{error.data.requestInfo.headers[key]}</div>
                            </React.Fragment>
                          );
                        })}
                  </div>
                </CollapsibleContent>
              </Collapsible>
              <Collapsible
                className="space-y-2"
                open={paremetersOpen}
                onOpenChange={setParametersOpen}
              >
                <CollapsibleTrigger
                  className="flex items-center gap-1 text-muted-foreground hover:text-foreground"
                  onClick={(e) => e.stopPropagation()}
                >
                  Parameters {paremetersOpen ? <RxChevronUp /> : <RxChevronDown />}
                </CollapsibleTrigger>
                <CollapsibleContent>
                  <div className="grid grid-cols-[7rem_auto] gap-2 text-xs p-2">
                    {error.data.requestInfo?.parameters &&
                      Object.keys(error.data.requestInfo.parameters)
                        .sort()
                        .map((key) => {
                          return (
                            <React.Fragment key={key}>
                              <div className="text-muted-foreground">{key}</div>
                              <div className="break-all">
                                {error.data.requestInfo.parameters[key]}
                              </div>
                            </React.Fragment>
                          );
                        })}
                  </div>
                </CollapsibleContent>
              </Collapsible>
              <Collapsible
                className="space-y-2"
                open={stackTraceOpen}
                onOpenChange={setStackTraceOpen}
              >
                <CollapsibleTrigger
                  className="flex items-center gap-1 text-muted-foreground hover:text-foreground"
                  onClick={(e) => e.stopPropagation()}
                >
                  StackTrace {stackTraceOpen ? <RxChevronUp /> : <RxChevronDown />}
                </CollapsibleTrigger>
                <CollapsibleContent>
                  <HighLightCode language="java" code={error.trace} className="p-2 text-xs" />
                </CollapsibleContent>
              </Collapsible>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
};
