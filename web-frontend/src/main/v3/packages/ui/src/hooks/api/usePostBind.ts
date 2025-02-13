import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, Bind } from '@pinpoint-fe/ui/src/constants';

export const usePostBind = (
  options?: UseMutationOptions<Bind.Response, unknown, FormData, unknown>,
) => {
  const postData = async (formData: FormData) => {
    try {
      // Fetch를 사용하여 POST 요청을 보냅니다.
      const response = await fetch(`${END_POINTS.BIND}`, {
        method: 'POST',
        body: formData,
      });
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
