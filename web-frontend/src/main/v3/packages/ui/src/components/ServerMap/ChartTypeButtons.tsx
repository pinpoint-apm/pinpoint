import { useAtom } from 'jotai';
import { Button } from '..';
import { serverMapChartTypeAtom } from '@pinpoint-fe/ui/src/atoms';
import { PiChartScatterBold } from 'react-icons/pi';
import { AiOutlineTable } from 'react-icons/ai';

export const ChartTypeButtons = () => {
  const [chartType, setChartType] = useAtom(serverMapChartTypeAtom);

  return (
    <div>
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
    </div>
  );
};
