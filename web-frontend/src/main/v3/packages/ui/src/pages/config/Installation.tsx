import React from 'react';

type InstallationItem = {
  label: string;
  renderer: React.ReactNode;
};
export interface InstallationPageProps {
  installationItemList: InstallationItem[];
}

export const InstallationPage = ({ installationItemList }: InstallationPageProps) => {
  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold">Installation</h3>
      </div>
      <div
        data-orientation="horizontal"
        role="none"
        className="shrink-0 bg-border h-[1px] w-full"
      ></div>
      {installationItemList.map(({ label, renderer }, i) => (
        <div className="space-y-2" key={i}>
          <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
            {label}
          </label>
          {renderer}
        </div>
      ))}
    </div>
  );
};
