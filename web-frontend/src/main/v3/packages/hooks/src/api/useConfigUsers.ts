import { useQuery, UseMutationOptions, useMutation } from '@tanstack/react-query';
import { ConfigUsers, END_POINTS, ErrorResponse } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams?: Partial<ConfigUsers.Parameters>) => {
  if (queryParams?.searchKey) {
    return `?${convertParamsToQueryString(queryParams)}`;
  }
  return '';
};

export const useGetConfigUsers = (params?: ConfigUsers.Parameters) => {
  const queryString = getQueryString(params);

  const { data, isLoading, refetch } = useQuery<ConfigUsers.Response>({
    queryKey: [END_POINTS.CONFIG_USERS, params],
    queryFn: queryFn(`${END_POINTS.CONFIG_USERS}${queryString}`),
    enabled: !!queryString,
  });
  return { data, isLoading, refetch };
};

export const usePostConfigUser = (
  options?: UseMutationOptions<ConfigUsers.Response, ErrorResponse, ConfigUsers.User, unknown>,
) => {
  const postData = async (formData: ConfigUsers.User) => {
    try {
      const response = await fetch(`${END_POINTS.CONFIG_USERS}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });
      if (!response?.ok) {
        const errorData = await response.json();
        throw new Error(errorData?.message);
      }

      const data = await response.json();
      return data;
    } catch (error) {
      throw error;
    }
  };

  return useMutation({
    mutationFn: postData,
    ...options,
  });
};

export const usePutConfigUser = (
  options?: UseMutationOptions<ConfigUsers.Response, ErrorResponse, ConfigUsers.User, unknown>,
) => {
  const puttData = async (formData: ConfigUsers.User) => {
    try {
      const response = await fetch(`${END_POINTS.CONFIG_USERS}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });
      if (!response?.ok) {
        const errorData = await response.json();
        throw new Error(errorData?.message);
      }

      const data = await response.json();
      return data;
    } catch (error) {
      throw error;
    }
  };

  return useMutation({
    mutationFn: puttData,
    ...options,
  });
};

export const useDeleteConfigUser = (
  options?: UseMutationOptions<ConfigUsers.Response, ErrorResponse, string, unknown>,
) => {
  const deleteData = async (userId: string) => {
    try {
      const response = await fetch(`${END_POINTS.CONFIG_USERS}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ userId }),
      });

      if (!response?.ok) {
        const errorData = await response.json();
        throw new Error(errorData?.message);
      }

      const data = await response.json();

      return data;
    } catch (error) {
      throw error;
    }
  };

  return useMutation({
    mutationFn: deleteData,
    ...options,
  });
};
