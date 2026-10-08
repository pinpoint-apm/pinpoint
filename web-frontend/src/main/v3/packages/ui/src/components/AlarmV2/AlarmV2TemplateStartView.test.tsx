import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AlarmV2TemplatePreset } from '@pinpoint-fe/ui/src/constants/types';
import { AlarmV2TemplateStartView } from './AlarmV2TemplateStartView';

/** What the picker hands the editor: the items, and a name when one source was picked. */

jest.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (key: string) => key, i18n: { language: 'en' } }),
}));

// `lib` re-exports the chart helpers, and they read a canvas at import time.
jest.mock('@pinpoint-fe/ui/src/lib/charts', () => ({}));

jest.mock('@pinpoint-fe/ui/src/hooks/api', () => ({
  useAlarmV2DataSourcesQuery: () => ({
    data: [
      { value: 'APPLICATION_RESPONSE', category: 'APM' },
      { value: 'RUM_ERROR', category: 'RUM' },
    ],
  }),
}));
jest.mock('@pinpoint-fe/ui/src/hooks/utility/useAlarmV2CatalogLabels', () => ({
  useAlarmV2CatalogLabels: () => ({
    dataSourceLabel: (x: string) => x,
    metricLabel: (x: string) => x,
  }),
}));

const preset = (name: string, ruleName: string, dataSource = 'APPLICATION_RESPONSE') =>
  ({
    name: { en: name, ko: name },
    description: { en: 'a preset', ko: 'a preset' },
    dataSource,
    rules: [
      {
        name: { en: ruleName, ko: ruleName },
        description: { en: '', ko: '' },
        severity: 'WARNING',
        dataSource,
        checkIntervalSec: 180,
        actionIntervalSec: 1800,
        conditions: { type: 'LEAF', metric: 'error_rate', op: '>=', threshold: 10, windowSec: 300 },
        filters: [],
      },
    ],
  }) as unknown as AlarmV2TemplatePreset.PresetData;

const renderView = (presets: AlarmV2TemplatePreset.PresetData[], onContinue = jest.fn()) => {
  render(
    <AlarmV2TemplateStartView presets={presets} onContinue={onContinue} onCancel={jest.fn()} />,
  );
  return onContinue;
};

// The button names the rule count, and says so differently when nothing is picked.
const continueButton = () =>
  screen
    .getAllByRole('button')
    .find((button) =>
      /START_WITH_RULES|START_EMPTY/.test(button.textContent ?? ''),
    ) as HTMLButtonElement;

describe('AlarmV2TemplateStartView', () => {
  // The bundle the viewer starts has no name of its own yet, and the one source they
  // picked already has one. Carrying it over saves them typing it again.
  test('hands over the name of the only source picked', async () => {
    const onContinue = renderView([preset('Response basics', 'High error rate')]);

    await userEvent.click(screen.getByText('Response basics'));
    await userEvent.click(continueButton());

    expect(onContinue).toHaveBeenCalledWith(
      [expect.objectContaining({ name: 'High error rate' })],
      'Response basics',
    );
  });

  // Several sources have no one name between them, so the editor asks for one.
  test('hands over no name when several sources are picked', async () => {
    const onContinue = renderView([
      preset('Response basics', 'High error rate'),
      preset('Response extras', 'Slow rate'),
    ]);

    await userEvent.click(screen.getByText('Response basics'));
    await userEvent.click(screen.getByText('Response extras'));
    await userEvent.click(continueButton());

    expect(onContinue).toHaveBeenCalledWith(expect.any(Array), undefined);
  });

  // One bundle measures one category. A row that cannot join looks it, not just refuses the click.
  test('dims a source of another category once one is picked', async () => {
    renderView([
      preset('Response basics', 'High error rate'),
      preset('Error basics', 'Error spike', 'RUM_ERROR'),
    ]);

    await userEvent.click(screen.getByText('Response basics'));
    await userEvent.click(screen.getByText('Error basics'));

    const rumRow = screen.getByText('Error basics').closest('label') as HTMLLabelElement;
    const rumBox = rumRow.querySelector('button[role="checkbox"]') as HTMLButtonElement;
    expect(rumRow.className).toContain('opacity-50');
    expect(rumBox.disabled).toBe(true);
    expect(rumBox.getAttribute('data-state')).toBe('unchecked');
  });

  test('hands over no name when nothing is picked', async () => {
    const onContinue = renderView([preset('Response basics', 'High error rate')]);

    await userEvent.click(continueButton());

    expect(onContinue).toHaveBeenCalledWith([], undefined);
  });
});
