import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, OtlpMetricDefUserDefined } from '@pinpoint-fe/constants';

export const usePatchOtlpMetricDefUserDefined = (
  options?: UseMutationOptions<
    OtlpMetricDefUserDefined.PatchResponse,
    unknown,
    OtlpMetricDefUserDefined.PatchParameters,
    unknown
  >,
) => {
  const patchData = async (params: OtlpMetricDefUserDefined.PatchParameters) => {
    try {
      const response = await fetch(`${END_POINTS.OTLP_METRIC_DEF_USER_DEFINED}`, {
        method: 'PATCH',
        body: JSON.stringify(params),
        headers: {
          'Content-Type': 'application/json',
        },
      });
      const data = await response.json();

      return data;
    } catch (error) {
      throw error;
    }
  };

  return useMutation({
    mutationFn: patchData,
    ...options,
  });
};
