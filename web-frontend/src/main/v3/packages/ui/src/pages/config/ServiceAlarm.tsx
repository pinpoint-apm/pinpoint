import { useAtomValue } from 'jotai';
import { selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';

export const ServiceAlarmPage = () => {
  const selectedService = useAtomValue(selectedServiceAtom);

  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold">Alarm ({selectedService})</h3>
      </div>
      <div
        data-orientation="horizontal"
        role="none"
        className="shrink-0 bg-border h-[1px] w-full"
      ></div>
    </div>
  );
};
