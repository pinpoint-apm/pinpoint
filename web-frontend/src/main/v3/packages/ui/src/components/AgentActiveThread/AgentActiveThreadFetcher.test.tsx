import { render, act, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { Provider, createStore } from 'jotai';
import { serverMapCurrentTargetAtom, serverMapDataAtom } from '@pinpoint-fe/ui/src/atoms';
import { GetServerMap } from '@pinpoint-fe/ui/src/constants';

// 이 테스트의 관심사는 소켓에 무엇이 실려 나가는지, 그리고 소켓을 언제 열고 닫는지다. 차트/표/설정
// 폼은 그와 무관하므로 모킹해 두고 렌더 트리를 가볍게 유지한다.
jest.mock('./AgentActiveThreadView', () => ({ AgentActiveThreadView: () => null }));
jest.mock('./AgentActiveThreadSkeleton', () => ({ AgentActiveThreadSkeleton: () => null }));
jest.mock('./AgentActiveSetting', () => ({
  AgentActiveSetting: () => null,
  DefaultValue: {},
}));
jest.mock('@pinpoint-fe/ui/src/components/HelpPopover', () => ({ HelpPopover: () => null }));
jest.mock('react-i18next', () => ({ useTranslation: () => ({ t: (key: string) => key }) }));

// jest.mock 팩토리는 `mock` 접두사가 붙은 변수만 참조할 수 있다.
let mockScreenServiceName: string | undefined;
jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  useTimezone: () => ['UTC'],
  useServiceNameForLink: () => mockScreenServiceName,
}));

import { AgentActiveThreadFetcher } from './AgentActiveThreadFetcher';

type Listener = (event: unknown) => void;

const sockets: MockWebSocket[] = [];

class MockWebSocket {
  static readonly CONNECTING = 0;
  static readonly OPEN = 1;
  static readonly CLOSING = 2;
  static readonly CLOSED = 3;

  readyState = MockWebSocket.CONNECTING;
  send = jest.fn();
  close = jest.fn();
  private listeners: Record<string, Listener[]> = {};

  constructor() {
    sockets.push(this);
  }

  addEventListener(type: string, listener: Listener) {
    (this.listeners[type] ||= []).push(listener);
  }

  dispatch(type: string, event: unknown = {}) {
    (this.listeners[type] || []).forEach((listener) => listener(event));
  }
}

const latestSocket = () => sockets[sockets.length - 1];

/** 소켓이 1초마다 밀어 주는 응답. */
const pushResponse = (socket: MockWebSocket, timeStamp: number) => {
  act(() => {
    socket.dispatch('message', {
      data: JSON.stringify({ type: 'RESPONSE', result: { timeStamp } }),
    });
  });
};

/**
 * 소켓이 열리고 첫 응답이 도착한 상태를 만든다.
 *
 * `readyState`를 먼저 OPEN으로 바꾼다 — 실제 브라우저도 open 이벤트가 불릴 때 이미 OPEN이고,
 * 컴포넌트는 그 시점에 첫 요청을 보낸다.
 */
const openSocket = (socket = latestSocket()) => {
  act(() => {
    socket.readyState = MockWebSocket.OPEN;
    socket.dispatch('open');
  });
  pushResponse(socket, 0);
};

const activeThreadCountRequests = (socket = latestSocket()) =>
  socket.send.mock.calls
    .map(([payload]) => JSON.parse(payload as string))
    .filter((message) => message.command === 'activeThreadCount');

const sentParameters = (socket = latestSocket()) => {
  const requests = activeThreadCountRequests(socket);
  return requests[requests.length - 1]?.parameters;
};

type MapNode = { key: string; applicationName: string; serviceType: string };

const APP_NODE = {
  key: 'A^a-1^TOMCAT',
  applicationName: 'a-1',
  serviceType: 'TOMCAT',
  nodeCategory: GetServerMap.NodeCategory.SERVER,
};

/**
 * USER 노드. **applicationName이 진입점 WAS와 같다**(`a-1`) — mock(`dev-mock/mockData.ts`)과
 * 실제 응답이 모두 이 형태다. 그래서 이름만으로는 WAS와 구별되지 않는다.
 */
const USER_NODE = {
  key: 'A^a-1^USER',
  applicationName: 'a-1',
  serviceType: 'USER',
  nodeCategory: GetServerMap.NodeCategory.USER,
};

/** servicemap 실시간 보기 경로. 로더가 serviceName 세그먼트를 항상 채워 넣는다. */
const SERVICE_MAP_REALTIME_PATH = '/serviceMap/realtime/A/a-1@TOMCAT';
/** servermap 실시간 보기 경로. 이 화면은 serviceName을 경로에 싣지 않는다. */
const SERVER_MAP_REALTIME_PATH = '/serverMap/realtime/a-1@TOMCAT';

const mount = ({
  nodes = [APP_NODE],
  selected = APP_NODE as MapNode | undefined,
  serviceName,
  path = SERVICE_MAP_REALTIME_PATH,
}: {
  nodes?: Partial<GetServerMap.NodeData>[];
  selected?: MapNode;
  serviceName?: string;
  path?: string;
} = {}) => {
  const store = createStore();
  store.set(serverMapDataAtom, {
    applicationMapData: { nodeDataArray: nodes, linkDataArray: [] },
  } as unknown as GetServerMap.Response);
  if (selected) {
    store.set(serverMapCurrentTargetAtom, { id: selected.key, ...selected, type: 'node' });
  }

  render(
    <MemoryRouter initialEntries={[path]}>
      <Provider store={store}>
        <AgentActiveThreadFetcher serviceName={serviceName} />
      </Provider>
    </MemoryRouter>,
  );

  /** map에서 노드를 클릭한 것과 같은 상태를 만든다. */
  const clickNode = (node: MapNode) =>
    act(() => {
      store.set(serverMapCurrentTargetAtom, { id: node.key, ...node, type: 'node' });
    });

  /** 헤더의 핀 버튼을 눌러 고정을 푼다. */
  const unpin = () =>
    act(() => {
      fireEvent.click(screen.getAllByRole('button')[0]);
    });

  return { store, clickNode, unpin };
};

describe('AgentActiveThreadFetcher', () => {
  const originalWebSocket = global.WebSocket;

  beforeAll(() => {
    global.WebSocket = MockWebSocket as unknown as typeof WebSocket;
  });

  afterAll(() => {
    global.WebSocket = originalWebSocket;
  });

  beforeEach(() => {
    sockets.length = 0;
    mockScreenServiceName = undefined;
  });

  it('조회 대상의 applicationName과 serviceType을 실어 보낸다', () => {
    mockScreenServiceName = 'A';
    mount();
    openSocket();

    expect(sentParameters()).toMatchObject({ applicationName: 'a-1', serviceType: 'TOMCAT' });
  });

  it('넘어온 serviceName이 있으면 그것으로 조회한다', () => {
    mockScreenServiceName = 'screen-service';
    mount({ serviceName: 'target-service' });
    openSocket();

    expect(sentParameters()).toEqual({
      applicationName: 'a-1',
      serviceType: 'TOMCAT',
      serviceName: 'target-service',
    });
  });

  it('넘어온 serviceName이 없으면 화면의 service로 조회한다', () => {
    mockScreenServiceName = 'screen-service';
    mount();
    openSocket();

    expect(sentParameters()).toMatchObject({ serviceName: 'screen-service' });
  });

  // enableServiceMap이 꺼져 있으면 useServiceNameForLink가 undefined를 반환한다. 그때는 예전과
  // 같은 형태로 나가야 한다 — service 개념이 없는 저장소에 service 이름이 새어 나가지 않도록.
  it('service를 알 수 없으면 예전과 같은 메시지를 보낸다', () => {
    mount();
    openSocket();

    expect(sentParameters()).toEqual({ applicationName: 'a-1' });
  });

  /**
   * servermap 실시간 보기는 경로에 serviceName을 싣지 않는 화면이다. 이 화면의 메시지는 예전과
   * 완전히 같아야 한다 — `useServiceNameForLink`의 전역 선택값 폴백을 그대로 쓰면 여기서도
   * service 정보가 실려 나간다.
   */
  it('servermap 실시간 보기는 applicationName만 보낸다', () => {
    mockScreenServiceName = 'screen-service';
    mount({ serviceName: 'target-service', path: SERVER_MAP_REALTIME_PATH });
    openSocket();

    expect(sentParameters()).toEqual({ applicationName: 'a-1' });
  });

  it('servicemap 실시간 보기는 service 정보를 함께 보낸다', () => {
    mockScreenServiceName = 'A';
    mount({ serviceName: 'A', path: SERVICE_MAP_REALTIME_PATH });
    openSocket();

    expect(sentParameters()).toEqual({
      applicationName: 'a-1',
      serviceType: 'TOMCAT',
      serviceName: 'A',
    });
  });

  // service group 노드는 기준 application이 없는데도 applicationName에 serviceName이 들어 있다
  // (flattenServiceMapResponse). 그대로 보내면 service를 application인 것처럼 묻게 된다.
  it('service group 노드를 골랐으면 요청을 보내지 않는다', () => {
    const groupNode = {
      key: 'B',
      serviceName: 'B',
      applicationName: 'B',
      serviceType: 'TOMCAT',
      nodeCategory: GetServerMap.NodeCategory.SERVER,
      subNodes: [{ key: 'B^b-1^TOMCAT', applicationName: 'b-1', serviceType: 'TOMCAT' }],
    } as unknown as GetServerMap.NodeData;

    mount({
      nodes: [groupNode],
      selected: { key: 'B', applicationName: 'B', serviceType: 'TOMCAT' },
      serviceName: 'B',
    });
    openSocket();

    expect(sentParameters()).toBeUndefined();
  });

  /**
   * WAS가 아닌 노드도 **고른 노드로 요청은 보낸다**(예전 동작). 액티브 스레드는 WAS만 가지므로
   * 차트 대신 안내 문구를 띄우는 것이 이 판단의 몫이다.
   *
   * USER 노드는 진입점 WAS와 applicationName이 같을 수 있어, 안내 없이 차트를 그리면 WAS의
   * 액티브 스레드를 그 노드의 것으로 착각하게 된다.
   */
  it('WAS가 아닌 노드(USER)도 그 노드로 요청하고 WAS가 아니라고 알려준다', () => {
    mockScreenServiceName = 'A';
    mount({ nodes: [USER_NODE, APP_NODE], selected: USER_NODE, serviceName: 'A' });
    openSocket();

    expect(sentParameters()).toMatchObject({ applicationName: 'a-1', serviceType: 'USER' });
    expect(screen.getByText('SERVER_MAP.REAL_TIME.NOT_WAS')).toBeTruthy();
  });

  // 핀을 풀지 않고도 그 다음에 고른 WAS가 대상이 되어야 한다. WAS가 아닌 노드를 대상으로
  // 삼아 버리면 핀이 걸린 채로 고정되어, 핀을 해제할 때까지 이 패널이 멈춘다.
  it('WAS가 아닌 노드를 거쳐도 그 다음에 고른 WAS가 대상이 된다', () => {
    mockScreenServiceName = 'A';
    const { clickNode } = mount({
      nodes: [USER_NODE, APP_NODE],
      selected: USER_NODE,
      serviceName: 'A',
    });

    clickNode(APP_NODE);
    openSocket();

    expect(sentParameters()).toEqual({
      applicationName: 'a-1',
      serviceType: 'TOMCAT',
      serviceName: 'A',
    });
  });

  /**
   * 핀을 풀면 조회 대상도 고른 것을 따라간다. 대상이 없어져도 **소켓은 그대로 열어 둔다** —
   * 연결을 화면이 열려 있는 동안 유지하는 것이 기존 동작이다. 구독을 취소하는 명령이 없어서
   * 직전 WAS의 응답은 계속 오지만, 새 요청을 보내지 않고 화면에도 쓰지 않는다.
   */
  it('핀을 푼 뒤 WAS가 아닌 노드를 골라도 소켓을 닫지 않는다', () => {
    const { clickNode, unpin } = mount({
      nodes: [USER_NODE, APP_NODE],
      selected: APP_NODE,
      serviceName: 'A',
    });
    const socket = latestSocket();
    openSocket(socket);

    unpin();
    clickNode(USER_NODE);
    pushResponse(socket, 2);

    expect(socket.close).not.toHaveBeenCalled();
    expect(screen.getByText('SERVER_MAP.REAL_TIME.NOT_WAS')).toBeTruthy();
  });

  /**
   * 핀이 걸려 있어도 WAS를 고르면 그 노드로 조회한다. 클릭한 노드의 액티브 스레드를 보여주는
   * 것이 이 패널의 목적이라, 핀 때문에 이전 노드의 응답만 계속 오면 안 된다.
   *
   * 이때 세 값은 한 벌로 갈려야 한다 — 하나만 새 노드의 값이 되면 그 service에 없는
   * application을 묻는 요청이 된다.
   */
  it('핀이 걸려 있어도 WAS를 고르면 그 노드로 조회한다', () => {
    mockScreenServiceName = 'service-a';
    const otherWas = {
      key: 'A^a-2^SPRING_BOOT',
      applicationName: 'a-2',
      serviceType: 'SPRING_BOOT',
      nodeCategory: GetServerMap.NodeCategory.SERVER,
    };
    const { clickNode } = mount({
      nodes: [APP_NODE, otherWas],
      selected: APP_NODE,
      serviceName: 'service-a',
    });
    const socket = latestSocket();
    openSocket(socket);

    clickNode(otherWas);

    expect(sentParameters(socket)).toEqual({
      applicationName: 'a-2',
      serviceType: 'SPRING_BOOT',
      serviceName: 'service-a',
    });
  });

  // 안내 문구는 핀과 무관하다. 핀이 걸려 있을 때 이 판단을 건너뛰면, WAS가 아닌 노드를 고른 채
  // 직전 WAS의 액티브 스레드가 그려진다.
  it('핀이 걸려 있어도 WAS가 아닌 노드를 고르면 WAS가 아니라고 알려준다', () => {
    mockScreenServiceName = 'A';
    const { clickNode } = mount({
      nodes: [APP_NODE, USER_NODE],
      selected: APP_NODE,
      serviceName: 'A',
    });
    const socket = latestSocket();
    openSocket(socket);

    clickNode(USER_NODE);

    expect(screen.getByText('SERVER_MAP.REAL_TIME.NOT_WAS')).toBeTruthy();
  });

  /**
   * 조회 대상이 될 수 없는 것(service group)을 거쳐 **같은 노드로 돌아왔을 때**도 요청이 다시
   * 나가야 한다. 대상 값이 그대로라 "보낼 필요 없다"고 건너뛰면 그 사이 구독이 끊겼을 때 되살릴
   * 방법이 없고, WAS가 하나뿐인 map에서는(진입 시 이미 그 WAS가 대상) 클릭해도 요청이 한 번도
   * 나가지 않는 상태가 된다.
   */
  it('조회할 수 없는 대상을 거쳐 같은 노드로 돌아오면 요청을 다시 보낸다', () => {
    mockScreenServiceName = 'A';
    const groupNode = {
      key: 'B',
      serviceName: 'B',
      applicationName: 'B',
      serviceType: 'TOMCAT',
      nodeCategory: GetServerMap.NodeCategory.SERVER,
      subNodes: [{ key: 'B^b-1^TOMCAT', applicationName: 'b-1', serviceType: 'TOMCAT' }],
    } as unknown as GetServerMap.NodeData;
    const { clickNode } = mount({
      nodes: [APP_NODE, groupNode],
      selected: APP_NODE,
      serviceName: 'A',
    });
    const socket = latestSocket();
    openSocket(socket);
    const requestsBefore = activeThreadCountRequests(socket).length;

    clickNode({ key: 'B', applicationName: 'B', serviceType: 'TOMCAT' });
    clickNode(APP_NODE);

    expect(activeThreadCountRequests(socket).length).toBe(requestsBefore + 1);
    expect(sentParameters(socket)).toMatchObject({ applicationName: 'a-1' });
  });

  // 반대로 핀을 눌렀을 뿐인데 같은 요청을 또 보내면, 서버가 'Already started' 에러를 남긴다.
  it('핀만 눌렀을 때는 같은 요청을 다시 보내지 않는다', () => {
    mockScreenServiceName = 'A';
    const { unpin } = mount({ serviceName: 'A' });
    const socket = latestSocket();
    openSocket(socket);
    const requestsBefore = activeThreadCountRequests(socket).length;

    unpin();

    expect(activeThreadCountRequests(socket).length).toBe(requestsBefore);
  });

  // 예기치 않게 끊기면(서버 재시작 등) 다시 붙고, 붙자마자 같은 대상으로 다시 구독한다.
  it('소켓이 끊기면 다시 붙어 같은 대상으로 조회한다', () => {
    mockScreenServiceName = 'A';
    mount({ serviceName: 'A' });
    const firstSocket = latestSocket();
    openSocket(firstSocket);

    act(() => {
      firstSocket.readyState = MockWebSocket.CLOSED;
      firstSocket.dispatch('close');
    });

    const reconnected = latestSocket();
    expect(reconnected).not.toBe(firstSocket);
    openSocket(reconnected);
    expect(sentParameters(reconnected)).toMatchObject({ applicationName: 'a-1' });
  });
});
