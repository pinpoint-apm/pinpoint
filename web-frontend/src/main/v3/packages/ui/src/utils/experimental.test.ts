import { Configuration, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';
import { getEnableServiceMap, pickEnableServiceMap } from './experimental';

const configWithServiceMap = (enable: boolean) =>
  ({ 'experimental.enableServiceMap.value': enable }) as unknown as Configuration;

describe('pickEnableServiceMap', () => {
  test('uses the configuration value while the user has picked nothing', () => {
    expect(pickEnableServiceMap(null, configWithServiceMap(true))).toBe(true);
    expect(pickEnableServiceMap(undefined, configWithServiceMap(true))).toBe(true);
    expect(pickEnableServiceMap(null, configWithServiceMap(false))).toBe(false);
  });

  test('is false while configuration has not loaded yet', () => {
    expect(pickEnableServiceMap(null, undefined)).toBe(false);
  });

  // 이 화면(useServicesFetch)과 요청 헤더(인터셉터)가 같은 규칙을 지나야 하는 이유.
  // 저장된 값이 있으면 configuration이 도착하기 전부터 켜진 것으로 보이는데, 그때 인터셉터가
  // configuration만 봤다면 /api/v2/services가 pServiceName 없이 나간다.
  test('a stored value applies before configuration has loaded', () => {
    expect(pickEnableServiceMap(true, undefined)).toBe(true);
    expect(pickEnableServiceMap(false, undefined)).toBe(false);
  });

  test('the value the user picked wins over the configuration default, both ways', () => {
    expect(pickEnableServiceMap(true, configWithServiceMap(false))).toBe(true);
    expect(pickEnableServiceMap(false, configWithServiceMap(true))).toBe(false);
  });

  // localStorage에 사람이 손으로 넣은 값이나 예전 형식이 남아 있어도 기본값으로 흘러가야 한다.
  test('ignores a stored value that is not a boolean', () => {
    expect(pickEnableServiceMap('true', configWithServiceMap(false))).toBe(false);
    expect(pickEnableServiceMap(1, configWithServiceMap(false))).toBe(false);
  });
});

describe('getEnableServiceMap', () => {
  beforeEach(() => {
    window.localStorage.clear();
  });

  test('falls back to configuration when nothing is stored', () => {
    expect(getEnableServiceMap(configWithServiceMap(true))).toBe(true);
  });

  test('reads the stored override', () => {
    window.localStorage.setItem(EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP, 'false');

    expect(getEnableServiceMap(configWithServiceMap(true))).toBe(false);
  });

  test('a broken stored value does not throw, it falls back to configuration', () => {
    window.localStorage.setItem(EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP, '{oops');

    expect(getEnableServiceMap(configWithServiceMap(true))).toBe(true);
  });
});
