import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, ErrorResponse } from '@pinpoint-fe/ui/src/constants';
import { AlarmV2Template } from '@pinpoint-fe/ui/src/constants/types';
import { parseResponseError } from '@pinpoint-fe/ui/src/hooks/api/reactQueryHelper';

type TemplatePayload = Pick<AlarmV2Template.TemplateData, 'name' | 'description' | 'items'>;

type AlarmV2TemplateMutationVariable =
  | { method: 'POST'; applicationName?: string; params: TemplatePayload }
  | { method: 'PUT'; id: number; applicationName?: string; params: TemplatePayload }
  | { method: 'DELETE'; id: number; applicationName?: string };

const appParams = (applicationName?: string) =>
  `applicationName=${encodeURIComponent(applicationName ?? '')}`;

/**
 * Writes to the bundle itself. Applying a bundle to an application creates rules
 * rather than editing the bundle, and answers with those rules, so it lives in
 * {@link useAlarmV2TemplateApplyMutation} and this one keeps a single return type.
 */
export const useAlarmV2TemplateMutation = (
  options?: UseMutationOptions<
    AlarmV2Template.TemplateData | void,
    ErrorResponse,
    AlarmV2TemplateMutationVariable,
    unknown
  >,
) => {
  const mutationFn = async (variable: AlarmV2TemplateMutationVariable) => {
    let url = `${END_POINTS.ALARM_V2_TEMPLATE}?${appParams(variable.applicationName)}`;
    let body: string | undefined;

    if (variable.method !== 'POST') {
      url = `${END_POINTS.ALARM_V2_TEMPLATE}/${variable.id}?${appParams(variable.applicationName)}`;
    }
    if (variable.method !== 'DELETE') {
      body = JSON.stringify(variable.params);
    }

    const response = await fetch(url, {
      method: variable.method,
      body,
      headers: body ? { 'Content-Type': 'application/json' } : undefined,
    });

    if (!response.ok) {
      await parseResponseError(response);
    }

    // Only the create answers with a body; the caller needs the new id to link channels.
    if (variable.method !== 'POST') {
      return;
    }

    return response.json();
  };

  return useMutation({ mutationFn, ...options });
};
