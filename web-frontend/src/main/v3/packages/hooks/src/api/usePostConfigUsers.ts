import useSWRMutation from 'swr/mutation';
import { ConfigUsers, END_POINTS } from '@pinpoint-fe/constants';
import { getMutateFetcher } from './swrConfigs';

interface PostConfigUsersProps {
  onCompleteSubmit?: () => void;
  onError?: () => void;
}

export const usePostConfigUsers = ({ onCompleteSubmit, onError }: PostConfigUsersProps) => {
  const { trigger, isMutating } = useSWRMutation(
    END_POINTS.CONFIG_USERS,
    getMutateFetcher<ConfigUsers.AddBody>('POST'),
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
