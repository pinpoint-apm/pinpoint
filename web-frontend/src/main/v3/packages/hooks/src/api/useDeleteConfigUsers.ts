import useSWRMutation from 'swr/mutation';
import { ConfigUsers, END_POINTS } from '@pinpoint-fe/constants';
import { getMutateFetcher } from './swrConfigs';

interface DeleteConfigUsersProps {
  onCompleteRemove?: () => void;
  onError?: () => void;
}

export const useDeleteConfigUsers = ({ onCompleteRemove, onError }: DeleteConfigUsersProps) => {
  const { trigger, isMutating } = useSWRMutation(
    END_POINTS.CONFIG_USERS,
    getMutateFetcher<ConfigUsers.RemoveBody>('DELETE'),
  );

  const onRemove = async (userId: string) => {
    try {
      await trigger({ userId });

      onCompleteRemove?.();
    } catch (e) {
      onError?.();
    }
  };

  return { isMutating, onRemove };
};
