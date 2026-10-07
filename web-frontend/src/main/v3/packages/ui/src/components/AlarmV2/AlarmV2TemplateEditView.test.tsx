import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AlarmV2Template } from '@pinpoint-fe/ui/src/constants/types';
import { AlarmV2TemplateEditView } from './AlarmV2TemplateEditView';

/** What the editor saves as the name when the viewer leaves the field empty. */

jest.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (key: string) => key, i18n: { language: 'en' } }),
}));

// `lib` re-exports the chart helpers, and they read a canvas at import time.
jest.mock('@pinpoint-fe/ui/src/lib/charts', () => ({}));

// The toast module brings its library's stylesheet, which no test can read.
jest.mock('@pinpoint-fe/ui/src/components/Toast', () => ({
  useReactToastifyToast: () => ({ success: jest.fn(), error: jest.fn() }),
}));

// The rule card reaches the package root, and that pulls in the whole component tree.
// A bundle with no items still opens one empty card, and the editor asks that card to
// validate itself through `formHandleRef`, so the stand-in answers through the same prop.
jest.mock('@pinpoint-fe/ui/src/components/AlarmV2/AlarmV2RuleForm', () => {
  const react = jest.requireActual('react');
  return {
    AlarmV2RuleForm: ({ formHandleRef }: { formHandleRef?: (handle: unknown) => void }) => {
      react.useEffect(() => {
        formHandleRef?.({
          validate: () => ({ name: 'a rule' }),
          getValues: () => ({ name: 'a rule' }),
          focusFirstError: () => {},
        });
      });
      return null;
    },
  };
});

jest.mock('@pinpoint-fe/ui/src/hooks/api', () => ({
  useAlarmV2DataSourcesQuery: () => ({ data: [] }),
  useAlarmV2MetricsQuery: () => ({ data: [] }),
}));
jest.mock('@pinpoint-fe/ui/src/hooks/utility/useAlarmV2CatalogLabels', () => ({
  useAlarmV2CatalogLabels: () => ({
    dataSourceLabel: (x: string) => x,
    metricLabel: (x: string) => x,
  }),
}));

// A bundle with no items has no rule cards, which is all this test needs.
const emptyBundle = {
  serviceName: 'my-service',
  name: '',
  items: [],
} as unknown as AlarmV2Template.TemplateData;

const nameField = () =>
  screen.getByPlaceholderText('CONFIGURATION.ALARM_V2.TEMPLATE_NAME_PLACEHOLDER');
const saveButton = () =>
  screen.getAllByRole('button').find((b) => b.textContent === 'COMMON.SAVE') as HTMLButtonElement;

const renderView = (props: Record<string, unknown> = {}) => {
  const onSubmit = jest.fn();
  render(
    <AlarmV2TemplateEditView
      template={emptyBundle}
      channels={[]}
      linkedChannelIds={[]}
      onSubmit={onSubmit}
      onCancel={jest.fn()}
      {...props}
    />,
  );
  return onSubmit;
};

describe('AlarmV2TemplateEditView', () => {
  test('seeds the field with the name the source gave', () => {
    renderView({
      template: { ...emptyBundle, name: 'Response basics' },
      defaultName: 'Response basics',
    });

    expect((nameField() as HTMLInputElement).value).toBe('Response basics');
  });

  // The field is seeded from the source, so an empty one means the viewer cleared it.
  // Saving the name they started with beats refusing to save at all.
  test('saves the default name when the viewer clears the field', async () => {
    const onSubmit = renderView({
      template: { ...emptyBundle, name: 'Response basics' },
      defaultName: 'Response basics',
    });

    await userEvent.clear(nameField());
    await userEvent.click(saveButton());

    expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({ name: 'Response basics' }), []);
    expect(screen.queryByText('CONFIGURATION.ALARM_V2.VALIDATION_NAME_REQUIRED')).toBeNull();
  });

  test('puts the saved name back in the field', async () => {
    renderView({
      template: { ...emptyBundle, name: 'Response basics' },
      defaultName: 'Response basics',
    });

    await userEvent.clear(nameField());
    await userEvent.click(saveButton());

    expect((nameField() as HTMLInputElement).value).toBe('Response basics');
  });

  // A bundle the viewer started from several sources has no name to fall back on.
  test('still asks for a name when there is no default', async () => {
    const onSubmit = renderView();

    await userEvent.click(saveButton());

    expect(onSubmit).not.toHaveBeenCalled();
    expect(screen.getByText('CONFIGURATION.ALARM_V2.VALIDATION_NAME_REQUIRED')).toBeDefined();
  });

  test('keeps what the viewer typed over the default', async () => {
    const onSubmit = renderView({
      template: { ...emptyBundle, name: 'Response basics' },
      defaultName: 'Response basics',
    });

    await userEvent.clear(nameField());
    await userEvent.type(nameField(), 'My own name');
    await userEvent.click(saveButton());

    expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({ name: 'My own name' }), []);
  });
});
