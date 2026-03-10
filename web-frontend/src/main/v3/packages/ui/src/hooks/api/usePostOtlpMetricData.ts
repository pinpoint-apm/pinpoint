import { END_POINTS, ErrorResponse, OtlpMetricData } from '@pinpoint-fe/ui/src/constants';
import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { parseResponseError } from './reactQueryHelper';

// It is an API to get metricData.(It should be GET request.)
// Due to the problem of the query parameters being too long, it is POST request to send query parameters in the body.

export const usePostOtlpMetricData = (
  options?: UseMutationOptions<
    OtlpMetricData.Response,
    Error & ErrorResponse,
    OtlpMetricData.Body,
    unknown
  >,
) => {
  const postData = async (bodyData: OtlpMetricData.Body) => {
    const response = await fetch(`${END_POINTS.OTLP_METRIC_DATA}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(bodyData),
    });

    if (!response.ok) {
      await parseResponseError(response);
    }
    return response.json();
  };

  return useMutation({
    mutationFn: postData,
    ...options,
  });
};
