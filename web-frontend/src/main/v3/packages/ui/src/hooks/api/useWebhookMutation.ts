import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, Webhook, ErrorResponse } from '@pinpoint-fe/ui/src/constants';

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
  const updateData = async ({ params, method }: WebhookMutationVariable) => {
    try {
      const response = await fetch(`${END_POINTS.WEBHOOK}`, {
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
