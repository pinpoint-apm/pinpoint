import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import { AlarmV2ChannelForm } from './AlarmV2ChannelForm';
import { useGetUserGroup } from '@pinpoint-fe/ui/src/hooks';
import { useAlarmV2ChannelMutation } from '@pinpoint-fe/ui/src/hooks/api';
import { useReactToastifyToast } from '@pinpoint-fe/ui/src/components/Toast';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { AlarmV2FullPage, AlarmV2ViewFallback } from './AlarmV2FullPage';
import { AlarmV2Channel } from '@pinpoint-fe/ui/src/constants/types';

/** The channel editor, filling the page. */
export interface AlarmV2ChannelFullPageProps {
  onClose: () => void;
  currentTargetChannel?: Partial<AlarmV2Channel.ChannelData>;
  channelsData?: AlarmV2Channel.ChannelData[];
  /**
   * The channel belongs to the service, and this does not name its owner. The server
   * asks for it to check the write permission.
   */
  applicationName: string;
}

export const AlarmV2ChannelFullPage = ({
  onClose,
  currentTargetChannel,
  channelsData,
  applicationName,
}: AlarmV2ChannelFullPageProps) => {
  const { t } = useTranslation();
  const toast = useReactToastifyToast();
  const queryClient = useQueryClient();
  // The view is up before the list arrives, and a link can name a channel that is
  // not there at all. The form that reads the groups draws only once one is found.
  const { data: userGroupList } = useGetUserGroup({}, { enabled: !!currentTargetChannel });
  const { mutate: saveChannel, isPending } = useAlarmV2ChannelMutation({
    onSuccess: () => {
      toast.success(
        currentTargetChannel?.id
          ? t('CONFIGURATION.ALARM_V2.CHANNEL_UPDATED')
          : t('COMMON.CREATE_SUCCESS'),
      );
      onClose();
      void queryClient.invalidateQueries({ queryKey: [END_POINTS.ALARM_V2_CHANNEL] });
    },
    onError: () => {
      toast.error(t('CONFIGURATION.ALARM_V2.CHANNEL_UPDATE_FAILED'));
    },
  });

  return (
    <AlarmV2FullPage
      onBack={onClose}
      title={
        <span>
          {currentTargetChannel?.id
            ? t('CONFIGURATION.ALARM_V2.EDIT_CHANNEL')
            : t('CONFIGURATION.ALARM_V2.ADD_CHANNEL')}
        </span>
      }
    >
      {currentTargetChannel ? (
        <AlarmV2ChannelForm
          key={currentTargetChannel.id ? `channel-${currentTargetChannel.id}` : 'channel-new'}
          data={currentTargetChannel}
          userGroups={userGroupList}
          pending={isPending}
          onCancel={onClose}
          onSubmit={(values) =>
            saveChannel(
              currentTargetChannel.id
                ? { method: 'PUT', id: currentTargetChannel.id, applicationName, params: values }
                : { method: 'POST', applicationName, params: values },
            )
          }
        />
      ) : (
        <AlarmV2ViewFallback loaded={!!channelsData} />
      )}
    </AlarmV2FullPage>
  );
};
