import { useQuery, useMutation } from '@tanstack/react-query';
import { END_POINTS, ConfigGroupMember } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: ConfigGroupMember.Parameters) => {
  if (queryParams.userGroupId) {
    return `?${convertParamsToQueryString(queryParams)}`;
  }
  return '';
};

export const useGetUserGroupMember = (params: ConfigGroupMember.Parameters) => {
  const queryString = getQueryString(params);

  const { data, isLoading, isFetching, refetch } = useQuery<ConfigGroupMember.Response>({
    queryKey: [END_POINTS.CONFIG_GROUP_MEMBER, params],
    queryFn: queryFn(`${END_POINTS.CONFIG_GROUP_MEMBER}${queryString}`),
    enabled: !!params.userGroupId,
  });

  return { data, isLoading, isValidating: isFetching, refetch };
};

interface PostUserGroupMemberProps {
  onCompleteSubmit?: () => void;
  onError?: () => void;
}

export const usePostUserGroupMember = ({ onCompleteSubmit, onError }: PostUserGroupMemberProps) => {
  const { mutateAsync, isPending } = useMutation({
    mutationFn: async (params: ConfigGroupMember.Body) => {
      const response = await fetch(END_POINTS.CONFIG_GROUP_MEMBER, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(params),
      });

      if (!response?.ok) {
        const errorData = await response.json();
        throw new Error(errorData?.message || 'Failed to post user group member');
      }

      const data = await response.json();
      return data;
    },
  });

  const onSubmit = async (params: ConfigGroupMember.Body) => {
    try {
      await mutateAsync(params);

      onCompleteSubmit?.();
    } catch (e) {
      onError?.();
    }
  };

  return { isMutating: isPending, onSubmit };
};

interface DeleteUserGroupMemberProps {
  onCompleteRemove?: () => void;
  onError?: () => void;
}

export const useDeleteUserGroupMember = ({
  onCompleteRemove,
  onError,
}: DeleteUserGroupMemberProps) => {
  const { mutateAsync, isPending } = useMutation({
    mutationFn: async (params: ConfigGroupMember.Body) => {
      const response = await fetch(END_POINTS.CONFIG_GROUP_MEMBER, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(params),
      });

      if (!response?.ok) {
        const errorData = await response.json();
        throw new Error(errorData?.message || 'Failed to delete user group member');
      }

      const data = await response.json();
      return data;
    },
  });

  const onRemove = async (params: ConfigGroupMember.Body) => {
    try {
      await mutateAsync(params);

      onCompleteRemove?.();
    } catch (e) {
      onError?.();
    }
  };

  return { isMutating: isPending, onRemove };
};
