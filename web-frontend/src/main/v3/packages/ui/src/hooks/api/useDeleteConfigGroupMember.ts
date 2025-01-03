import useSWRMutation from 'swr/mutation';
import { ConfigGroupMember, END_POINTS } from '@pinpoint-fe/ui/constants';
import { getMutateFetcher } from './swrConfigs';

interface DeleteConfigGroupMemberProps {
  onCompleteRemove?: () => void;
  onError?: () => void;
}

export const useDeleteConfigGroupMember = ({
  onCompleteRemove,
  onError,
}: DeleteConfigGroupMemberProps) => {
  const { trigger, isMutating } = useSWRMutation(
    END_POINTS.CONFIG_GROUP_MEMBER,
    getMutateFetcher<ConfigGroupMember.Body>('DELETE'),
  );

  const onRemove = async (params: ConfigGroupMember.Body) => {
    try {
      await trigger(params);

      onCompleteRemove?.();
    } catch (e) {
      onError?.();
    }
  };

  return { isMutating, onRemove };
};
