import useSWRMutation from 'swr/mutation';
import { ConfigUserGroup, END_POINTS } from '@pinpoint-fe/ui/constants';
import { getMutateFetcher } from './swrConfigs';

interface DeleteConfigUserGroupProps {
  onCompleteRemove?: () => void;
  onError?: () => void;
}

export const useDeleteConfigUserGroup = ({
  onCompleteRemove,
  onError,
}: DeleteConfigUserGroupProps) => {
  const { trigger, isMutating } = useSWRMutation(
    END_POINTS.CONFIG_USER_GROUP,
    getMutateFetcher<ConfigUserGroup.Body>('DELETE'),
  );

  const onRemove = async (params: ConfigUserGroup.Body) => {
    try {
      await trigger(params);

      onCompleteRemove?.();
    } catch (e) {
      onError?.();
    }
  };

  return { isMutating, onRemove };
};
