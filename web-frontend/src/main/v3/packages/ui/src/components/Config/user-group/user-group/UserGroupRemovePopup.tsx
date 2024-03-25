import React from 'react';
import { useTranslation } from 'react-i18next';
import { RemovePopup } from '../../../Popup';

export interface UserGroupRemovePopupProps {
  popupTrigger: React.ReactNode;
  removeGroupName: string;
  onClickRemove?: (userGroupName: string) => void;
}

export const UserGroupRemovePopup = ({
  popupTrigger,
  removeGroupName,
  onClickRemove,
}: UserGroupRemovePopupProps) => {
  const { t } = useTranslation();

  return (
    <RemovePopup
      popupTrigger={popupTrigger}
      popupTitle={t('CONFIGURATION.USER_GROUP.USER_GROUP_REMOVE_TITLE')}
      popupDesc={t('CONFIGURATION.USER_GROUP.USER_GROUP_REMOVE_DESC')}
      popupContents={<div className="text-sm font-semibold">{removeGroupName}</div>}
      onClickRemove={() => onClickRemove?.(removeGroupName)}
    />
  );
};
