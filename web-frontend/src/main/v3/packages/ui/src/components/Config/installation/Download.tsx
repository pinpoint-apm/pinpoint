import { useGetConfigInstallationInfo } from '@pinpoint-fe/ui/hooks';
import { LuExternalLink } from 'react-icons/lu';

export interface DownloadProps {}

export const Download = () => {
  const { data } = useGetConfigInstallationInfo();
  return (
    <div className="text-sm">
      <a
        className="inline-block text-blue-500 hover:underline hover:text-blue-600"
        href={data?.message.downloadUrl}
      >
        <span className="flex items-center gap-0.5">
          Download Pinpoint <LuExternalLink />
        </span>
      </a>
    </div>
  );
};
