import { AlarmV2Rule } from '@pinpoint-fe/ui/src/constants/types';
import type { FormValues } from './AlarmV2RuleForm';
import { buildRuleSaveData, type RuleSaveContext } from './buildRuleSaveData';

const CONTEXT: RuleSaveContext = {
  serviceName: 'DEFAULT',
  applicationName: 'test-app',
  applicationType: 'SPRING_BOOT',
};

const CONDITIONS: AlarmV2Rule.AlarmCondition = {
  type: AlarmV2Rule.ConditionType.LEAF,
  metric: 'error_count',
  op: '>=',
  threshold: 100,
  windowSec: 3600,
};

const values = (overrides: Partial<FormValues> = {}): FormValues =>
  ({
    name: 'rule name',
    description: 'rule description',
    severity: 'WARNING',
    dataSource: 'TEST_SOURCE',
    checkIntervalSec: 300,
    actionIntervalSec: 1800,
    conditions: CONDITIONS,
    filters: [],
    enabled: true,
    ...overrides,
  }) as FormValues;

describe('buildRuleSaveData', () => {
  it('throws when the application type is unknown', () => {
    expect(() => buildRuleSaveData(values(), { ...CONTEXT, applicationType: undefined })).toThrow(
      'applicationType is required',
    );
  });

  describe('a standalone rule', () => {
    it('sends every field, with templateItemId nulled', () => {
      const result = buildRuleSaveData(values(), CONTEXT);

      expect(result).toEqual({
        serviceName: 'DEFAULT',
        applicationName: 'test-app',
        applicationType: 'SPRING_BOOT',
        enabled: true,
        templateItemId: null,
        name: 'rule name',
        description: 'rule description',
        severity: 'WARNING',
        dataSource: 'TEST_SOURCE',
        checkIntervalSec: 300,
        actionIntervalSec: 1800,
        conditions: CONDITIONS,
        filters: [],
      });
    });

    it('sends an empty filter list rather than omitting the key', () => {
      const result = buildRuleSaveData(values({ filters: undefined }), CONTEXT);

      expect(result.filters).toEqual([]);
      expect(JSON.parse(JSON.stringify(result))).toHaveProperty('filters');
    });
  });

  describe('a rule linked to a bundle item', () => {
    const linked = (overrideKeys: FormValues['overrideKeys']) =>
      buildRuleSaveData(values({ templateItemId: 7, overrideKeys }), CONTEXT);

    it('omits every inheritable field when nothing is overridden', () => {
      const result = linked([]);

      expect(result).toEqual({
        serviceName: 'DEFAULT',
        applicationName: 'test-app',
        applicationType: 'SPRING_BOOT',
        enabled: true,
        templateItemId: 7,
        dataSource: 'TEST_SOURCE',
      });
      for (const key of [
        'name',
        'description',
        'severity',
        'checkIntervalSec',
        'actionIntervalSec',
        'conditions',
        'filters',
      ]) {
        expect(result).not.toHaveProperty(key);
      }
    });

    it('sends only the overridden field', () => {
      const result = linked(['conditions']);

      expect(result.conditions).toEqual(CONDITIONS);
      expect(result).not.toHaveProperty('name');
      expect(result).not.toHaveProperty('severity');
    });

    it.each([
      ['name', 'rule name'],
      ['description', 'rule description'],
      ['severity', 'WARNING'],
      ['checkIntervalSec', 300],
      ['actionIntervalSec', 1800],
    ] as const)('carries %s when it is overridden', (key, expected) => {
      expect(linked([key])).toHaveProperty(key, expected);
    });

    it('sends an empty filter list when filters are overridden but cleared', () => {
      expect(linked(['filters']).filters).toEqual([]);
      expect(
        buildRuleSaveData(
          values({ templateItemId: 7, overrideKeys: ['filters'], filters: undefined }),
          CONTEXT,
        ).filters,
      ).toEqual([]);
    });

    it('always sends the data source, which a linked rule cannot inherit', () => {
      expect(linked([]).dataSource).toBe('TEST_SOURCE');
      expect(linked(['name']).dataSource).toBe('TEST_SOURCE');
    });
  });
});
