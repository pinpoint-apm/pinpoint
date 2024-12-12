import { useMutation, UseMutationOptions } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/constants';

type deleteApplicationParams = {
  applicationName: string;
  password: string;
};

export const useDeleteApplication = (
  options?: UseMutationOptions<null, unknown, deleteApplicationParams, unknown>,
) => {
  const deleteApplication = async (params: deleteApplicationParams) => {
    try {
      const response = await fetch(
        `${END_POINTS.ADMIN_REMOVE_APPLICATION}?applicationName=${params?.applicationName}&password=${params?.password}`,
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        },
      );

      if (!response?.ok) {
        const errorData = await response.json();
        throw new Error(errorData?.message);
      }

      return null;
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
  password: string;
};

export const useDeleteAgent = (
  options?: UseMutationOptions<null, unknown, deleteAgentParams, unknown>,
) => {
  const deleteAgent = async (params: deleteAgentParams) => {
    try {
      const response = await fetch(
        `${END_POINTS.ADMIN_REMOVE_AGENT}?applicationName=${params?.applicationName}&agentId=${params?.agentId}&password=${params?.password}`,
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        },
      );

      if (!response?.ok) {
        const errorData = await response.json();
        throw new Error(errorData?.message);
      }

      return null;
    } catch (error) {
      throw error;
    }
  };

  return useMutation({
    mutationFn: deleteAgent,
    ...options,
  });
};
