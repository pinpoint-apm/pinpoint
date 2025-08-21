import { useQuery, useMutation } from '@tanstack/react-query';
import { END_POINTS, ConfigUserGroup } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString, isEmpty } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: ConfigUserGroup.Parameters) => {
  if (isEmpty(queryParams)) {
    return '';
  }

  return `?${convertParamsToQueryString(queryParams)}`;
};

export const useGetUserGroup = (
  params: ConfigUserGroup.Parameters,
  { enabled }: { enabled: boolean } = { enabled: true },
) => {
  const queryString = getQueryString(params);

  const { data, isLoading, isFetching, refetch } = useQuery<ConfigUserGroup.Response>({
    queryKey: [END_POINTS.CONFIG_USER_GROUP, params],
    queryFn: queryFn(`${END_POINTS.CONFIG_USER_GROUP}${queryString}`),
    enabled: !!enabled,
  });

  return { data, isLoading, isValidating: isFetching, refetch };
};

interface PostUserGroupProps {
  onCompleteSubmit?: (userGroupName: string) => void;
  onError?: () => void;
}

export const usePostUserGroup = ({ onCompleteSubmit, onError }: PostUserGroupProps) => {
  const { mutateAsync, isPending } = useMutation({
    mutationFn: async (userGroupInfo: ConfigUserGroup.Body) => {
      const response = await fetch(END_POINTS.CONFIG_USER_GROUP, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(userGroupInfo),
      });

      if (!response?.ok) {
        const errorData = await response.json();
        throw new Error(errorData?.message || 'Failed to post user group');
      }

      const data = await response.json();
      return data;
    },
  });

  const onSubmit = async (userGroupInfo: ConfigUserGroup.Body) => {
    try {
      await mutateAsync(userGroupInfo);

      onCompleteSubmit?.(userGroupInfo.id);
    } catch (e) {
      onError?.();
    }
  };

  return { isMutating: isPending, onSubmit };
};

interface DeleteUserGroupProps {
  onCompleteRemove?: () => void;
  onError?: () => void;
}

export const useDeleteUserGroup = ({ onCompleteRemove, onError }: DeleteUserGroupProps) => {
  const { mutateAsync, isPending } = useMutation({
    mutationFn: async (params: ConfigUserGroup.Body) => {
      const response = await fetch(END_POINTS.CONFIG_USER_GROUP, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(params),
      });

      if (!response?.ok) {
        const errorData = await response.json();
        throw new Error(errorData?.message || 'Failed to delete user group');
      }

      const data = await response.json();
      return data;
    },
  });

  const onRemove = async (params: ConfigUserGroup.Body) => {
    try {
      await mutateAsync(params);

      onCompleteRemove?.();
    } catch (e) {
      onError?.();
    }
  };

  return { isMutating: isPending, onRemove };
};
