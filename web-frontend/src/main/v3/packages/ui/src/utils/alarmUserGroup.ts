import { Configuration } from '@pinpoint-fe/ui/src/constants';

export const getAlarmUserGroupQuery = (configuration?: Configuration) => {
  const userId = configuration?.userId;
  const memberCheck = configuration?.alarmChannelMemberCheck === true;
  return {
    params: memberCheck && userId ? { userId } : {},
    enabled: !!configuration && (!memberCheck || !!userId),
  };
};

/** A saved group the user is not a member of stays offered, so the alarm can be edited without losing it. */
export const withSavedUserGroup = (groupIds: string[], savedGroupId?: string) =>
  savedGroupId && !groupIds.includes(savedGroupId) ? [savedGroupId, ...groupIds] : groupIds;
