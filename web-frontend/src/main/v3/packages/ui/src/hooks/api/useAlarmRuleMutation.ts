import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, AlarmRule, ErrorResponse } from '@pinpoint-fe/ui/src/constants';
import { assertSuccessResult, requestJson } from './reactQueryHelper';

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
  const updateData = async ({ params, method }: AlarmRuleMutationVariable) =>
    assertSuccessResult(
      await requestJson<AlarmRule.MutaionResponse>(`${END_POINTS.ALARM_RULE}`, {
        method,
        body: JSON.stringify(params),
        headers: {
          'Content-Type': 'application/json',
        },
      }),
    );

  return useMutation({
    mutationFn: updateData,
    ...options,
  });
};
