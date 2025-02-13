import { useGetConfigInstallationInfo } from '@pinpoint-fe/ui/src/hooks';
import { useAtomValue } from 'jotai';
import {
  installationAgentIdAtom,
  installationApplicationNameAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { Textarea } from '../../ui';
import { ClipboardCopyButton } from '../../Button';

export interface JvmArgumentInfoProps {}

export const JvmArgumentInfo = () => {
  const { data } = useGetConfigInstallationInfo();
  const applicationName = useAtomValue(installationApplicationNameAtom);
  const agentId = useAtomValue(installationAgentIdAtom);
  const textareaValue = data
    ? `${data.message.installationArgument}\n-Dpinpoint.applicationName=${applicationName}\n-Dpinpoint.agentId=${agentId}`
    : '';

  return (
    <div className="relative flex w-1/2 h-20">
      <Textarea
        className="w-full h-full py-3 text-xs resize-none focus-visible:ring-0"
        value={textareaValue}
        readOnly
      />
      <ClipboardCopyButton
        copyValue={textareaValue}
        containerClassName=""
        btnClassName="border-none shadow-none w-8 h-8 absolute right-0"
      />
    </div>
  );
};
