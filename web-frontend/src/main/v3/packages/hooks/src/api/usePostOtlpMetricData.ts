import { END_POINTS, OtlpMetricData, OtlpMetricDefUserDefined } from '@pinpoint-fe/constants';
import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { useOpenTelemetrySearchParameters } from '../searchParameters';

export const usePostOtlpMetricData = (
  options?: UseMutationOptions<OtlpMetricData.Response, unknown, OtlpMetricData.Body, unknown>,
) => {
  const { application, dateRange } = useOpenTelemetrySearchParameters();

  const postData = async (bodyData: OtlpMetricData.Body) => {
    try {
      // Fetch를 사용하여 POST 요청을 보냅니다.
      const response = await fetch(`${END_POINTS.OTLP_METRIC_DATA}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...bodyData,
          from: dateRange.from.getTime(),
          to: dateRange.to.getTime(),
        }),
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
