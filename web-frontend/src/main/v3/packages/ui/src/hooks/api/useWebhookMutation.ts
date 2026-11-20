import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, Webhook, ErrorResponse } from '@pinpoint-fe/ui/src/constants';
import { assertSuccessResult, requestJson } from './reactQueryHelper';

type WebhookMutationVariable = {
  params: Webhook.PostParameters | Webhook.PutParameters | Webhook.DeleteParmeters;
  method: 'POST' | 'PUT' | 'DELETE';
};

export const useWebhookMutation = (
  options?: UseMutationOptions<
    Webhook.MutaionResponse,
    ErrorResponse,
    WebhookMutationVariable,
    unknown
  >,
) => {
  const updateData = async ({ params, method }: WebhookMutationVariable) =>
    assertSuccessResult(
      await requestJson<Webhook.MutaionResponse>(`${END_POINTS.WEBHOOK}`, {
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
