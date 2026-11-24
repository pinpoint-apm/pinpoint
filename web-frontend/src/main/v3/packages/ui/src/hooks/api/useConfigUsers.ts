import { useQuery, UseMutationOptions, useMutation } from '@tanstack/react-query';
import { ConfigUsers, END_POINTS, ErrorResponse } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn, requestJson } from './reactQueryHelper';

const getQueryString = (queryParams?: Partial<ConfigUsers.Parameters>) => {
  if (queryParams?.searchKey) {
    return `?${convertParamsToQueryString(queryParams)}`;
  }
  return '';
};

export const useGetConfigUsers = (
  params?: ConfigUsers.Parameters,
  { throwOnError }: { throwOnError?: boolean } = {},
) => {
  const queryString = getQueryString(params);

  const { data, isLoading, refetch } = useQuery<ConfigUsers.Response>({
    queryKey: [END_POINTS.CONFIG_USERS, params],
    queryFn: queryFn(`${END_POINTS.CONFIG_USERS}${queryString}`),
    enabled: true,
    throwOnError,
  });
  return { data, isLoading, refetch };
};

export const usePostConfigUser = (
  options?: UseMutationOptions<ConfigUsers.Response, ErrorResponse, ConfigUsers.User, unknown>,
) => {
  const postData = (formData: ConfigUsers.User) =>
    requestJson<ConfigUsers.Response>(END_POINTS.CONFIG_USERS, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(formData),
    });

  return useMutation({
    mutationFn: postData,
    ...options,
  });
};

export const usePutConfigUser = (
  options?: UseMutationOptions<ConfigUsers.Response, ErrorResponse, ConfigUsers.User, unknown>,
) => {
  const putData = (formData: ConfigUsers.User) =>
    requestJson<ConfigUsers.Response>(END_POINTS.CONFIG_USERS, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(formData),
    });

  return useMutation({
    mutationFn: putData,
    ...options,
  });
};

export const useDeleteConfigUser = (
  options?: UseMutationOptions<ConfigUsers.Response, ErrorResponse, string, unknown>,
) => {
  const deleteData = (userId: string) =>
    requestJson<ConfigUsers.Response>(END_POINTS.CONFIG_USERS, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ userId }),
    });

  return useMutation({
    mutationFn: deleteData,
    ...options,
  });
};
