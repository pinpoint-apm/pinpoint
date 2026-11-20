import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, TransactionMetaData } from '@pinpoint-fe/ui/src/constants';
import { requestJson } from './reactQueryHelper';

export const usePostTransactionMetaData = (
  options?: UseMutationOptions<TransactionMetaData.PostResponse, unknown, FormData, unknown>,
) => {
  const postData = async (formData: FormData) => {
    const params = new URLSearchParams();
    formData.forEach((value, key) => {
      if (typeof value === 'string') {
        params.append(key, value);
      } else {
        params.append(key, String(value));
      }
    });

    return requestJson<TransactionMetaData.PostResponse>(`${END_POINTS.TRANSACTION_META_DATA}`, {
      method: 'POST',
      body: params.toString(),
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });
  };

  return useMutation({
    mutationFn: postData,
    ...options,
  });
};
