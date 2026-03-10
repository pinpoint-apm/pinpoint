import { useMutation, UseMutationOptions } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { parseResponseError } from './reactQueryHelper';

type deleteApplicationParams = {
  applicationName: string;
  password: string;
};

export const useDeleteApplication = (
  options?: UseMutationOptions<null, unknown, deleteApplicationParams, unknown>,
) => {
  const deleteApplication = async (params: deleteApplicationParams) => {
    const response = await fetch(
      `${END_POINTS.ADMIN_REMOVE_APPLICATION}?applicationName=${params?.applicationName}&password=${params?.password}`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      },
    );
    if (!response.ok) {
      await parseResponseError(response);
    }
    return null;
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
    const response = await fetch(
      `${END_POINTS.ADMIN_REMOVE_AGENT}?applicationName=${params?.applicationName}&agentId=${params?.agentId}&password=${params?.password}`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      },
    );
    if (!response.ok) {
      await parseResponseError(response);
    }
    return null;
  };

  return useMutation({
    mutationFn: deleteAgent,
    ...options,
  });
};
