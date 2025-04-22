import { useAtomValue } from 'jotai';
import { Button, useServerMapChartType } from '..';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { useLocalStorage } from '@pinpoint-fe/ui/src/hooks';
import { EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';
import { PiChartScatterBold } from 'react-icons/pi';
import { AiOutlineTable } from 'react-icons/ai';

export const ChartTypeButtons = () => {
  const configuration = useAtomValue(configurationAtom);
  const [enableHeatmap] = useLocalStorage(
    EXPERIMENTAL_CONFIG_KEYS.ENABLE_HEATMAP,
    !!configuration?.['experimental.enableHeatmap.value'],
  );

  const [chartType, setChartType] = useServerMapChartType();

  if (!enableHeatmap) {
    return null;
  }

  return (
    <div>
      <Button
        size="icon"
        className="rounded-r-none"
        variant={chartType === 'scatter' ? 'default' : 'outline'}
        onClick={() => {
          setChartType('scatter');
        }}
      >
        <PiChartScatterBold />
      </Button>
      <Button
        size="icon"
        className="rounded-l-none"
        variant={chartType === 'heatmap' ? 'default' : 'outline'}
        onClick={() => {
          setChartType('heatmap');
        }}
      >
        <AiOutlineTable />
      </Button>
    </div>
  );
};
