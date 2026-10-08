import { AlarmV2Channel } from '@pinpoint-fe/ui/src/constants/types';

/**
 * A channel whose user group resolves to nobody still evaluates, still enqueues and still
 * reports success, so the rule looks healthy while the alarm reaches no one. Only EMAIL and
 * SMS land here; a WEBHOOK posts to its own destination and always has one.
 *
 * A query that does not compute the count leaves it absent, which is not a warning.
 */
export const hasNoRecipient = (channel?: Partial<AlarmV2Channel.ChannelData>) =>
  !!channel && channel.methodType !== 'WEBHOOK' && channel.recipientCount === 0;
