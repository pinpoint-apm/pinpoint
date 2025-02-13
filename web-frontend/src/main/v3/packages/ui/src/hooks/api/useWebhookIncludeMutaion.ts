import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, ErrorResponse, WebhookInclude } from '@pinpoint-fe/ui/src/constants';

type WebhookIncludeMutationVariable = {
  params: WebhookInclude.Parameters;
  method: 'POST' | 'PUT';
};

export const useWebhookIncludeMutaion = (
  options?: UseMutationOptions<
    WebhookInclude.Response,
    ErrorResponse,
    WebhookIncludeMutationVariable,
    unknown
  >,
) => {
  const updateData = async ({ params, method }: WebhookIncludeMutationVariable) => {
    try {
      const response = await fetch(`${END_POINTS.INCLUDE_WEBHOOK}`, {
        method,
        body: JSON.stringify(params),
        headers: {
          'Content-Type': 'application/json',
        },
      });

      const data = await response.json();

      if (data?.result !== 'SUCCESS') {
        throw data;
      }
      return data;
    } catch (error) {
      throw error;
    }
  };

  return useMutation({
    mutationFn: updateData,
    ...options,
  });
};
