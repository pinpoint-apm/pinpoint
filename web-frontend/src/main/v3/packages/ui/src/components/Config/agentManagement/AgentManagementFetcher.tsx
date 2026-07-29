import React from 'react';
import { useTranslation } from 'react-i18next';
import { ApplicationType, AgentManagementList } from '@pinpoint-fe/ui/src/constants';
import { Button, useReactToastifyToast } from '../../../components';
import { AgentManagementTable } from './AgentManagementTable';
import {
  useDeleteAgent,
  useDeleteApplication,
  useGetAgentManagementList,
} from '@pinpoint-fe/ui/src/hooks';
import { FaRegTrashCan } from 'react-icons/fa6';
import { AgentManagementRemovePopup } from './AgentManagementRemovePopup';

export interface AgentManagementFetcherProps {
  application?: ApplicationType;
  onRemoveApplicationSuccess?: () => void;
}

export const AgentManagementFetcher = ({
  application,
  onRemoveApplicationSuccess,
}: AgentManagementFetcherProps) => {
  const toast = useReactToastifyToast();

  const { t } = useTranslation();

  const { data, refetch } = useGetAgentManagementList({
    applicationName: application?.applicationName || '',
    serviceTypeName: application?.serviceType,
  });

  const onError = React.useCallback(() => {
    toast.error(t('COMMON.REMOVE_FAIL'), {
      autoClose: 2000,
    });
  }, []);

  const onSuccess = React.useCallback(() => {
    toast.success(t('COMMON.REMOVE_SUCCESS'), {
      autoClose: 2000,
    });
  }, []);

  const { mutate: deleteApplication } = useDeleteApplication({
    onError,
    onSuccess: () => {
      onSuccess();
      onRemoveApplicationSuccess?.();
    },
  });

  const { mutate: deleteAgent } = useDeleteAgent({
    onError,
    onSuccess: () => {
      onSuccess();
      refetch();
    },
  });

  function handleRemoveApplication(removeApplication?: ApplicationType, password: string = '') {
    deleteApplication({
      applicationName: removeApplication?.applicationName || '',
      serviceTypeName: removeApplication?.serviceType,
      password,
    });
  }

  function handleRemoveAgent(removeAgent?: AgentManagementList.Instance, password: string = '') {
    deleteAgent({
      applicationName: removeAgent?.applicationName || '',
      serviceTypeName: removeAgent?.serviceTypeName,
      agentId: removeAgent?.agentId || '',
      password,
    });
  }

  return (
    <div className="flex flex-col gap-2">
      <div className="text-end">
        <AgentManagementRemovePopup
          popupTrigger={
            <Button variant={'destructive'}>
              <FaRegTrashCan className="mr-0.5" />{' '}
              {t('CONFIGURATION.AGENT_MANAGEMENT.LABEL.REMOVE_APPLICATION')}
            </Button>
          }
          isApplication={true}
          application={application}
          onClickRemove={handleRemoveApplication}
        />
      </div>
      <AgentManagementTable data={data || []} onRemove={handleRemoveAgent} />
    </div>
  );
};
