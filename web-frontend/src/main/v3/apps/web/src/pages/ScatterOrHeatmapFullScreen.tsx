import { useAtomValue } from 'jotai';
import { ScatterOrHeatmapFullScreenPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

export default function ScatterOrHeatmapFullScreen() {
  const configuration = useAtomValue(configurationAtom) as
    | (Configuration & Record<string, unknown>)
    | undefined;
  return <ScatterOrHeatmapFullScreenPage configuration={configuration} />;
}
