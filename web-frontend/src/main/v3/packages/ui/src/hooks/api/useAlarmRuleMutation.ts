import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, AlarmRule, ErrorResponse } from '@pinpoint-fe/constants';

type AlarmRuleMutationVariable = {
  params: AlarmRule.PostParameters | AlarmRule.PutParameters | AlarmRule.DeleteParmeters;
  method: 'POST' | 'PUT' | 'DELETE';
};

export const useAlarmRuleMutation = (
  options?: UseMutationOptions<
    AlarmRule.MutaionResponse,
    ErrorResponse,
    AlarmRuleMutationVariable,
    unknown
  >,
) => {
  const updateData = async ({ params, method }: AlarmRuleMutationVariable) => {
    try {
      const response = await fetch(`${END_POINTS.ALARM_RULE}`, {
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
