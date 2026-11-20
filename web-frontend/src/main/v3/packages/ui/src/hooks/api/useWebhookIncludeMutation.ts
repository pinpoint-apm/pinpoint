import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, ErrorResponse, WebhookInclude } from '@pinpoint-fe/ui/src/constants';
import { assertSuccessResult, requestJson } from './reactQueryHelper';

type WebhookIncludeMutationVariable = {
  params: WebhookInclude.Parameters;
  method: 'POST' | 'PUT';
};

export const useWebhookIncludeMutation = (
  options?: UseMutationOptions<
    WebhookInclude.Response,
    ErrorResponse,
    WebhookIncludeMutationVariable,
    unknown
  >,
) => {
  const updateData = async ({ params, method }: WebhookIncludeMutationVariable) =>
    assertSuccessResult(
      await requestJson<WebhookInclude.Response>(`${END_POINTS.INCLUDE_WEBHOOK}`, {
        method,
        body: JSON.stringify(params),
        headers: {
          'Content-Type': 'application/json',
        },
      }),
    );

  return useMutation({
    mutationFn: updateData,
    ...options,
  });
};
