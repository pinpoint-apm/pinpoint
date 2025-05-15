import { useAtom } from 'jotai';
import { serverMapChartTypeAtom } from '@pinpoint-fe/ui/src/atoms';
import { PiChartScatterBold } from 'react-icons/pi';
import { AiOutlineTable } from 'react-icons/ai';
import { TooltipContent, TooltipProvider, Tooltip, TooltipTrigger, Button } from '../ui';
import * as TooltipPrimitive from '@radix-ui/react-tooltip';

export const ChartTypeButtons = () => {
  const [chartType, setChartType] = useAtom(serverMapChartTypeAtom);

  return (
    <div>
      <TooltipProvider delayDuration={0}>
        <Tooltip>
          <TooltipTrigger asChild>
            <Button
              size="icon"
              className="rounded-r-none"
              variant={chartType === 'heatmap' ? 'default' : 'outline'}
              onClick={() => {
                setChartType('heatmap');
              }}
            >
              <AiOutlineTable />
            </Button>
          </TooltipTrigger>
          <TooltipPrimitive.Portal>
            <TooltipContent className="z-[1500]">
              <p>Heatmap chart</p>
            </TooltipContent>
          </TooltipPrimitive.Portal>
        </Tooltip>
        <Tooltip>
          <TooltipTrigger asChild>
            <Button
              size="icon"
              className="rounded-l-none"
              variant={chartType === 'scatter' ? 'default' : 'outline'}
              onClick={() => {
                setChartType('scatter');
              }}
            >
              <PiChartScatterBold />
            </Button>
          </TooltipTrigger>
          <TooltipPrimitive.Portal>
            <TooltipContent className="z-[1500]">
              <p>Scatter chart</p>
            </TooltipContent>
          </TooltipPrimitive.Portal>
        </Tooltip>
      </TooltipProvider>
    </div>
  );
};
