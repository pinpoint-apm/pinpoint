import { useTranslation } from 'react-i18next';
import { MdOutlineWarningAmber } from 'react-icons/md';
import { AlarmV2Channel } from '@pinpoint-fe/ui/src/constants/types';
import { hasNoRecipient } from './hasNoRecipient';

/**
 * The mark the channel lists carry. Icon only: the lists repeat it per row, and the sentence
 * that says what to do about it belongs in the channel editor, where it can be acted on.
 */
export const AlarmV2NoRecipientIcon = ({
  channel,
}: {
  channel?: Partial<AlarmV2Channel.ChannelData>;
}) => {
  const { t } = useTranslation();
  if (!hasNoRecipient(channel)) {
    return null;
  }
  const label = t('CONFIGURATION.ALARM_V2.NO_RECIPIENT');
  // orange-700 rather than status-warn: this is the only signal in a list that carries no
  // text, and status-warn does not clear 3:1 against the row background.
  return (
    <MdOutlineWarningAmber
      role="img"
      title={label}
      aria-label={label}
      className="size-4 shrink-0 text-orange-700"
    />
  );
};
