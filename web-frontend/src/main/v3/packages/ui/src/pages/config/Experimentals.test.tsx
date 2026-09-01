import { fireEvent, render, screen } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';
import { ExperimentalPage } from './Experimentals';

jest.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

const store = getDefaultStore();

// 체크박스 라벨은 configuration이 내려주는 description이다.
const SERVICE_MAP_LABEL = 'Enable service-based application map grouping.';
const configuration = {
  'experimental.enableServerMapRealTime.description': 'Enable server map real time.',
  'experimental.useStatisticsAgentState.description': 'Use statistics agent state.',
  'experimental.enableServiceMap.description': SERVICE_MAP_LABEL,
  'experimental.enableServiceMap.value': false,
} as unknown as Configuration;

const serviceMapCheckbox = () =>
  document.getElementById(EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP);

describe('ExperimentalPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    store.set(configurationAtom, configuration);
  });

  test('exposes the enableServiceMap item by default', () => {
    render(<ExperimentalPage />);

    expect(serviceMapCheckbox()).not.toBeNull();
    expect(screen.getByText(SERVICE_MAP_LABEL)).toBeDefined();
  });

  test('hides the enableServiceMap item when the renderer opts out', () => {
    render(<ExperimentalPage showEnableServiceMap={false} />);

    expect(serviceMapCheckbox()).toBeNull();
    expect(screen.queryByText(SERVICE_MAP_LABEL)).toBeNull();
  });

  // showEnableServiceMap=false는 절대적이다. 항목을 그릴지와 체크 상태를 정하는 값은 다른
  // 것이므로, configuration이 켜 놓았든 localStorage에 저장된 값이 있든 나오면 안 된다.
  test('stays hidden even when configuration has the value on', () => {
    store.set(configurationAtom, {
      ...configuration,
      'experimental.enableServiceMap.value': true,
    } as unknown as Configuration);

    render(<ExperimentalPage showEnableServiceMap={false} />);

    expect(serviceMapCheckbox()).toBeNull();
    expect(screen.queryByText(SERVICE_MAP_LABEL)).toBeNull();
  });

  test('stays hidden even when the user had already stored an override', () => {
    window.localStorage.setItem(EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP, 'true');

    render(<ExperimentalPage showEnableServiceMap={false} />);

    expect(serviceMapCheckbox()).toBeNull();
    expect(screen.queryByText(SERVICE_MAP_LABEL)).toBeNull();
  });

  // 다른 항목을 눌러 리렌더가 일어나도 숨긴 항목이 되살아나지 않아야 한다.
  test('stays hidden across a re-render caused by toggling another item', () => {
    render(<ExperimentalPage showEnableServiceMap={false} />);

    const realTime = document.getElementById(
      EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVER_MAP_REAL_TIME,
    ) as HTMLElement;
    fireEvent.click(realTime);

    expect(serviceMapCheckbox()).toBeNull();
    expect(screen.queryByText(SERVICE_MAP_LABEL)).toBeNull();
  });

  test('the other experimental items stay put either way', () => {
    const { unmount } = render(<ExperimentalPage showEnableServiceMap={false} />);

    expect(
      document.getElementById(EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVER_MAP_REAL_TIME),
    ).not.toBeNull();
    expect(
      document.getElementById(EXPERIMENTAL_CONFIG_KEYS.USE_STATISTICS_AGENT_STATE),
    ).not.toBeNull();
    unmount();

    render(<ExperimentalPage />);

    expect(
      document.getElementById(EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVER_MAP_REAL_TIME),
    ).not.toBeNull();
    expect(
      document.getElementById(EXPERIMENTAL_CONFIG_KEYS.USE_STATISTICS_AGENT_STATE),
    ).not.toBeNull();
  });
});
