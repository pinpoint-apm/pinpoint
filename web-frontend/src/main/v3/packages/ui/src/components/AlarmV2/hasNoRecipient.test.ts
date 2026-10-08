import { AlarmV2Channel } from '@pinpoint-fe/ui/src/constants/types';
import { hasNoRecipient } from './hasNoRecipient';

const channel = (
  methodType: AlarmV2Channel.ChannelData['methodType'],
  recipientCount?: number,
): AlarmV2Channel.ChannelData => ({
  channelName: 'test',
  methodType,
  destination: methodType === 'WEBHOOK' ? 'https://example.com/alarm' : 'ops-team',
  recipientCount,
});

describe('hasNoRecipient', () => {
  it('warns on an EMAIL or SMS channel whose group resolves to nobody', () => {
    expect(hasNoRecipient(channel('EMAIL', 0))).toBe(true);
    expect(hasNoRecipient(channel('SMS', 0))).toBe(true);
  });

  it('stays quiet once the group has someone', () => {
    expect(hasNoRecipient(channel('EMAIL', 1))).toBe(false);
  });

  it('stays quiet on a webhook, which posts to its own destination', () => {
    expect(hasNoRecipient(channel('WEBHOOK', 0))).toBe(false);
  });

  it('stays quiet when the count is missing rather than zero', () => {
    expect(hasNoRecipient(channel('EMAIL'))).toBe(false);
    expect(hasNoRecipient(undefined)).toBe(false);
  });
});
