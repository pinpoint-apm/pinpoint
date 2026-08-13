import { act, renderHook } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { Edge } from '@pinpoint-fe/server-map';
import { serverMapDataAtom } from '@pinpoint-fe/ui/src/atoms';
import { FilteredMapType as FilteredMap, GetServerMap } from '@pinpoint-fe/ui/src/constants';

/**
 * 훅이 components 배럴에서 가져오는 두 값만 대신 세운다. 배럴을 그대로 불러오면 echarts를
 * 끌고 와 jest(CJS)에서 로드되지 않는다. 필터 형태를 만드는 규칙(`getDefaultFilters`)은
 * 그쪽 테스트가 보고, 여기서는 그 결과가 어떤 경로로 열리는지를 본다.
 */
const FILTER_TRANSACTION = 1;
const FILTER_STATE: FilteredMap.FilterState = {
  fromApplication: 'FRONT',
  fromServiceType: 'TOMCAT',
  toApplication: 'ACL-PORTAL-DEV',
  toServiceType: 'SPRING_BOOT',
  transactionResult: null,
  applicationName: '',
  serviceType: '',
  agentName: '',
  responseFrom: 0,
  responseTo: 'max',
  url: '',
  fromAgentName: '',
  toAgentName: '',
};

/** 기준 application을 고를 수 없는 필터. servicemap의 service group 링크가 이렇게 된다. */
const EMPTY_FILTER_STATE: FilteredMap.FilterState = {
  ...FILTER_STATE,
  fromApplication: '',
  fromServiceType: '',
  toApplication: '',
  toServiceType: '',
};

let mockFilterState = FILTER_STATE;

jest.mock('@pinpoint-fe/ui/src/components', () => ({
  SERVERMAP_MENU_FUNCTION_TYPE: { FILTER_TRANSACTION: 1, FILTER_WIZARD: 2, MERGE: 3 },
  getDefaultFilters: () => mockFilterState,
}));

import { useServerMapOnClickMenuItem } from './useServerMapOnClickMenuItem';

const FROM = '2023-11-10-14-30-00';
const TO = '2023-11-10-15-00-00';

const LINK_KEY = 'FRONT^TOMCAT~ACL-PORTAL-DEV^SPRING_BOOT';
const edge = { id: LINK_KEY, source: 'FRONT^TOMCAT', target: 'ACL-PORTAL-DEV^SPRING_BOOT' } as Edge;

/** 클릭한 링크가 map 데이터에 있어야 hint(outRpcList)와 sourceIsWas 판단이 이뤄진다. */
const setServerMapData = (sourceIsWas: boolean) => {
  const data = {
    applicationMapData: {
      nodeDataArray: [],
      linkDataArray: [
        {
          key: LINK_KEY,
          sourceInfo: {
            applicationName: 'FRONT',
            nodeCategory: sourceIsWas
              ? GetServerMap.NodeCategory.SERVER
              : GetServerMap.NodeCategory.UNKNOWN_GROUP,
          },
          targetInfo: {
            applicationName: 'ACL-PORTAL-DEV',
            nodeCategory: GetServerMap.NodeCategory.SERVER,
          },
          filter: { outRpcList: [] },
        },
      ],
    },
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } as any;

  act(() => getDefaultStore().set(serverMapDataAtom, data));
};

const clickFilterTransaction = (serviceName?: string) => {
  const { result } = renderHook(() =>
    useServerMapOnClickMenuItem({ from: FROM, to: TO, serviceName }),
  );

  result.current(FILTER_TRANSACTION, edge);
};

const openFilteredMap = (serviceName?: string) => {
  clickFilterTransaction(serviceName);

  return (window.open as jest.Mock).mock.calls[0][0] as string;
};

describe('useServerMapOnClickMenuItem', () => {
  beforeEach(() => {
    window.open = jest.fn();
    mockFilterState = FILTER_STATE;
    setServerMapData(true);
  });

  afterEach(() => {
    jest.clearAllMocks();
    act(() => getDefaultStore().set(serverMapDataAtom, undefined));
  });

  // servermap에서 넘어온 경로. servicemap이 들어오기 전과 형태가 같아야 한다.
  test('opens the filteredMap without a service segment when no service name is given', () => {
    const url = openFilteredMap();

    expect(url.startsWith(`/filteredMap/FRONT@TOMCAT?from=${FROM}&to=${TO}`)).toBe(true);
    expect(url).toContain('filter=');
  });

  // servicemap에서 넘어온 경로. 새 탭으로 열리므로 어떤 service를 보던 중이었는지 URL에 남아야 한다.
  test('carries the service name as its own segment when given', () => {
    const url = openFilteredMap('DEFAULT');

    expect(url.startsWith(`/filteredMap/DEFAULT/FRONT@TOMCAT?from=${FROM}&to=${TO}`)).toBe(true);
    expect(url).toContain('filter=');
  });

  // 출발지가 WAS가 아니면 도착지 application이 기준이 된다. serviceName이 붙어도 그 규칙은 같다.
  test('keeps picking the application by sourceIsWas', () => {
    setServerMapData(false);

    expect(openFilteredMap()).toContain('/filteredMap/ACL-PORTAL-DEV@SPRING_BOOT');
  });

  // 기준 application이 없으면 filteredMap은 조회를 못 한다. 빈 화면을 새 탭으로 열지 않는다.
  test('does not open anything when no application can be picked', () => {
    mockFilterState = EMPTY_FILTER_STATE;

    clickFilterTransaction('DEFAULT');

    expect(window.open).not.toHaveBeenCalled();
  });
});
