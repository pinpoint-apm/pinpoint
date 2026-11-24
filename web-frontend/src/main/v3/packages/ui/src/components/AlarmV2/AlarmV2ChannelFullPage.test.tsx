import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { AlarmV2ChannelFullPage } from './AlarmV2ChannelFullPage';

/** Which user groups the channel editor offers. */

jest.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

// `lib` re-exports the chart helpers, and they read a canvas at import time.
jest.mock('@pinpoint-fe/ui/src/lib/charts', () => ({}));

// The package root pulls in the whole component tree.
jest.mock('@pinpoint-fe/ui', () => ({
  LoadingButton: () => <button />,
}));

jest.mock('@pinpoint-fe/ui/src/components/Toast', () => ({
  useReactToastifyToast: () => ({ success: jest.fn(), error: jest.fn() }),
}));

jest.mock('@tanstack/react-query', () => ({
  useQueryClient: () => ({ invalidateQueries: jest.fn() }),
}));

let mockConfiguration: { userId?: string; alarmChannelMemberCheck?: boolean } | undefined;
const mockUserGroupParams: unknown[] = [];
jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  useConfiguration: () => mockConfiguration,
  useGetUserGroup: (params: unknown, options: { enabled: boolean }) => {
    if (options.enabled) mockUserGroupParams.push(params);
    return { data: [{ id: 'my-team', number: '1' }] };
  },
}));

jest.mock('@pinpoint-fe/ui/src/hooks/api', () => ({
  useAlarmV2ChannelMutation: () => ({ mutate: jest.fn(), isPending: false }),
}));

const renderPage = (destination: string) =>
  render(
    <MemoryRouter>
      <AlarmV2ChannelFullPage
        onClose={jest.fn()}
        applicationName="Shopping-Web"
        currentTargetChannel={{ id: 4, channelName: 'ops', methodType: 'EMAIL', destination }}
        channelsData={[]}
      />
    </MemoryRouter>,
  );

beforeEach(() => {
  mockUserGroupParams.length = 0;
  // Radix Select needs these, and jsdom has none of them.
  Element.prototype.hasPointerCapture = () => false;
  Element.prototype.releasePointerCapture = () => {};
  Element.prototype.scrollIntoView = () => {};
});

test('asks only for the groups of the logged-in user', () => {
  mockConfiguration = { userId: 'alice', alarmChannelMemberCheck: true };
  renderPage('my-team');

  expect(mockUserGroupParams).toContainEqual({ userId: 'alice' });
  expect(mockUserGroupParams).not.toContainEqual({});
});

test('asks for no group without a login', () => {
  mockConfiguration = { alarmChannelMemberCheck: true };
  renderPage('my-team');

  expect(mockUserGroupParams).toEqual([]);
  screen.getByText('CONFIGURATION.ALARM.NO_LOGIN_USER_GROUP');
});

test('says nothing about the login when the user is logged in', () => {
  mockConfiguration = { userId: 'alice', alarmChannelMemberCheck: true };
  renderPage('my-team');

  expect(screen.queryByText('CONFIGURATION.ALARM.NO_LOGIN_USER_GROUP')).toBeNull();
});

test('asks for every group when the member check is off', () => {
  mockConfiguration = { userId: 'alice', alarmChannelMemberCheck: false };
  renderPage('my-team');

  expect(mockUserGroupParams).toContainEqual({});
  expect(mockUserGroupParams).not.toContainEqual({ userId: 'alice' });
});

test('keeps showing a saved group the user is not a member of', () => {
  mockConfiguration = { userId: 'alice', alarmChannelMemberCheck: true };
  renderPage('other-team');

  const pickers = screen.getAllByRole('combobox').map((picker) => picker.textContent);
  expect(pickers).toContain('other-team');
});

test('drops a saved group the user is not a member of when the method changes', async () => {
  mockConfiguration = { userId: 'alice', alarmChannelMemberCheck: true };
  renderPage('other-team');

  const [methodPicker] = screen.getAllByRole('combobox');
  await userEvent.click(methodPicker);
  await userEvent.click(screen.getByRole('option', { name: 'SMS' }));

  const pickers = screen.getAllByRole('combobox').map((picker) => picker.textContent);
  expect(pickers).not.toContain('other-team');
  await userEvent.click(screen.getAllByRole('combobox')[1]);
  expect(screen.queryByRole('option', { name: 'other-team' })).toBeNull();
});

test('keeps an own group when the method changes', async () => {
  mockConfiguration = { userId: 'alice', alarmChannelMemberCheck: true };
  renderPage('my-team');

  const [methodPicker] = screen.getAllByRole('combobox');
  await userEvent.click(methodPicker);
  await userEvent.click(screen.getByRole('option', { name: 'SMS' }));

  const pickers = screen.getAllByRole('combobox').map((picker) => picker.textContent);
  expect(pickers).toContain('my-team');
});
