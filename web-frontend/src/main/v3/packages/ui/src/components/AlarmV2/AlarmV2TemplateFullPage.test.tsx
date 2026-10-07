import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { AlarmV2Template } from '@pinpoint-fe/ui/src/constants/types';
import { AlarmV2TemplateFullPage } from './AlarmV2TemplateFullPage';

/** Which of the three bundle screens the view shows, and what the confirm answers. */

jest.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

// `lib` re-exports the chart helpers, and they read a canvas at import time.
jest.mock('@pinpoint-fe/ui/src/lib/charts', () => ({}));

jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2TemplateStartView', () => ({
  AlarmV2TemplateStartView: (props: { onContinue?: (items: unknown[]) => void }) => (
    <button data-testid="start-view" onClick={() => props.onContinue?.([])}>
      continue
    </button>
  ),
}));
jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2TemplateEditView', () => ({
  AlarmV2TemplateEditView: (props: { template?: { name?: string } }) => (
    <div data-testid="edit-view">{props.template?.name}</div>
  ),
}));
const mockSaveTemplate = jest.fn();
const mockRunTemplateSave = jest.fn().mockResolvedValue(undefined);
const mockDismissPendingTemplateSave = jest.fn();
let mockPendingTemplateSave: unknown;
// What the view hands the save hook is the thing no other suite sees: the hook's own
// tests inject it by hand, so a wrong `editingTemplate` here would turn off the confirm.
const mockTemplateSaveArgs: Record<string, unknown>[] = [];
jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/useAlarmV2Saves', () => ({
  channelIdsOf: (channels: { id?: number }[] = []) =>
    channels.map((c) => c.id).filter((id): id is number => id !== undefined),
  useAlarmV2TemplateSave: (args: Record<string, unknown>) => {
    mockTemplateSaveArgs.push(args);
    return {
      saveTemplate: mockSaveTemplate,
      runTemplateSave: mockRunTemplateSave,
      isPending: false,
      pendingTemplateSave: mockPendingTemplateSave,
      dismissPendingTemplateSave: mockDismissPendingTemplateSave,
    };
  },
}));

jest.mock('@pinpoint-fe/ui/src/hooks/api', () => ({
  useAlarmV2TemplatePresetsQuery: () => ({ data: [] }),
  useAlarmV2ChannelsByTemplateQuery: () => ({ data: [], isLoading: false }),
}));

jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2ConfirmDialog', () => ({
  AlarmV2ConfirmDialog: ({ open, onConfirm }: { open?: boolean; onConfirm?: () => void }) =>
    open ? (
      <button data-testid="confirm" onClick={onConfirm}>
        confirm
      </button>
    ) : null,
}));

const template = {
  id: 10,
  name: 'Response basics',
  items: [],
} as unknown as AlarmV2Template.TemplateData;

const baseProps = {
  onClose: jest.fn(),
  serviceName: 'my-service',
  applicationName: 'Shopping-Web',
  applicationType: 'SPRING_BOOT',
  isTemplateNewView: false,
  isTemplateEditView: false,
  viewTemplateId: undefined as number | undefined,
  templatesData: [template],
  rulesData: [],
  channelsData: [],
  isChannelsLoading: false,
  hasPermission: true,
};

const renderView = (props: Partial<typeof baseProps> = {}) =>
  render(
    <MemoryRouter>
      <AlarmV2TemplateFullPage {...baseProps} {...props} />
    </MemoryRouter>,
  );

describe('AlarmV2TemplateFullPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockPendingTemplateSave = undefined;
    mockTemplateSaveArgs.length = 0;
  });

  // A new bundle starts at the picker, and the items it hands back open the editor.
  test('starts a new bundle at the picker', () => {
    renderView({ isTemplateNewView: true });

    expect(screen.getByTestId('start-view')).toBeDefined();
    expect(screen.queryByTestId('edit-view')).toBeNull();
  });

  test('opens the editor once the picker has handed its items over', async () => {
    renderView({ isTemplateNewView: true });

    await userEvent.click(screen.getByTestId('start-view'));

    expect(screen.getByTestId('edit-view')).toBeDefined();
    expect(screen.queryByTestId('start-view')).toBeNull();
  });

  // The editor seeds its state once, at mount, so it must not mount before the list
  // arrives -- a reload lands here with no data yet.
  test('waits for the list before it edits a bundle', () => {
    renderView({ isTemplateEditView: true, viewTemplateId: 999 });

    expect(screen.queryByTestId('edit-view')).toBeNull();
  });

  test('edits the bundle the list carries', () => {
    renderView({ isTemplateEditView: true, viewTemplateId: 10 });

    expect(screen.getByTestId('edit-view').textContent).toBe('Response basics');
  });

  describe('the confirm', () => {
    test('stays away while nothing waits on it', () => {
      renderView({ isTemplateEditView: true, viewTemplateId: 10 });

      expect(screen.queryByTestId('confirm')).toBeNull();
    });

    test('saves what waits on it, and lets it go', async () => {
      mockPendingTemplateSave = {
        templateData: { name: 'a bundle', items: [] },
        selectedChannelIds: [4],
      };
      renderView({ isTemplateEditView: true, viewTemplateId: 10 });

      await userEvent.click(screen.getByTestId('confirm'));

      expect(mockDismissPendingTemplateSave).toHaveBeenCalled();
      expect(mockRunTemplateSave).toHaveBeenCalledWith({ name: 'a bundle', items: [] }, [4]);
    });
  });

  // The confirm only stops a bundle that has stamped rules, and it reads that off the
  // bundle the view resolved. A view that resolves none turns the confirm off silently.
  describe('what the view hands the save hook', () => {
    test('passes the bundle the id names', () => {
      renderView({ isTemplateEditView: true, viewTemplateId: 10 });

      expect(mockTemplateSaveArgs[mockTemplateSaveArgs.length - 1]).toEqual(
        expect.objectContaining({ editingTemplate: expect.objectContaining({ id: 10 }) }),
      );
    });

    test('passes no bundle while a new one is being started', () => {
      renderView({ isTemplateNewView: true });

      expect(mockTemplateSaveArgs[mockTemplateSaveArgs.length - 1]).toEqual(
        expect.objectContaining({ editingTemplate: undefined }),
      );
    });
  });
});
