import React from 'react';
import { useTranslation } from 'react-i18next';
import { ConfigGroupMember } from '@pinpoint-fe/ui/constants';
import { RemovePopup } from '../../../Popup';

export interface GroupMemberRemovePopupProps {
  popupTrigger: React.ReactNode;
  removeMember: ConfigGroupMember.GroupMember;
  onClickRemove?: (memberId: string) => void;
}

export const GroupMemberRemovePopup = ({
  popupTrigger,
  removeMember,
  onClickRemove,
}: GroupMemberRemovePopupProps) => {
  const { t } = useTranslation();

  return (
    <RemovePopup
      popupTrigger={popupTrigger}
      popupTitle={t('CONFIGURATION.USER_GROUP.GROUP_MEMBER_REMOVE_TITLE')}
      popupDesc={t('CONFIGURATION.USER_GROUP.GROUP_MEMBER_REMOVE_DESC')}
      popupContents={
        <div className="text-sm font-semibold">
          {removeMember.name}{' '}
          <span className="text-muted-foreground">/ {removeMember.department}</span>
        </div>
      }
      onClickRemove={() => onClickRemove?.(removeMember.memberId)}
    />
  );
};
