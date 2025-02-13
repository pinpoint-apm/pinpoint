import { END_POINTS, OtlpMetricData } from '@pinpoint-fe/ui/src/constants';
import { UseMutationOptions, useMutation } from '@tanstack/react-query';

// It is an API to get metricData.(It should be GET request.)
// Due to the problem of the query parameters being too long, it is POST request to send query parameters in the body.

export const usePostOtlpMetricData = (
  options?: UseMutationOptions<OtlpMetricData.Response, Error, OtlpMetricData.Body, unknown>,
) => {
  const postData = async (bodyData: OtlpMetricData.Body) => {
    const response = await fetch(`${END_POINTS.OTLP_METRIC_DATA}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(bodyData),
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(JSON.stringify(data)); // 에러 처리
    }
    return data;
  };

  return useMutation({
    mutationFn: postData,
    ...options,
  });
};
