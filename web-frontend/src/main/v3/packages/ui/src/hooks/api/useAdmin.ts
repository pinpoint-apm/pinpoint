import { useMutation, UseMutationOptions } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { parseResponseError } from './reactQueryHelper';

type deleteApplicationParams = {
  applicationName: string;
  serviceTypeName?: string;
  serviceTypeCode?: number;
  password: string;
};

export const useDeleteApplication = (
  options?: UseMutationOptions<null, unknown, deleteApplicationParams, unknown>,
) => {
  const deleteApplication = async (params: deleteApplicationParams) => {
    const queryString = convertParamsToQueryString(params);
    const response = await fetch(`${END_POINTS.ADMIN_APPLICATIONS}?${queryString}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      },
    });
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
  serviceTypeName?: string;
  serviceTypeCode?: number;
  agentId: string;
  password: string;
};

export const useDeleteAgent = (
  options?: UseMutationOptions<null, unknown, deleteAgentParams, unknown>,
) => {
  const deleteAgent = async (params: deleteAgentParams) => {
    const queryString = convertParamsToQueryString(params);
    const response = await fetch(`${END_POINTS.ADMIN_AGENTS}?${queryString}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      },
    });
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
