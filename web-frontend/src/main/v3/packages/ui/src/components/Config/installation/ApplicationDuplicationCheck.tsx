import React from 'react';
import { useTranslation } from 'react-i18next';
import { useSetAtom } from 'jotai';
import { installationApplicationNameAtom } from '@pinpoint-fe/ui/src/atoms';
import { useGetConfigApplicationDuplicationCheck } from '@pinpoint-fe/ui/src/hooks';
import { CheckStatus, DuplicationCheck, DUPLICATION_CHECK_STATUS } from './DuplicationCheck';

export interface ApplicationDuplicationCheckProps {}

export const ApplicationDuplicationCheck = () => {
  const setInstallationApplicationName = useSetAtom(installationApplicationNameAtom);
  const { t } = useTranslation();
  const [userApplicationName, setUserApplicationName] = React.useState('');
  const { data, error } = useGetConfigApplicationDuplicationCheck({
    applicationName: userApplicationName,
  });
  const handleOnCheck = (value: string) => {
    setUserApplicationName(value);
  };
  const checkStatus: CheckStatus = data
    ? { status: DUPLICATION_CHECK_STATUS.SUCCESS }
    : error
      ? { status: DUPLICATION_CHECK_STATUS.ERROR, message: error.message }
      : null;

  React.useEffect(() => {
    if (checkStatus?.status === DUPLICATION_CHECK_STATUS.SUCCESS) {
      setInstallationApplicationName(userApplicationName);
    }
  }, [checkStatus]);

  return (
    <DuplicationCheck
      placeholder={t('CONFIGURATION.INSTALLATION.APPLICATION_NAME_PLACEHOLDER')}
      guideText={t('CONFIGURATION.INSTALLATION.APPLICATION_NAME_DUPLICATION_CHECK')}
      checkStatus={checkStatus}
      onCheck={handleOnCheck}
    />
  );
};
