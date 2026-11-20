import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, ErrorResponse, PostService } from '@pinpoint-fe/ui/src/constants';
import { assertSuccessResult, requestJson } from './reactQueryHelper';

export const usePostService = (
  options?: UseMutationOptions<PostService.Response, ErrorResponse, PostService.Body, unknown>,
) => {
  const postService = async (body: PostService.Body): Promise<PostService.Response> =>
    assertSuccessResult(
      await requestJson<PostService.Response>(END_POINTS.SERVICES, {
        method: 'POST',
        body: JSON.stringify(body),
        headers: {
          'Content-Type': 'application/json',
        },
      }),
    );

  return useMutation({
    mutationFn: postService,
    ...options,
  });
};
