import React from 'react';
import { TraceViewerData } from '@pinpoint-fe/ui/src/constants';
import { Separator } from '@radix-ui/react-dropdown-menu';
import { getColorByString } from '@pinpoint-fe/ui/src/lib/colors';
import { LuChevronFirst, LuChevronLast } from 'react-icons/lu';
import { cn } from '@pinpoint-fe/ui/src/lib';

export interface TimelineInfoProps {
  data?: TraceViewerData.TraceEvent[];
  selectedTrace?: TraceViewerData.TraceEvent;
  start?: number;
  onClose?: () => void;
}

export const TimelineInfo = ({ data = [], selectedTrace }: TimelineInfoProps) => {
  const [applicationNameSet, setApplicationNameSet] = React.useState<Set<string>>(new Set());
  const [collapsed, setCollapsed] = React.useState(false);

  React.useEffect(() => {
    if (data) {
      const newApplicationNameSet = new Set<string>();
      data?.forEach((item) => {
        if (item.name) {
          newApplicationNameSet.add(item?.args['Application Name']);
        }
      });
      setApplicationNameSet(newApplicationNameSet);
    }
  }, [data]);

  if (selectedTrace) {
    return;
  }

  return (
    <div className="border-l max-w-96">
      <div className="flex items-center h-12 p-2 text-sm font-semibold border-b relativ bg-secondary/50">
        <button
          className="flex items-center justify-center w-6 h-6 opacity-50 cursor-pointer hover:opacity-100 hover:font-semibold "
          onClick={() => setCollapsed(!collapsed)}
        >
          {collapsed ? <LuChevronFirst /> : <LuChevronLast />}
        </button>
      </div>
      <Separator />
      <div
        className={cn('overflow-auto h-[calc(100%-3.2rem)] p-2', {
          'w-10': collapsed,
        })}
      >
        {Array.from(applicationNameSet).map((name, index) => (
          <div key={index} className="flex items-center gap-2 w-max">
            <div
              className="w-2.5 h-2.5"
              style={{
                backgroundColor: getColorByString(name),
              }}
            ></div>
            <div className="text-xs">{name}</div>
          </div>
        ))}
      </div>
    </div>
  );
};
