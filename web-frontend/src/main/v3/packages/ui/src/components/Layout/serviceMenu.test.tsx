import { buildServiceAsideMenus, buildServiceSidebarItems } from './serviceMenu';
import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { DEFAULT_SERVICE } from '@pinpoint-fe/ui/src/atoms';

describe('buildServiceSidebarItems', () => {
  const noop = () => {};

  test('returns an empty list when services is undefined', () => {
    expect(buildServiceSidebarItems(undefined, DEFAULT_SERVICE, noop)).toEqual([]);
  });

  test('orders the selected service first, then DEFAULT, then the rest alphabetically', () => {
    const items = buildServiceSidebarItems(
      ['beta', DEFAULT_SERVICE, 'alpha', 'selected'],
      'selected',
      noop,
    );

    expect(items.map((i) => i.name)).toEqual(['selected', DEFAULT_SERVICE, 'alpha', 'beta']);
  });

  test('keeps DEFAULT first when the selected service is not in the list', () => {
    const items = buildServiceSidebarItems(['b', DEFAULT_SERVICE, 'a'], 'missing', noop);

    expect(items.map((i) => i.name)).toEqual([DEFAULT_SERVICE, 'a', 'b']);
  });

  test('marks only the selected service as selected and builds its path', () => {
    const items = buildServiceSidebarItems(['a', 'b'], 'b', noop);
    const selected = items.find((i) => i.name === 'b');
    const other = items.find((i) => i.name === 'a');

    expect(selected?.selected).toBe(true);
    expect(other?.selected).toBe(false);
    expect(selected?.path).toBe(`${APP_PATH.CONFIG_SERVICES}#b`);
  });

  test('invokes onSelect with the service name when an item is clicked', () => {
    const onSelect = jest.fn();
    const items = buildServiceSidebarItems(['a', 'b'], 'a', onSelect);

    items.find((i) => i.name === 'b')?.onClick?.();

    expect(onSelect).toHaveBeenCalledWith('b');
  });

  test('does not mutate the input services array while sorting', () => {
    const services = ['b', 'a', DEFAULT_SERVICE];
    buildServiceSidebarItems(services, 'a', noop);

    expect(services).toEqual(['b', 'a', DEFAULT_SERVICE]);
  });
});

describe('buildServiceAsideMenus', () => {
  test('returns an empty list when services is undefined', () => {
    expect(buildServiceAsideMenus(undefined)).toEqual([]);
  });

  test('maps each service to a config-services menu entry', () => {
    expect(buildServiceAsideMenus(['a', 'b'])).toEqual([
      { name: 'a', path: APP_PATH.CONFIG_SERVICES, href: APP_PATH.CONFIG_SERVICES },
      { name: 'b', path: APP_PATH.CONFIG_SERVICES, href: APP_PATH.CONFIG_SERVICES },
    ]);
  });
});
