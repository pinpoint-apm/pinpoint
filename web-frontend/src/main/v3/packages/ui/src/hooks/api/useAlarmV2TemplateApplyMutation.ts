import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, ErrorResponse } from '@pinpoint-fe/ui/src/constants';
import { AlarmV2Rule } from '@pinpoint-fe/ui/src/constants/types';
import { parseResponseError } from '@pinpoint-fe/ui/src/hooks/api/reactQueryHelper';

type AlarmV2TemplateApplyVariable = {
  /** Stamps one rule per bundle item onto the application, or removes them again. */
  method: 'APPLY' | 'UNAPPLY';
  id: number;
  applicationName: string;
  applicationType: string;
};

export const useAlarmV2TemplateApplyMutation = (
  options?: UseMutationOptions<
    AlarmV2Rule.Response | void,
    ErrorResponse,
    AlarmV2TemplateApplyVariable,
    unknown
  >,
) => {
  const mutationFn = async (variable: AlarmV2TemplateApplyVariable) => {
    const params = new URLSearchParams({
      applicationName: variable.applicationName,
      applicationType: variable.applicationType,
    });

    const response = await fetch(
      `${END_POINTS.ALARM_V2_TEMPLATE}/${variable.id}/applications?${params}`,
      { method: variable.method === 'APPLY' ? 'POST' : 'DELETE' },
    );

    if (!response.ok) {
      await parseResponseError(response);
    }

    // Applying answers with the rules it created; removing them answers with nothing.
    if (variable.method === 'UNAPPLY') {
      return;
    }

    return response.json();
  };

  return useMutation({ mutationFn, ...options });
};
