import { ServiceSettingTable } from '../../components/Config/serviceSetting';

export const ServiceSettingPage = () => {
  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold">Service Setting</h3>
      </div>
      <div
        data-orientation="horizontal"
        role="none"
        className="shrink-0 bg-border h-[1px] w-full"
      ></div>
      <ServiceSettingTable />
    </div>
  );
};
