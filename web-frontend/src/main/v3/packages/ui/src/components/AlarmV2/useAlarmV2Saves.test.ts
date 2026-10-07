import { act, renderHook, waitFor } from '@testing-library/react';
import { AlarmV2Template } from '@pinpoint-fe/ui/src/constants/types';
import { useAlarmV2RuleSave, useAlarmV2TemplateSave } from './useAlarmV2Saves';

/** When a bundle save stops to ask, and what it does once it runs. */

jest.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

const mockToast = { success: jest.fn(), error: jest.fn() };
jest.mock('@pinpoint-fe/ui/src/components/Toast', () => ({
  useReactToastifyToast: () => mockToast,
}));

const mockNavigate = jest.fn();
jest.mock('react-router', () => ({ useNavigate: () => mockNavigate }));

const mockInvalidateQueries = jest.fn().mockResolvedValue(undefined);
jest.mock('@tanstack/react-query', () => ({
  useQueryClient: () => ({ invalidateQueries: mockInvalidateQueries }),
}));

const mockSaveRuleAsync = jest.fn();
const mockSaveTemplateAsync = jest.fn();
const mockLinkChannelAsync = jest.fn();
jest.mock('@pinpoint-fe/ui/src/hooks/api', () => ({
  useAlarmV2RuleMutation: jest.fn(),
  useAlarmV2TemplateMutation: () => ({
    mutateAsync: mockSaveTemplateAsync,
    isPending: false,
  }),
  useAlarmV2ChannelMutation: () => ({ mutateAsync: mockLinkChannelAsync }),
}));

const templateData = { name: 'Response basics', items: [] };
const appliedTemplate = (usedRuleCount: number) =>
  ({ id: 10, name: 'Response basics', usedRuleCount }) as AlarmV2Template.TemplateData;

const setup = (overrides: Partial<Parameters<typeof useAlarmV2TemplateSave>[0]> = {}) => {
  const closeView = jest.fn();
  const { result } = renderHook(() =>
    useAlarmV2TemplateSave({
      applicationName: 'Shopping-Web',
      editingTemplate: undefined,
      templateChannelIds: [],
      closeView,
      ...overrides,
    }),
  );
  return { result, closeView };
};

describe('useAlarmV2TemplateSave', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockSaveTemplateAsync.mockResolvedValue({ id: 10 });
    mockLinkChannelAsync.mockResolvedValue(undefined);
  });

  describe('saving asks first', () => {
    // Editing a bundle rewrites the rules it stamped, in applications the viewer cannot
    // see from this page. Asking is the only sign that happens.
    test('holds the save when the bundle already stamped a rule', async () => {
      const { result } = setup({ editingTemplate: appliedTemplate(3) });

      act(() => result.current.saveTemplate(templateData, [4]));

      await waitFor(() =>
        expect(result.current.pendingTemplateSave).toEqual({
          templateData,
          selectedChannelIds: [4],
        }),
      );
      expect(mockSaveTemplateAsync).not.toHaveBeenCalled();
    });

    test('saves at once when the bundle stamped nothing', async () => {
      const { result } = setup({ editingTemplate: appliedTemplate(0) });

      act(() => result.current.saveTemplate(templateData, []));

      await waitFor(() => expect(mockSaveTemplateAsync).toHaveBeenCalled());
      expect(result.current.pendingTemplateSave).toBeUndefined();
    });

    test('saves at once when the bundle is new', async () => {
      const { result } = setup();

      act(() => result.current.saveTemplate(templateData, []));

      await waitFor(() => expect(mockSaveTemplateAsync).toHaveBeenCalled());
    });

    test('lets go of what waits once it is dismissed', async () => {
      const { result } = setup({ editingTemplate: appliedTemplate(3) });
      act(() => result.current.saveTemplate(templateData, [4]));
      await waitFor(() => expect(result.current.pendingTemplateSave).toBeDefined());

      act(() => result.current.dismissPendingTemplateSave());

      await waitFor(() => expect(result.current.pendingTemplateSave).toBeUndefined());
    });
  });

  describe('running the save', () => {
    test('reports and clears the cache', async () => {
      const { result, closeView } = setup();

      await result.current.runTemplateSave(templateData, []);

      expect(mockToast.success).toHaveBeenCalled();
      expect(closeView).toHaveBeenCalled();
      expect(mockInvalidateQueries).toHaveBeenCalled();
    });

    test('links the channels the viewer picked', async () => {
      const { result } = setup({ templateChannelIds: [1] });

      await result.current.runTemplateSave(templateData, [2]);

      expect(mockLinkChannelAsync).toHaveBeenCalledWith(
        expect.objectContaining({ method: 'LINK_TEMPLATE', channelId: 2, templateId: 10 }),
      );
      expect(mockLinkChannelAsync).toHaveBeenCalledWith(
        expect.objectContaining({ method: 'UNLINK_TEMPLATE', channelId: 1, templateId: 10 }),
      );
    });

    // The bundle is saved by then, so the viewer is told about the links alone and the
    // page stays where it is.
    test('stops short of the all-clear when a link fails', async () => {
      mockLinkChannelAsync.mockRejectedValueOnce(new Error('nope'));
      const { result, closeView } = setup();

      await result.current.runTemplateSave(templateData, [2]);

      expect(mockToast.error).toHaveBeenCalledWith('CONFIGURATION.ALARM_V2.CHANNEL_LINK_FAILED');
      expect(mockToast.success).not.toHaveBeenCalled();
      expect(closeView).not.toHaveBeenCalled();
    });

    test('tells the viewer when the bundle itself cannot be saved', async () => {
      mockSaveTemplateAsync.mockRejectedValueOnce(new Error('nope'));
      const { result, closeView } = setup();

      await result.current.runTemplateSave(templateData, []);

      expect(mockToast.error).toHaveBeenCalledWith('CONFIGURATION.ALARM_V2.TEMPLATE_UPDATE_FAILED');
      expect(closeView).not.toHaveBeenCalled();
    });
  });
});

describe('useAlarmV2RuleSave', () => {
  const values = { name: 'High error rate' } as unknown as Parameters<
    ReturnType<typeof useAlarmV2RuleSave>['saveRule']
  >[0];

  const setupRule = (overrides: Partial<Parameters<typeof useAlarmV2RuleSave>[0]> = {}) => {
    const closeView = jest.fn();
    const { result } = renderHook(() =>
      useAlarmV2RuleSave({
        serviceName: 'my-service',
        applicationName: 'Shopping-Web',
        fallbackApplicationType: 'SPRING_BOOT',
        currentRule: undefined,
        ruleChannelIds: [],
        saveRuleAsync: mockSaveRuleAsync,
        linkChannelAsync: mockLinkChannelAsync,
        closeView,
        ...overrides,
      }),
    );
    return { result, closeView };
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockSaveRuleAsync.mockResolvedValue({ id: 7 });
    mockLinkChannelAsync.mockResolvedValue(undefined);
  });

  test('creates a rule the viewer has not saved before', async () => {
    const { result } = setupRule();

    await result.current.saveRule(values, []);

    expect(mockSaveRuleAsync).toHaveBeenCalledWith(expect.objectContaining({ method: 'POST' }));
  });

  test('updates the rule the viewer opened', async () => {
    const { result } = setupRule({ currentRule: { id: 3 } });

    await result.current.saveRule(values, []);

    expect(mockSaveRuleAsync).toHaveBeenCalledWith(
      expect.objectContaining({ method: 'PUT', id: 3 }),
    );
  });

  test('links the channels the viewer picked', async () => {
    const { result } = setupRule({ ruleChannelIds: [1] });

    await result.current.saveRule(values, [2]);

    expect(mockLinkChannelAsync).toHaveBeenCalledWith(
      expect.objectContaining({ method: 'LINK', channelId: 2, ruleId: 7 }),
    );
    expect(mockLinkChannelAsync).toHaveBeenCalledWith(
      expect.objectContaining({ method: 'UNLINK', channelId: 1, ruleId: 7 }),
    );
  });

  // A rule a bundle stamped takes its channels from that bundle. Writing links here
  // would reach every application the bundle is applied to.
  test('leaves the channels of a rule its bundle stamped alone', async () => {
    const { result } = setupRule({ currentRule: { id: 3 }, ruleChannelIds: [1] });

    await result.current.saveRule({ ...values, templateItemId: 9 }, [2]);

    expect(mockSaveRuleAsync).toHaveBeenCalled();
    expect(mockLinkChannelAsync).not.toHaveBeenCalled();
  });

  test('tells the viewer when the rule cannot be saved', async () => {
    mockSaveRuleAsync.mockRejectedValueOnce(new Error('nope'));
    const { result, closeView } = setupRule();

    await result.current.saveRule(values, []);

    expect(mockToast.error).toHaveBeenCalledWith('CONFIGURATION.ALARM_V2.RULE_UPDATE_FAILED');
    expect(closeView).not.toHaveBeenCalled();
  });
});
