import React from 'react';
import { useTranslation } from 'react-i18next';
import { RemovePopup } from '../../Popup';
import { SearchApplication, ApplicationType } from '@pinpoint-fe/ui/constants';

export interface AgentManagementRemovePopupProps {
  isApplication?: boolean;
  popupTrigger: React.ReactNode;
  application?: ApplicationType;
  agent?: SearchApplication.Instance;
  onClickRemove?: (removeTarget?: SearchApplication.Instance | ApplicationType) => void;
}

export const AgentManagementRemovePopup = ({
  isApplication,
  popupTrigger,
  application,
  agent,
  onClickRemove,
}: AgentManagementRemovePopupProps) => {
  const { t } = useTranslation();

  return (
    <RemovePopup
      popupTrigger={popupTrigger}
      popupTitle={t(
        `CONFIGURATION.AGENT_MANAGEMENT.${
          isApplication ? 'REMOVE_APPLICATION_TITLE' : 'REMOVE_AGENT_TITLE'
        }`,
      )}
      popupDesc={t(
        `CONFIGURATION.AGENT_MANAGEMENT.${
          isApplication ? 'REMOVE_APPLICATION_DESC' : 'REMOVE_AGENT_DESC'
        }`,
      )}
      popupContents={
        <div className="text-sm font-semibold">
          {isApplication ? application?.applicationName : agent?.hostName}{' '}
          {!isApplication && <span className="text-muted-foreground">({agent?.agentId})</span>}
        </div>
      }
      onClickRemove={() => onClickRemove?.(isApplication ? application : agent)}
    />
  );
};
