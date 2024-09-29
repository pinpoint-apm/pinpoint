import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, TransactionMetaData } from '@pinpoint-fe/constants';

export const usePostTransactionMetaData = (
  options?: UseMutationOptions<TransactionMetaData.PostResponse, unknown, FormData, unknown>,
) => {
  const postData = async (formData: FormData) => {
    try {
      const response = await fetch(`${END_POINTS.TRANSACTION_META_DATA}?`, {
        method: 'POST',
        body: formData,
        // body: formData,
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
