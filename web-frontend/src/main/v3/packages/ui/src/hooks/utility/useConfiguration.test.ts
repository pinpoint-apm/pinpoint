import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';
import { useConfiguration } from './useConfiguration';

const store = getDefaultStore();

/** 확장 저장소(naver 등)의 Configuration을 흉내낸 타입. */
interface ExtendedConfiguration extends Configuration {
  showSqlStat: boolean;
  'periodMax.sqlStat': number;
}

const setConfiguration = (configuration: Configuration | undefined) =>
  act(() => {
    store.set(configurationAtom, configuration);
  });

describe('useConfiguration', () => {
  beforeEach(() => {
    setConfiguration(undefined);
  });

  test('returns undefined before the configuration is loaded', () => {
    // configuration은 부트스트랩 이후 비동기로 로드되므로 초기에는 비어 있다.
    expect(renderHook(() => useConfiguration()).result.current).toBeUndefined();
  });

  test('returns the configuration stored in the atom', () => {
    const configuration = { showHeatmap: true } as Configuration;
    setConfiguration(configuration);

    expect(renderHook(() => useConfiguration()).result.current).toEqual(configuration);
  });

  test('reflects a configuration update', () => {
    const { result } = renderHook(() => useConfiguration());

    setConfiguration({ showHeatmap: true } as Configuration);
    expect(result.current?.showHeatmap).toBe(true);

    setConfiguration({ showHeatmap: false } as Configuration);
    expect(result.current?.showHeatmap).toBe(false);
  });

  test('returns undefined again when the configuration is cleared', () => {
    setConfiguration({ showHeatmap: true } as Configuration);
    setConfiguration(undefined);

    expect(renderHook(() => useConfiguration()).result.current).toBeUndefined();
  });

  test('keeps the fields of an extended configuration when read with a type argument', () => {
    // 확장 저장소는 자기 타입을 타입 인자로 넘겨 추가 필드까지 읽는다.
    // atom은 base 타입이지만 구조적 타이핑상 확장 객체가 그대로 들어가고 값도 보존된다.
    const configuration = {
      showHeatmap: true,
      showSqlStat: true,
      'periodMax.sqlStat': 7,
    } as ExtendedConfiguration;
    setConfiguration(configuration);

    const { result } = renderHook(() => useConfiguration<ExtendedConfiguration>());

    expect(result.current?.showSqlStat).toBe(true);
    expect(result.current?.['periodMax.sqlStat']).toBe(7);
    expect(result.current?.showHeatmap).toBe(true);
  });
});
