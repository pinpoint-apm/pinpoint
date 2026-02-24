import { useAtomValue } from 'jotai';
import { ErrorAnalysisPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

export default function ErrorAnalysis() {
  const configuration = useAtomValue(configurationAtom);
  return <ErrorAnalysisPage configuration={configuration} />;
}
