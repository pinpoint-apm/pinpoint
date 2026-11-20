import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, Bind } from '@pinpoint-fe/ui/src/constants';
import { requestJson } from './reactQueryHelper';

export const usePostBind = (
  options?: UseMutationOptions<Bind.Response, unknown, FormData, unknown>,
) => {
  const postData = (formData: FormData) =>
    requestJson<Bind.Response>(`${END_POINTS.BIND}`, {
      method: 'POST',
      body: formData,
    });

  return useMutation({
    mutationFn: postData,
    ...options,
  });
};
