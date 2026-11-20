import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { DeleteService, END_POINTS, ErrorResponse } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { assertSuccessResult, requestJson } from './reactQueryHelper';

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
    return assertSuccessResult(
      await requestJson<DeleteService.Response>(`${END_POINTS.SERVICE}?${queryString}`, {
        method: 'DELETE',
      }),
    );
  };

  return useMutation({
    mutationFn: deleteService,
    ...options,
  });
};
