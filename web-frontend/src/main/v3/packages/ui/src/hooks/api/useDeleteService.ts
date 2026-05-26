import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { DeleteService, END_POINTS, ErrorResponse } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';

export const useDeleteService = (
  options?: UseMutationOptions<
    DeleteService.Response,
    ErrorResponse,
    DeleteService.Parameters,
    unknown
  >,
) => {
  const deleteService = async (
    params: DeleteService.Parameters,
  ): Promise<DeleteService.Response> => {
    const queryString = convertParamsToQueryString(params);
    const response = await fetch(`${END_POINTS.SERVICE}?${queryString}`, {
      method: 'DELETE',
    });
    const data = await response.json();

    if (!response.ok || data?.result !== 'SUCCESS') {
      throw data;
    }
    return data;
  };

  return useMutation({
    mutationFn: deleteService,
    ...options,
  });
};
