import React from 'react';
import { useTranslation } from 'react-i18next';
import { RemovePopup } from '../../Popup';
import { ConfigUsers } from '@pinpoint-fe/ui/constants';

export interface UserRemovePopupProps {
  popupTrigger: React.ReactNode;
  removeUser: ConfigUsers.User;
  onClickRemove?: (userGroupName: string) => void;
}

export const UserRemovePopup = ({
  popupTrigger,
  removeUser,
  onClickRemove,
}: UserRemovePopupProps) => {
  const { t } = useTranslation();

  return (
    <RemovePopup
      popupTrigger={popupTrigger}
      popupTitle={t('CONFIGURATION.USERS.USER_REMOVE_TITLE')}
      popupDesc={t('CONFIGURATION.USERS.USER_REMOVE_DESC')}
      popupContents={
        <div className="text-sm font-semibold">
          {removeUser.name} <span className="text-muted-foreground">/ {removeUser.department}</span>
        </div>
      }
      onClickRemove={() => onClickRemove?.(removeUser.userId)}
    />
  );
};
