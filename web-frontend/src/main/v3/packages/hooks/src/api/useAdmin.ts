import { useMutation, UseMutationOptions } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/constants';

type Response = {
  result: 'SUCCESS';
};

export const useDeleteApplication = (
  options?: UseMutationOptions<Response, unknown, string, unknown>,
) => {
  const deleteApplication = async (applicationName: string) => {
    try {
      const response = await fetch(
        `${END_POINTS.ADMIN_REMOVE_APPLICATION}?applicationName=${applicationName}`,
        {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json',
          },
        },
      );

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
    mutationFn: deleteApplication,
    ...options,
  });
};

type deleteAgentParams = {
  applicationName: string;
  agentId: string;
};

export const useDeleteAgent = (
  options?: UseMutationOptions<Response, unknown, deleteAgentParams, unknown>,
) => {
  const deleteAgent = async (params: deleteAgentParams) => {
    try {
      const response = await fetch(
        `${END_POINTS.ADMIN_REMOVE_AGENT}?applicationName=${params?.applicationName}&agentId=${params?.agentId}`,
        {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json',
          },
        },
      );

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
    mutationFn: deleteAgent,
    ...options,
  });
};
