import React from 'react';
import { useTranslation } from 'react-i18next';
import { Configuration, ApplicationType, SearchApplication } from '@pinpoint-fe/ui/constants';
import { ApplicationCombinedList } from '../../../components/Application';
import { Button, ScrollArea, Separator, useReactToastifyToast } from '../../../components';
import { AgentManagementTable } from './AgentManagementTable';
import {
  useDeleteAgent,
  useDeleteApplication,
  useGetAgentsSearchApplication,
} from '@pinpoint-fe/ui/hooks';
import { FaRegTrashCan } from 'react-icons/fa6';
import { AgentManagementRemovePopup } from './AgentManagementRemovePopup';

export interface AgentManagementFetcherProps {
  configuration?: Configuration;
}

export const AgentManagementFetcher = ({ configuration }: AgentManagementFetcherProps) => {
  void configuration; // Not use configuration
  const toast = useReactToastifyToast();

  const { t } = useTranslation();
  const [application, setApplication] = React.useState<ApplicationType>();

  const { data, refetch } = useGetAgentsSearchApplication({
    application: application?.applicationName || '',
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
      setApplication(undefined);
    },
  });

  const { mutate: deleteAgent } = useDeleteAgent({
    onError,
    onSuccess: () => {
      onSuccess();
      refetch();
    },
  });

  const agentList = React.useMemo(() => {
    return data
      ?.map((group) => {
        return group?.instancesList;
      })
      ?.flat(1);
  }, [data]);

  function handleRemoveApplication(removeApplication?: ApplicationType) {
    deleteApplication({
      applicationName: removeApplication?.applicationName || '',
      password: '',
    });
  }

  function handleRemoveAgent(removeAgent?: SearchApplication.Instance) {
    console.log('removeAgent', removeAgent, 'application', application);
    deleteAgent({
      applicationName: removeAgent?.applicationName || '',
      agentId: removeAgent?.agentId || '',
      password: '',
    });
  }

  return (
    <ScrollArea>
      <div className="flex gap-10">
        <h3 className="text-lg font-semibold">Agent management</h3>
        <ApplicationCombinedList
          open={!application}
          selectedApplication={application}
          onClickApplication={(application) => setApplication(application)}
        />
      </div>
      <Separator className="my-6" />
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
        <AgentManagementTable data={agentList || []} onRemove={handleRemoveAgent} />
      </div>
    </ScrollArea>
  );
};
