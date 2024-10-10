import { END_POINTS, OtlpMetricData } from '@pinpoint-fe/constants';
import { UseMutationOptions, useMutation } from '@tanstack/react-query';

// It is an API to get metricData.(It should be GET request.)
// Due to the problem of the query parameters being too long, it is POST request to send query parameters in the body.

export const usePostOtlpMetricData = (
  options?: UseMutationOptions<OtlpMetricData.Response, unknown, OtlpMetricData.Body, unknown>,
) => {
  const postData = async (bodyData: OtlpMetricData.Body) => {
    try {
      const response = await fetch(`${END_POINTS.OTLP_METRIC_DATA}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(bodyData),
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
