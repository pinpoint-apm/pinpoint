import { act, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { AlarmV2Page } from './AlarmV2';

/**
 * What the page assembles, not what its parts draw. (#14282)
 *
 * The page routes a URL to one of six views, scopes each tab, and turns a save into a
 * a toast and a cache invalidation. The parts it draws have their own tests, so this one
 * replaces them with markers and asks only which of them the page chose.
 */

jest.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

// The root barrel pulls in the whole package, which this test does not need.
jest.mock('@pinpoint-fe/ui', () => ({
  DataTableSkeleton: () => <div data-testid="skeleton" />,
}));

jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2RuleList', () => ({
  AlarmV2RuleList: (props: Record<string, unknown>) => (
    <div data-testid="rule-list" data-disabled={String(props.disabled)} />
  ),
}));
jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2ChannelList', () => ({
  AlarmV2ChannelList: (props: Record<string, unknown>) => (
    <div data-testid="channel-list" data-disabled={String(props.disabled)} />
  ),
}));
jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2TemplateList', () => ({
  AlarmV2TemplateList: (props: Record<string, unknown>) => (
    <div data-testid="template-list" data-disabled={String(props.disabled)} />
  ),
}));
jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2TemplateEditView', () => ({
  AlarmV2TemplateEditView: () => <div data-testid="template-edit" />,
}));
jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2TemplateStartView', () => ({
  AlarmV2TemplateStartView: () => <div data-testid="template-start" />,
}));
jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2EmptyState', () => ({
  AlarmV2EmptyState: ({ children }: { children?: React.ReactNode }) => (
    <div data-testid="empty-state">{children}</div>
  ),
}));
let mockSubmitChannelForm: ((values: unknown) => void) | undefined;
jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2ChannelForm', () => ({
  AlarmV2ChannelForm: (props: { onSubmit?: (values: unknown) => void }) => {
    mockSubmitChannelForm = props.onSubmit;
    return <div data-testid="channel-form" />;
  },
}));
// The sheet opens on the rule it is given; it has no `open` prop.
jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2HistorySheet', () => ({
  AlarmV2HistorySheet: ({ rule }: { rule?: { id?: number } }) =>
    rule ? <div data-testid="history-sheet">{rule.id}</div> : null,
}));
jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2Sheet', () => ({
  AlarmV2Sheet: ({ children, open }: { children?: React.ReactNode; open?: boolean }) =>
    open ? <div data-testid="sheet">{children}</div> : null,
}));
jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2ConfirmDialog', () => ({
  AlarmV2ConfirmDialog: () => null,
}));

// The rule form hands its values back through onSubmit, which is where a save begins.
let mockSubmitRuleForm: ((values: unknown, channelIds: number[]) => void) | undefined;
jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2RuleForm', () => ({
  AlarmV2RuleForm: (props: {
    onSubmit?: (values: unknown, channelIds: number[]) => void;
    applicationType?: string;
  }) => {
    mockSubmitRuleForm = props.onSubmit;
    return <div data-testid="rule-form" data-application-type={props.applicationType} />;
  },
}));

// `lib` re-exports the chart helpers, and they read a canvas at import time. Every
// `components/ui` import reaches them, so the test cuts them off at the one module.
jest.mock('@pinpoint-fe/ui/src/lib/charts', () => ({}));

// The notice imports the component barrel, which reaches the chart libraries.
jest.mock('../Forbidden403', () => ({
  Forbidden403: () => <div data-testid="forbidden" />,
}));

const mockToast = { success: jest.fn(), error: jest.fn() };
jest.mock('@pinpoint-fe/ui/src/components/Toast', () => ({
  useReactToastifyToast: () => mockToast,
}));

const mockInvalidateQueries = jest.fn();
const mockCancelQueries = jest.fn();
jest.mock('@tanstack/react-query', () => ({
  useQueryClient: () => ({
    invalidateQueries: mockInvalidateQueries,
    cancelQueries: mockCancelQueries,
  }),
}));

let mockIsForbidden = false;
let mockIsDefaultService = false;
jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  useGetUserGroup: () => ({ data: [] }),
  useIsForbiddenPath: () => mockIsForbidden,
  useIsDefaultService: () => mockIsDefaultService,
}));

jest.mock('@pinpoint-fe/ui/src/hooks/utility/useRequestService', () => ({
  useRequestService: () => 'my-service',
}));

// Module scope on purpose: a fresh array each call re-runs the deep link effect forever.
const mockRules: { id: number; applicationName: string; applicationType: string }[] = [];
const mockChannels: { id: number; name: string }[] = [];

const mockMutateChannelSave = jest.fn();
const mockMutateRuleSaveAsync = jest.fn().mockResolvedValue({ id: 7 });

jest.mock('@pinpoint-fe/ui/src/hooks/api', () => ({
  useAlarmV2RuleMutation: () => ({
    mutate: jest.fn(),
    mutateAsync: mockMutateRuleSaveAsync,
    isPending: false,
  }),
  useAlarmV2ChannelMutation: () => ({
    mutate: mockMutateChannelSave,
    mutateAsync: jest.fn().mockResolvedValue(undefined),
    isPending: false,
  }),
  useAlarmV2TemplateMutation: () => ({
    mutate: jest.fn(),
    mutateAsync: jest.fn().mockResolvedValue({ id: 3 }),
    isPending: false,
  }),
  useAlarmV2TemplateApplyMutation: () => ({
    mutateAsync: jest.fn().mockResolvedValue(undefined),
    isPending: false,
  }),
  useAlarmV2ChannelQuery: () => ({ data: mockChannels, isLoading: false }),
  useAlarmV2TemplateQuery: () => ({ data: [] }),
  useAlarmV2RuleQuery: () => ({ data: mockRules }),
  useAlarmV2DataSourcesQuery: () => ({ data: [] }),
  useAlarmV2ChannelsByRuleQuery: () => ({ data: [], isLoading: false }),
  useAlarmV2ChannelsByTemplateQuery: () => ({ data: [], isLoading: false }),
  useAlarmV2RuleHistoryQuery: () => ({ data: [] }),
  useAlarmV2RuleDetailQuery: () => ({ data: undefined }),
  useAlarmV2RuleStateQuery: () => ({ data: undefined }),
  useAlarmV2TemplatePresetsQuery: () => ({ data: [] }),
}));

const application = { applicationName: 'Shopping-Web', serviceType: 'SPRING_BOOT' };

const renderPage = (search = '', props: Record<string, unknown> = {}) =>
  render(
    <MemoryRouter initialEntries={[`/config/alarm${search}`]}>
      <AlarmV2Page hasPermission applications={[application]} {...props} />
    </MemoryRouter>,
  );

const buttonLabelled = (label: string) =>
  screen.getByText(label).closest('button') as HTMLButtonElement | null;

const storeSelection = () =>
  window.localStorage.setItem('pp.configAlarmV2SelectedApplication', JSON.stringify(application));

describe('AlarmV2Page', () => {
  beforeEach(() => {
    mockIsForbidden = false;
    mockIsDefaultService = false;
    mockSubmitRuleForm = undefined;
    mockSubmitChannelForm = undefined;
    window.localStorage.clear();
    mockRules.length = 0;
    mockChannels.length = 0;
    jest.clearAllMocks();
  });

  describe('the notice replaces the body', () => {
    // The body must stop asking while the notice is up: a 403 that arrives then leaves the
    // cache, and an observer that is still there asks again at once.
    test('draws no tab when the path is forbidden', () => {
      mockIsForbidden = true;

      renderPage();

      expect(screen.getByTestId('forbidden')).toBeDefined();
      expect(screen.queryByTestId('rule-list')).toBeNull();
      expect(screen.queryByText('CONFIGURATION.ALARM_V2.RULES')).toBeNull();
    });
  });

  describe('each tab keeps its own scope', () => {
    test('the rules tab offers the application picker', () => {
      const ApplicationList = () => <div data-testid="application-picker" />;

      renderPage('', { ApplicationList });

      expect(screen.getByTestId('application-picker')).toBeDefined();
      expect(screen.getByTestId('rule-list')).toBeDefined();
    });

    // A channel and a template belong to the service, so the viewer reaches them without
    // an application. Gating them on the picker locks a service that has none selected.
    test('lets the viewer add a channel with no application selected', async () => {
      renderPage();

      await userEvent.click(screen.getByText('CONFIGURATION.ALARM_V2.CHANNELS'));

      expect(buttonLabelled('CONFIGURATION.ALARM_V2.ADD_CHANNEL')?.disabled).toBe(false);
      expect(screen.getByTestId('channel-list').getAttribute('data-disabled')).toBe('false');
    });

    test('lets the viewer add a template with no application selected', async () => {
      renderPage();

      await userEvent.click(screen.getByText('CONFIGURATION.ALARM_V2.TEMPLATES'));

      expect(buttonLabelled('CONFIGURATION.ALARM_V2.ADD_TEMPLATE')?.disabled).toBe(false);
    });

    // Each tab mounts its own search box, and the tab the viewer leaves unmounts. A
    // keyword left behind must not narrow the list they come back to.
    test('clears the search when the viewer leaves the tab', async () => {
      renderPage();
      const search = () =>
        screen.getByLabelText('CONFIGURATION.ALARM_V2.SEARCH_RULES') as HTMLInputElement;

      await userEvent.type(search(), 'checkout');
      expect(search().value).toBe('checkout');

      await userEvent.click(screen.getByText('CONFIGURATION.ALARM_V2.CHANNELS'));
      await userEvent.click(screen.getByText('CONFIGURATION.ALARM_V2.RULES'));

      expect(search().value).toBe('');
    });

    // The default service has no user group of its own, so the server reads the manager
    // of the application instead. Without one it refuses the write.
    test('asks the default service for an application first', async () => {
      mockIsDefaultService = true;
      renderPage();

      await userEvent.click(screen.getByText('CONFIGURATION.ALARM_V2.CHANNELS'));

      expect(buttonLabelled('CONFIGURATION.ALARM_V2.ADD_CHANNEL')?.disabled).toBe(true);
    });

    test('lets the default service through once an application is picked', async () => {
      mockIsDefaultService = true;
      storeSelection();
      renderPage();

      await userEvent.click(screen.getByText('CONFIGURATION.ALARM_V2.CHANNELS'));

      expect(buttonLabelled('CONFIGURATION.ALARM_V2.ADD_CHANNEL')?.disabled).toBe(false);
    });

    test('keeps every tab read-only without the permission', async () => {
      renderPage('', { hasPermission: false });

      await userEvent.click(screen.getByText('CONFIGURATION.ALARM_V2.CHANNELS'));

      expect(buttonLabelled('CONFIGURATION.ALARM_V2.ADD_CHANNEL')?.disabled).toBe(true);
    });
  });

  describe('the url chooses the view', () => {
    test('opens the rule form for a new rule', () => {
      storeSelection();

      renderPage('?view=rule-new');

      expect(screen.getByTestId('rule-form')).toBeDefined();
      expect(screen.queryByTestId('rule-list')).toBeNull();
    });

    test('opens the channel form for a new channel', () => {
      renderPage('?view=channel-new');

      expect(screen.getByTestId('channel-form')).toBeDefined();
    });

    test('opens the start view for a new template', () => {
      renderPage('?view=template-new');

      expect(screen.getByTestId('template-start')).toBeDefined();
    });

    test('takes the application the name names', () => {
      const ApplicationList = ({
        selectedApplication,
      }: {
        selectedApplication?: { applicationName?: string };
      }) => <div data-testid="application-picker">{selectedApplication?.applicationName}</div>;

      renderPage('?applicationName=Shopping-Web', { ApplicationList });

      expect(screen.getByTestId('application-picker').textContent).toBe('Shopping-Web');
    });

    test('leaves the tabs up when the rule the id names is not there', () => {
      renderPage('?ruleId=999');

      expect(screen.queryByTestId('history-sheet')).toBeNull();
      expect(screen.getByTestId('rule-list')).toBeDefined();
    });

    // The id keeps its own meaning next to `?view=`: it opens the history of that rule.
    test('opens the history of the rule the id names', () => {
      mockRules.push({ id: 5, applicationName: 'Shopping-Web', applicationType: 'SPRING_BOOT' });

      renderPage('?ruleId=5');

      expect(screen.getByTestId('history-sheet').textContent).toBe('5');
    });

    test('leaves the history closed when a view is asked for as well', () => {
      mockRules.push({ id: 5, applicationName: 'Shopping-Web', applicationType: 'SPRING_BOOT' });

      renderPage('?ruleId=5&view=rule-edit');

      expect(screen.queryByTestId('history-sheet')).toBeNull();
    });
  });

  describe('a save reports and invalidates', () => {
    test('tells the viewer and clears the cache when a rule is saved', async () => {
      storeSelection();
      renderPage('?view=rule-new');

      await act(async () => {
        await mockSubmitRuleForm?.({ name: 'a rule' }, []);
      });

      expect(mockMutateRuleSaveAsync).toHaveBeenCalled();
      expect(mockToast.success).toHaveBeenCalled();
      expect(mockInvalidateQueries).toHaveBeenCalled();
    });

    // The server reads the application name off the request to check the write
    // permission, although the channel itself belongs to the service.
    test('creates a channel the viewer has not saved before', async () => {
      storeSelection();
      renderPage('?view=channel-new');

      await act(async () => mockSubmitChannelForm?.({ name: 'on call' }));

      expect(mockMutateChannelSave).toHaveBeenCalledWith({
        method: 'POST',
        applicationName: 'Shopping-Web',
        params: { name: 'on call' },
      });
    });

    test('updates the channel the viewer opened', async () => {
      storeSelection();
      mockChannels.push({ id: 12, name: 'on call' });
      renderPage('?view=channel-edit&channelId=12');

      await act(async () => mockSubmitChannelForm?.({ name: 'on call again' }));

      expect(mockMutateChannelSave).toHaveBeenCalledWith({
        method: 'PUT',
        id: 12,
        applicationName: 'Shopping-Web',
        params: { name: 'on call again' },
      });
    });

    test('tells the viewer when a rule cannot be saved', async () => {
      storeSelection();
      mockMutateRuleSaveAsync.mockRejectedValueOnce(new Error('nope'));
      renderPage('?view=rule-new');

      await act(async () => {
        await mockSubmitRuleForm?.({ name: 'a rule' }, []);
      });

      expect(mockToast.error).toHaveBeenCalled();
    });
  });
});
