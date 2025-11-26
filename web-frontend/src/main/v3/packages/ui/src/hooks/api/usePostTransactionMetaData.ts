import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, TransactionMetaData } from '@pinpoint-fe/ui/src/constants';

export const usePostTransactionMetaData = (
  options?: UseMutationOptions<TransactionMetaData.PostResponse, unknown, FormData, unknown>,
) => {
  const postData = async (formData: FormData) => {
    try {
      const params = new URLSearchParams();
      formData.forEach((value, key) => {
        if (typeof value === 'string') {
          params.append(key, value);
        } else {
          params.append(key, String(value));
        }
      });

      const response = await fetch(`${END_POINTS.TRANSACTION_META_DATA}`, {
        method: 'POST',
        body: params.toString(),
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
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
