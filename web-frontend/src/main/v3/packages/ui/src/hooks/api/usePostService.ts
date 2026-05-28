import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, ErrorResponse, PostService } from '@pinpoint-fe/ui/src/constants';

export const usePostService = (
  options?: UseMutationOptions<PostService.Response, ErrorResponse, PostService.Body, unknown>,
) => {
  const postService = async (body: PostService.Body): Promise<PostService.Response> => {
    const response = await fetch(END_POINTS.SERVICES, {
      method: 'POST',
      body: JSON.stringify(body),
      headers: {
        'Content-Type': 'application/json',
      },
    });
    const data = await response.json();

    if (!response.ok || data?.result !== 'SUCCESS') {
      throw data;
    }
    return data;
  };

  return useMutation({
    mutationFn: postService,
    ...options,
  });
};
