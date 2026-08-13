import { act, renderHook } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { serverMapDataAtom } from '@pinpoint-fe/ui/src/atoms';
import { FilteredMapType as FilteredMap, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { useFilterWizardOnClickApply } from './useFilterWizardOnClickApply';

const FROM = '2023-11-10-14-30-00';
const TO = '2023-11-10-15-00-00';

/** 노드에 필터를 걸었을 때의 형태. 그 application이 그대로 filteredMap의 기준이 된다. */
const nodeFilterState: FilteredMap.FilterState = {
  fromApplication: '',
  fromServiceType: '',
  toApplication: '',
  toServiceType: '',
  transactionResult: null,
  applicationName: 'ACL-PORTAL-DEV',
  serviceType: 'SPRING_BOOT',
  agentName: '',
  responseFrom: 0,
  responseTo: 'max',
  url: '',
  fromAgentName: '',
  toAgentName: '',
};

const apply = (serviceName?: string) => {
  const { result } = renderHook(() =>
    useFilterWizardOnClickApply({ from: FROM, to: TO, serviceName }),
  );

  result.current([nodeFilterState]);

  return (window.open as jest.Mock).mock.calls[0][0] as string;
};

describe('useFilterWizardOnClickApply', () => {
  beforeEach(() => {
    window.open = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
    act(() => getDefaultStore().set(serverMapDataAtom, undefined));
  });

  // servermap의 필터 위저드. servicemap이 들어오기 전과 형태가 같아야 한다.
  test('opens the filteredMap without a service segment when no service name is given', () => {
    const url = apply();

    expect(url.startsWith(`/filteredMap/ACL-PORTAL-DEV@SPRING_BOOT?from=${FROM}&to=${TO}`)).toBe(
      true,
    );
    expect(url).toContain('filter=');
  });

  // 기준 application이 없으면 filteredMap은 조회를 못 한다. 빈 화면을 새 탭으로 열지 않는다.
  // (servicemap의 service group 노드·링크에 필터를 걸었을 때)
  test('does not open anything when no application can be picked', () => {
    const { result } = renderHook(() =>
      useFilterWizardOnClickApply({ from: FROM, to: TO, serviceName: 'DEFAULT' }),
    );

    result.current([{ ...nodeFilterState, applicationName: '', serviceType: '' }]);

    expect(window.open).not.toHaveBeenCalled();
  });

  // servicemap의 필터 위저드. 새 탭으로 열리므로 service가 URL에 남아야 한다.
  test('carries the service name as its own segment when given', () => {
    const url = apply('DEFAULT');

    expect(
      url.startsWith(`/filteredMap/DEFAULT/ACL-PORTAL-DEV@SPRING_BOOT?from=${FROM}&to=${TO}`),
    ).toBe(true);
    expect(url).toContain('filter=');
  });

  /**
   * 링크(WAS→WAS)에 걸린 필터. 기준 application과 hint는 map에서 그 링크를 찾아 정해지는데,
   * link key 형식이 map API마다 다르다(servermap 2단, servicemap 3단).
   *
   * 못 찾으면 sourceIsWas가 undefined로 남아 출발지가 아니라 **도착지**가 기준이 되고 hint도
   * 비어버려서, 같은 링크인데 두 map의 URL이 달라진다.
   */
  describe('a filter on a WAS→WAS link', () => {
    const linkFilterState: FilteredMap.FilterState = {
      ...nodeFilterState,
      fromApplication: 'FRONT',
      fromServiceType: 'TOMCAT',
      toApplication: 'ACL-PORTAL-DEV',
      toServiceType: 'SPRING_BOOT',
      applicationName: '',
      serviceType: '',
    };

    const setLink = (key: string) => {
      const data = {
        applicationMapData: {
          nodeDataArray: [],
          linkDataArray: [
            {
              key,
              sourceInfo: {
                applicationName: 'FRONT',
                nodeCategory: GetServerMap.NodeCategory.SERVER,
              },
              targetInfo: {
                applicationName: 'ACL-PORTAL-DEV',
                nodeCategory: GetServerMap.NodeCategory.SERVER,
              },
              filter: { outRpcList: [{ rpc: '/api/a', rpcServiceTypeCode: 1234 }] },
            },
          ],
        },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } as any;

      act(() => getDefaultStore().set(serverMapDataAtom, data));
    };

    const applyLinkFilter = (serviceName?: string) => {
      const { result } = renderHook(() =>
        useFilterWizardOnClickApply({ from: FROM, to: TO, serviceName }),
      );

      result.current([linkFilterState]);

      return (window.open as jest.Mock).mock.calls[0][0] as string;
    };

    test('picks the source application and carries the hint (servermap, 2-part key)', () => {
      setLink('FRONT^TOMCAT~ACL-PORTAL-DEV^SPRING_BOOT');

      const url = applyLinkFilter();

      expect(url.startsWith('/filteredMap/FRONT@TOMCAT?')).toBe(true);
      expect(url).toContain('ACL-PORTAL-DEV');
      expect(url).toContain('hint=%7B%22ACL-PORTAL-DEV%22:%5B%22/api/a%22,1234%5D%7D');
    });

    // 3단 key에서도 같은 결정이 나와야 한다. serviceName 세그먼트만 다른 URL이어야 한다.
    test('produces the same decisions on a 3-part key (servicemap)', () => {
      setLink('DEFAULT^FRONT^TOMCAT~DEFAULT^ACL-PORTAL-DEV^SPRING_BOOT');

      const url = applyLinkFilter('DEFAULT');

      expect(url.startsWith('/filteredMap/DEFAULT/FRONT@TOMCAT?')).toBe(true);
      expect(url).toContain('hint=%7B%22ACL-PORTAL-DEV%22:%5B%22/api/a%22,1234%5D%7D');
    });

    // 두 map의 URL은 serviceName 세그먼트를 빼면 완전히 같아야 한다.
    test('differs from the servermap URL only by the service segment', () => {
      setLink('FRONT^TOMCAT~ACL-PORTAL-DEV^SPRING_BOOT');
      const serverMapUrl = applyLinkFilter();

      jest.clearAllMocks();
      window.open = jest.fn();

      setLink('DEFAULT^FRONT^TOMCAT~DEFAULT^ACL-PORTAL-DEV^SPRING_BOOT');
      const serviceMapUrl = applyLinkFilter('DEFAULT');

      expect(serviceMapUrl.replace('/filteredMap/DEFAULT/', '/filteredMap/')).toBe(serverMapUrl);
    });
  });
});
