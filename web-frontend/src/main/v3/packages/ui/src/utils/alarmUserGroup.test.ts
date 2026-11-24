import { Configuration } from '@pinpoint-fe/ui/src/constants';
import { getAlarmUserGroupQuery, withSavedUserGroup } from './alarmUserGroup';

const config = (values: Partial<Configuration>) => values as Configuration;

describe('getAlarmUserGroupQuery', () => {
  test('asks for the groups of the logged-in user', () => {
    expect(
      getAlarmUserGroupQuery(config({ userId: 'alice', alarmChannelMemberCheck: true })),
    ).toEqual({
      params: { userId: 'alice' },
      enabled: true,
    });
  });

  test('asks for no group without a login', () => {
    expect(getAlarmUserGroupQuery(config({ alarmChannelMemberCheck: true })).enabled).toBe(false);
  });

  test('asks for no group before the configuration arrives', () => {
    expect(getAlarmUserGroupQuery(undefined).enabled).toBe(false);
  });

  test('asks for every group when the configuration has no member check', () => {
    expect(getAlarmUserGroupQuery(config({ userId: 'alice' }))).toEqual({
      params: {},
      enabled: true,
    });
  });

  test('asks for every group when the member check is off', () => {
    expect(
      getAlarmUserGroupQuery(config({ userId: 'alice', alarmChannelMemberCheck: false })),
    ).toEqual({
      params: {},
      enabled: true,
    });
  });
});

describe('withSavedUserGroup', () => {
  test('keeps a saved group the user is not a member of', () => {
    expect(withSavedUserGroup(['team-a'], 'team-b')).toEqual(['team-b', 'team-a']);
  });

  test('does not repeat a saved group already offered', () => {
    expect(withSavedUserGroup(['team-a'], 'team-a')).toEqual(['team-a']);
  });
});
