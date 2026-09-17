import { UseMutationOptions, useMutation } from '@tanstack/react-query';
import { END_POINTS, ErrorResponse } from '@pinpoint-fe/ui/src/constants';
import { AlarmV2Channel } from '@pinpoint-fe/ui/src/constants/types';
import { parseResponseError } from '@pinpoint-fe/ui/src/hooks/api/reactQueryHelper';

type AlarmV2ChannelMutationVariable =
  | {
      method: 'POST';
      applicationName: string;
      params: AlarmV2Channel.ChannelWriteData;
    }
  | {
      method: 'PUT';
      id: number;
      applicationName: string;
      params: AlarmV2Channel.ChannelWriteData;
    }
  | { method: 'DELETE'; id: number; applicationName: string }
  | {
      method: 'LINK';
      ruleId: number;
      channelId: number;
      applicationName: string;
      applicationType: string;
    }
  | {
      method: 'UNLINK';
      ruleId: number;
      channelId: number;
      applicationName: string;
      applicationType: string;
    }
  | {
      method: 'LINK_TEMPLATE';
      templateId: number;
      channelId: number;
      applicationName?: string;
    }
  | {
      method: 'UNLINK_TEMPLATE';
      templateId: number;
      channelId: number;
      applicationName?: string;
    };

const HTTP_METHOD_OF: Record<AlarmV2ChannelMutationVariable['method'], string> = {
  POST: 'POST',
  PUT: 'PUT',
  DELETE: 'DELETE',
  LINK: 'POST',
  UNLINK: 'DELETE',
  LINK_TEMPLATE: 'POST',
  UNLINK_TEMPLATE: 'DELETE',
};

const buildApplicationPermissionQuery = (applicationName: string) =>
  `applicationName=${encodeURIComponent(applicationName)}`;

export const useAlarmV2ChannelMutation = (
  options?: UseMutationOptions<
    AlarmV2Channel.ChannelData | void,
    ErrorResponse,
    AlarmV2ChannelMutationVariable,
    unknown
  >,
) => {
  const mutationFn = async (variable: AlarmV2ChannelMutationVariable) => {
    let url = END_POINTS.ALARM_V2_CHANNEL;
    const method = HTTP_METHOD_OF[variable.method];
    let body: string | undefined;

    if (variable.method === 'POST') {
      url = `${url}?${buildApplicationPermissionQuery(variable.applicationName)}`;
      body = JSON.stringify(variable.params);
    } else if (variable.method === 'PUT') {
      url = `${url}/${variable.id}?${buildApplicationPermissionQuery(variable.applicationName)}`;
      body = JSON.stringify(variable.params);
    } else if (variable.method === 'DELETE') {
      const query = buildApplicationPermissionQuery(variable.applicationName);
      url = `${url}/${variable.id}?${query}`;
    } else if (variable.method === 'LINK' || variable.method === 'UNLINK') {
      const query = new URLSearchParams({
        applicationName: variable.applicationName,
        applicationType: variable.applicationType,
      }).toString();
      url = `${url}/rule/${variable.ruleId}/${variable.channelId}?${query}`;
    } else if (variable.method === 'LINK_TEMPLATE' || variable.method === 'UNLINK_TEMPLATE') {
      const query = buildApplicationPermissionQuery(variable.applicationName ?? '');
      url = `${url}/template/${variable.templateId}/${variable.channelId}?${query}`;
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

    if (
      variable.method === 'PUT' ||
      variable.method === 'DELETE' ||
      variable.method === 'LINK' ||
      variable.method === 'UNLINK' ||
      variable.method === 'LINK_TEMPLATE' ||
      variable.method === 'UNLINK_TEMPLATE'
    )
      return;

    return response.json();
  };

  return useMutation({ mutationFn, ...options });
};
