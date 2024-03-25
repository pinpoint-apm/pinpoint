import { HelpCommunity, HelpDocument } from '../../components';

export interface HelpPageProps {}
export const HelpPage = () => {
  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold">Help</h3>
      </div>
      <div
        data-orientation="horizontal"
        role="none"
        className="shrink-0 bg-border h-[1px] w-full"
      ></div>
      <div className="space-y-2">
        <h4 className="font-semibold leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
          Document & Guide
        </h4>
        <HelpDocument />
      </div>
      <div className="space-y-2">
        <h4 className="font-semibold leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
          Community
        </h4>
        <HelpCommunity />
      </div>
    </div>
  );
};
