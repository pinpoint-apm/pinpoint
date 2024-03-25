import useSWRMutation from 'swr/mutation';
import { ConfigUsers, END_POINTS } from '@pinpoint-fe/constants';
import { getMutateFetcher } from './swrConfigs';

interface PutConfigUsersProps {
  onCompleteSubmit?: () => void;
  onError?: () => void;
}

export const usePutConfigUsers = ({ onCompleteSubmit, onError }: PutConfigUsersProps) => {
  const { trigger, isMutating } = useSWRMutation(
    END_POINTS.CONFIG_USERS,
    getMutateFetcher<ConfigUsers.AddBody>('PUT'),
  );

  const onSubmit = async (userInfo: ConfigUsers.User) => {
    try {
      await trigger(userInfo);

      onCompleteSubmit?.();
    } catch (e) {
      onError?.();
    }
  };

  return { isMutating, onSubmit };
};
