import useSWRMutation from 'swr/mutation';
import { ConfigUserGroup, END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { getMutateFetcher } from './swrConfigs';

interface PostConfigUserGroupProps {
  onCompleteSubmit?: (userGroupName: string) => void;
  onError?: () => void;
}

export const usePostConfigUserGroup = ({ onCompleteSubmit, onError }: PostConfigUserGroupProps) => {
  const { trigger, isMutating } = useSWRMutation(
    END_POINTS.CONFIG_USER_GROUP,
    getMutateFetcher<ConfigUserGroup.Body>('POST'),
  );

  const onSubmit = async (userGroupInfo: ConfigUserGroup.Body) => {
    try {
      await trigger(userGroupInfo);

      onCompleteSubmit?.(userGroupInfo.id);
    } catch (e) {
      onError?.();
    }
  };

  return { isMutating, onSubmit };
};
