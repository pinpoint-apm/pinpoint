import React from 'react';
import { useSetAtom } from 'jotai';
import { useTranslation } from 'react-i18next';
import { installationAgentIdAtom } from '@pinpoint-fe/ui/src/atoms';
import { useGetConfigAgentDuplicationCheck } from '@pinpoint-fe/ui/src/hooks';
import { CheckStatus, DUPLICATION_CHECK_STATUS, DuplicationCheck } from './DuplicationCheck';

export interface AgentDuplicationCheckProps {}

export const AgentDuplicationCheck = () => {
  const setInstallationAgentId = useSetAtom(installationAgentIdAtom);
  const { t } = useTranslation();
  const [userAgentId, setUserAgentId] = React.useState('');
  const { data, error } = useGetConfigAgentDuplicationCheck({
    agentId: userAgentId,
  });
  const handleOnCheck = (value: string) => {
    setUserAgentId(value);
  };
  const checkStatus: CheckStatus = data
    ? { status: DUPLICATION_CHECK_STATUS.SUCCESS }
    : error
      ? { status: DUPLICATION_CHECK_STATUS.ERROR, message: error.message }
      : null;

  React.useEffect(() => {
    if (checkStatus?.status === DUPLICATION_CHECK_STATUS.SUCCESS) {
      setInstallationAgentId(userAgentId);
    }
  }, [checkStatus]);

  return (
    <DuplicationCheck
      placeholder={t('CONFIGURATION.INSTALLATION.AGENT_ID_PLACEHOLDER')}
      guideText={t('CONFIGURATION.INSTALLATION.AGENT_ID_DUPLICATION_CHECK')}
      checkStatus={checkStatus}
      onCheck={handleOnCheck}
    />
  );
};
