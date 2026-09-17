import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, ErrorResponse } from '@pinpoint-fe/ui/src/constants';
import { AlarmV2Rule } from '@pinpoint-fe/ui/src/constants/types';
import { parseResponseError } from '@pinpoint-fe/ui/src/hooks/api/reactQueryHelper';

type AlarmV2RuleMutationVariable =
  | { method: 'POST'; params: AlarmV2Rule.RuleSaveData }
  | { method: 'PUT'; id: number; params: AlarmV2Rule.RuleSaveData }
  | { method: 'DELETE'; id: number; applicationName: string; applicationType: string }
  | {
      method: 'PATCH';
      id: number;
      applicationName: string;
      applicationType: string;
      enabled: boolean;
    };

const appParams = ({
  applicationName,
  applicationType,
}: {
  applicationName: string;
  applicationType: string;
}) => new URLSearchParams({ applicationName, applicationType }).toString();

export const useAlarmV2RuleMutation = (
  options?: UseMutationOptions<
    AlarmV2Rule.RuleData | void,
    ErrorResponse,
    AlarmV2RuleMutationVariable,
    unknown
  >,
) => {
  const mutationFn = async (variable: AlarmV2RuleMutationVariable) => {
    let url = END_POINTS.ALARM_V2_RULE;
    let body: string | undefined;
    const method = variable.method;

    if (variable.method === 'POST') {
      body = JSON.stringify(variable.params);
    } else if (variable.method === 'PUT') {
      url = `${url}/${variable.id}`;
      body = JSON.stringify(variable.params);
    } else if (variable.method === 'DELETE') {
      url = `${url}/${variable.id}?${appParams(variable)}`;
    } else if (variable.method === 'PATCH') {
      url = `${url}/${variable.id}?${appParams(variable)}`;
      body = JSON.stringify({ enabled: variable.enabled });
    }

    const headers: Record<string, string> = {
      ...(body ? { 'Content-Type': 'application/json' } : {}),
    };

    const response = await fetch(url, {
      method,
      body,
      headers: Object.keys(headers).length > 0 ? headers : undefined,
    });

    if (!response.ok) {
      await parseResponseError(response);
    }

    if (variable.method === 'DELETE' || variable.method === 'PATCH' || variable.method === 'PUT')
      return;

    return response.json();
  };

  return useMutation({ mutationFn, ...options });
};
