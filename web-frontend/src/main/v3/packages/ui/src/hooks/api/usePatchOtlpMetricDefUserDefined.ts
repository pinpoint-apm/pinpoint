import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, OtlpMetricDefUserDefined } from '@pinpoint-fe/ui/src/constants';
import { requestJson } from './reactQueryHelper';

export const usePatchOtlpMetricDefUserDefined = (
  options?: UseMutationOptions<
    OtlpMetricDefUserDefined.PatchResponse,
    unknown,
    OtlpMetricDefUserDefined.PatchParameters,
    unknown
  >,
) => {
  const patchData = (params: OtlpMetricDefUserDefined.PatchParameters) =>
    requestJson<OtlpMetricDefUserDefined.PatchResponse>(
      `${END_POINTS.OTLP_METRIC_DEF_USER_DEFINED}`,
      {
        method: 'PATCH',
        body: JSON.stringify(params),
        headers: {
          'Content-Type': 'application/json',
        },
      },
    );

  return useMutation({
    mutationFn: patchData,
    ...options,
  });
};
