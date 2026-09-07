import React from 'react';
import { render } from '@testing-library/react';
import { GetServiceMap } from '@pinpoint-fe/ui/src/constants';

const mockUseGetServiceMap = jest.fn();
const mockIsDefaultService = jest.fn();
const mockQueryOption = {
  inbound: 3,
  outbound: 2,
  wasOnly: true,
  bidirectional: true,
};

jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  useGetServiceMap: (...args: unknown[]) => mockUseGetServiceMap(...args),
  useIsDefaultService: () => mockIsDefaultService(),
  useExperimentals: () => ({ statisticsAgentState: { value: true } }),
  useServerMapSearchParameters: () => ({
    application: { applicationName: 'a-1', serviceType: 'SPRING_BOOT' },
    dateRange: { from: new Date('2024-01-01T00:00:00'), to: new Date('2024-01-01T01:00:00') },
    queryOption: mockQueryOption,
  }),
}));

// cytoscape를 끌고 오는 실제 map은 이 테스트의 관심사가 아니다. 조회 파라미터만 본다.
jest.mock('../ServerMap/ServerMapCore', () => ({
  ServerMapCore: () => null,
}));

jest.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

import { ServiceMapFetcher } from './ServiceMapFetcher';

const getRequestedParams = (): GetServiceMap.Parameters => mockUseGetServiceMap.mock.calls[0][0];

describe('ServiceMapFetcher', () => {
  beforeEach(() => {
    mockUseGetServiceMap.mockReturnValue({ data: undefined, isLoading: false, error: null });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  // DEFAULT service는 고른 application 하나를 기준으로 map을 그리므로 조회 조건 박스가 있고,
  // 거기서 정한 값이 그대로 조회에 실려야 한다(servermap과 같다).
  test('sends the search option of the DEFAULT service', () => {
    mockIsDefaultService.mockReturnValue(true);

    render(<ServiceMapFetcher />);

    expect(getRequestedParams()).toMatchObject({
      applicationName: 'a-1',
      serviceTypeName: 'SPRING_BOOT',
      calleeRange: 3,
      callerRange: 2,
      wasOnly: true,
      bidirectional: true,
    });
  });

  // DEFAULT가 아닌 service는 기준 application이 없어 조회 조건 박스도 없다. 경로에 남아 있는
  // 값이 있더라도 싣지 않아야 백엔드가 지금까지와 같은 고정값으로 map을 그린다.
  test('omits the search option when the service has no base application', () => {
    mockIsDefaultService.mockReturnValue(false);

    render(<ServiceMapFetcher />);

    const params = getRequestedParams();
    expect(params.calleeRange).toBeUndefined();
    expect(params.callerRange).toBeUndefined();
    expect(params.wasOnly).toBeUndefined();
    expect(params.bidirectional).toBeUndefined();
    expect(params.applicationName).toBeUndefined();
  });
});
