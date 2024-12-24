import useSWRMutation from 'swr/mutation';
import { ConfigGroupMember, END_POINTS } from '@pinpoint-fe/ui/constants';
import { getMutateFetcher } from './swrConfigs';

interface PostConfigGroupMemberProps {
  onCompleteSubmit?: () => void;
  onError?: () => void;
}

export const usePostConfigGroupMember = ({
  onCompleteSubmit,
  onError,
}: PostConfigGroupMemberProps) => {
  const { trigger, isMutating } = useSWRMutation(
    END_POINTS.CONFIG_GROUP_MEMBER,
    getMutateFetcher<ConfigGroupMember.Body>('POST'),
  );

  const onSubmit = async (params: ConfigGroupMember.Body) => {
    try {
      await trigger(params);

      onCompleteSubmit?.();
    } catch (e) {
      onError?.();
    }
  };

  return { isMutating, onSubmit };
};
