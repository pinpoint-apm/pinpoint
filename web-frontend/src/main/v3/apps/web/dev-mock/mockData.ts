/**
 * [MOCK #10497] 로컬에서 cross-service servicemap을 재현하기 위한 임시 mock 데이터.
 *
 * 이슈 #10497의 상황을 로컬 저장소에 만들어 두기 위한 것이다.
 *
 *   (A service) -> (B service)
 *
 * A service의 servicemap에 B service가 group 노드로 함께 그려지고, group을 펼쳐 `b-1`을 고르면
 * 우측 ChartsBoard의 조회가 `pServiceName: B`로 나가야 한다. 로컬 저장소에는 보통 service가
 * 하나뿐이라 이 상황 자체를 만들 수 없어서 map 응답을 통째로 mock한다.
 *
 * 조회 응답에는 **요청에 실려 온 pServiceName을 그대로 박아서** 내려준다(서버 목록의 agent 이름).
 * 화면만 보고도 어떤 service로 요청이 나갔는지 알 수 있게 하기 위한 것이다.
 * 고친 뒤: `b-1`을 고르면 `pServiceName=B`. 고치기 전: 화면의 service인 `pServiceName=A`.
 *
 * 이 파일이 있는 `dev-mock/` 디렉터리는 통째로 지울 수 있다. 지우는 방법은 README.md 참고.
 */

/* eslint-disable @typescript-eslint/no-explicit-any */

/** map을 그리는(= 화면에서 보고 있는) service */
export const VIEWING_SERVICE = 'A';
/** map에 group 노드로 함께 그려지는 다른 service */
export const OTHER_SERVICE = 'B';

export const MOCK_SERVICES = [VIEWING_SERVICE, OTHER_SERVICE];

const VIEWING_APPS = ['a-1', 'a-2'];
const OTHER_APPS = ['b-1', 'b-2'];

/** mock이 가로챌 application. 그 외 application 조회는 실제 백엔드로 그대로 넘긴다. */
export const MOCK_APPLICATIONS = [...VIEWING_APPS, ...OTHER_APPS];

const TOMCAT = { serviceType: 'TOMCAT', serviceTypeCode: 1010, nodeCategory: 'SERVER' };
const USER = { serviceType: 'USER', serviceTypeCode: 2, nodeCategory: 'USER' };

type NodeType = typeof TOMCAT;

/** 같은 입력이면 항상 같은 수치가 나오도록 하는 결정적 seed. service마다 수치가 달라 보이게 한다. */
const seedOf = (...parts: string[]) =>
  parts
    .join('^')
    .split('')
    .reduce((acc, char) => (acc * 31 + char.charCodeAt(0)) % 89, 7) + 5;

const makeHistogram = (seed: number) => ({
  '1s': seed * 12,
  '3s': seed * 4,
  '5s': seed * 2,
  Slow: seed,
  Error: Math.floor(seed / 3),
});

const sumOf = (histogram: Record<string, number>) =>
  Object.values(histogram).reduce((acc, value) => acc + value, 0);

const makeResponseStatistics = (seed: number) => ({
  Tot: seed * 19,
  Sum: seed * 19 * (80 + seed),
  Avg: 80 + seed,
  Max: 300 + seed * 7,
});

const HISTOGRAM_KEYS = ['1s', '3s', '5s', 'Slow', 'Error'];

const makeTimeSeriesHistogram = (seed: number, timestamps: number[]) =>
  HISTOGRAM_KEYS.map((key, keyIndex) => ({
    key,
    values: timestamps.map(
      (_, index) => Math.round(seed / (keyIndex + 1)) + ((index * (seed + keyIndex)) % 5),
    ),
  }));

const makeApdex = (seed: number) => {
  const totalSamples = seed * 19;
  const satisfiedCount = Math.round(totalSamples * 0.8);
  const toleratingCount = Math.round(totalSamples * 0.1);

  return {
    apdexScore: Number(((satisfiedCount + toleratingCount / 2) / totalSamples).toFixed(2)),
    apdexFormula: { satisfiedCount, toleratingCount, totalSamples },
  };
};

/** 지정한 구간을 균등하게 나눈 timestamp. timeSeries 값 배열의 길이 기준이 된다. */
export const makeTimestamps = (from: number, to: number, count = 20) => {
  const step = Math.max(1, Math.floor((to - from) / count));
  return Array.from({ length: count }, (_, index) => from + step * index);
};

const makeAppNode = (
  serviceName: string,
  applicationName: string,
  nodeType: NodeType,
  timestamps: number[],
) => {
  const seed = seedOf(serviceName, applicationName);
  const histogram = makeHistogram(seed);
  const agentId = `${applicationName}-agent`;

  return {
    type: 'app',
    // servicemap의 노드 key는 3단이다: serviceName^applicationName^serviceType
    key: `${serviceName}^${applicationName}^${nodeType.serviceType}`,
    // 통계 API가 쓰는 2단 key.
    nodeKey: `${applicationName}^${nodeType.serviceType}`,
    serviceKey: serviceName,
    serviceName,
    applicationName,
    serviceType: nodeType.serviceType,
    serviceTypeCode: nodeType.serviceTypeCode,
    nodeCategory: nodeType.nodeCategory,
    isQueue: false,
    isAuthorized: true,
    totalCount: sumOf(histogram),
    errorCount: histogram.Error,
    slowCount: histogram.Slow,
    hasAlert: false,
    responseStatistics: makeResponseStatistics(seed),
    histogram,
    apdex: makeApdex(seed),
    timeSeriesHistogram: makeTimeSeriesHistogram(seed, timestamps),
    instanceCount: 1,
    instanceErrorCount: 0,
    agents: [{ id: agentId, name: agentId }],
  };
};

const infoOf = (node: any) => ({
  serviceName: node.serviceName,
  applicationName: node.applicationName,
  serviceType: node.serviceType,
  serviceTypeCode: node.serviceTypeCode,
  nodeCategory: node.nodeCategory,
});

const makeAppLink = (source: any, target: any, timestamps: number[]) => {
  const seed = seedOf(source.applicationName, target.applicationName);
  const histogram = makeHistogram(seed);

  return {
    type: 'app',
    key: `${source.key}~${target.key}`,
    linkKey: `${source.nodeKey}~${target.nodeKey}`,
    from: source.key,
    to: target.key,
    fromAgents: source.agents,
    toAgents: target.agents,
    sourceInfo: infoOf(source),
    targetInfo: infoOf(target),
    filter: {
      serviceName: source.serviceName,
      applicationName: source.applicationName,
      serviceTypeCode: source.serviceTypeCode,
      serviceTypeName: source.serviceType,
    },
    totalCount: sumOf(histogram),
    errorCount: histogram.Error,
    slowCount: histogram.Slow,
    responseStatistics: makeResponseStatistics(seed),
    histogram,
    timeSeriesHistogram: makeTimeSeriesHistogram(seed, timestamps),
    hasAlert: false,
  };
};

/**
 * `/api/servermap/serviceMap` 응답.
 *
 * A service는 보고 있는 service라 펼쳐진 app 노드로, B service는 group 노드 하나로 내려온다.
 * (백엔드 `ServiceMapViewBuilder`의 `expandedServiceNames`와 같은 규칙)
 */
export const makeServiceMapResponse = (from: number, to: number) => {
  const timestamps = makeTimestamps(from, to);

  const user = makeAppNode(VIEWING_SERVICE, VIEWING_APPS[0], USER, timestamps);
  const a1 = makeAppNode(VIEWING_SERVICE, VIEWING_APPS[0], TOMCAT, timestamps);
  const a2 = makeAppNode(VIEWING_SERVICE, VIEWING_APPS[1], TOMCAT, timestamps);
  const b1 = makeAppNode(OTHER_SERVICE, OTHER_APPS[0], TOMCAT, timestamps);
  const b2 = makeAppNode(OTHER_SERVICE, OTHER_APPS[1], TOMCAT, timestamps);

  const groupLinks = [makeAppLink(a1, b1, timestamps), makeAppLink(a1, b2, timestamps)];
  const groupTotal = groupLinks.reduce((acc, link) => acc + link.totalCount, 0);

  return {
    applicationMapData: {
      range: {
        from,
        to,
        fromDateTime: new Date(from).toISOString(),
        toDateTime: new Date(to).toISOString(),
      },
      timestamp: timestamps,
      nodeDataArray: [
        user,
        a1,
        a2,
        // 다른 service는 group 노드 하나로 접혀서 온다. key가 serviceName 하나뿐이고
        // 자식 노드는 nodes에 담겨 온다(ServiceGroupNodeView).
        { key: OTHER_SERVICE, type: 'service', serviceName: OTHER_SERVICE, nodes: [b1, b2] },
      ],
      linkDataArray: [
        makeAppLink(user, a1, timestamps),
        makeAppLink(a1, a2, timestamps),
        // 한쪽 끝이 접힌 service라 group 링크로 온다. 자식 링크는 links에 담겨 온다.
        {
          key: `${a1.key}~${OTHER_SERVICE}`,
          from: a1.key,
          to: OTHER_SERVICE,
          type: 'service',
          totalCount: groupTotal,
          links: groupLinks,
        },
      ],
    },
  };
};

/** 요청에 실려 온 pServiceName을 화면에서 바로 읽을 수 있도록 만드는 표식. */
const receivedServiceLabel = (requestedServiceName: string) =>
  `pServiceName=${requestedServiceName || '(none)'}`;

/** `/api/histogram/statistics`, `/api/histogram/statistics/links` 응답. */
export const makeHistogramStatisticsResponse = (
  applicationName: string,
  requestedServiceName: string,
  from: number,
  to: number,
) => {
  const timestamps = makeTimestamps(from, to);
  // 응답 수치도 요청에 실려 온 service에 따라 달라지게 한다. 헤더가 잘못 나가면 숫자도 달라진다.
  const seed = seedOf(requestedServiceName, applicationName);
  const histogram = makeHistogram(seed);
  const agentId = `${applicationName}-agent`;
  const agentName = `${agentId} / ${receivedServiceLabel(requestedServiceName)}`;

  return {
    currentServerTime: to,
    histogram,
    responseStatistics: makeResponseStatistics(seed),
    timeSeriesHistogram: makeTimeSeriesHistogram(seed, timestamps),
    timestamp: timestamps,
    instanceCount: 1,
    instanceErrorCount: 0,
    agentHistogram: { [agentId]: histogram },
    agentResponseStatistics: { [agentId]: makeResponseStatistics(seed) },
    agentTimeSeriesHistogram: { [agentId]: makeTimeSeriesHistogram(seed, timestamps) },
    serverList: {
      [`${applicationName}-host`]: {
        name: `${applicationName}-host`,
        status: null,
        linkList: [],
        instanceList: {
          [agentId]: {
            hasInspector: false,
            name: agentId,
            agentName,
            serviceType: TOMCAT.serviceType,
            status: { code: 200, desc: 'Running' },
          },
        },
      },
    },
  };
};

/** `/api/agents/overview` 응답. */
export const makeAgentOverviewResponse = (
  applicationName: string,
  requestedServiceName: string,
  to: number,
) => {
  const agentId = `${applicationName}-agent`;

  return [
    {
      agentId,
      agentName: `${agentId} / ${receivedServiceLabel(requestedServiceName)}`,
      agentVersion: '3.0.0-MOCK',
      applicationName,
      container: false,
      hasInspector: false,
      hostName: `${applicationName}-host`,
      ip: '127.0.0.1',
      linkList: [],
      pid: 10000 + seedOf(applicationName),
      ports: '',
      serviceType: TOMCAT.serviceType,
      serviceTypeCode: TOMCAT.serviceTypeCode,
      startTimestamp: to - 60 * 60 * 1000,
      status: { agentId, eventTimestamp: to, state: { code: 200, desc: 'Running' } },
      vmVersion: '17',
    },
  ];
};

/** `/api/getApdexScore` 응답. */
export const makeApdexScoreResponse = (applicationName: string, requestedServiceName: string) =>
  makeApdex(seedOf(requestedServiceName, applicationName));

/** `/api/getScatterData` 응답. */
export const makeScatterResponse = (
  applicationName: string,
  requestedServiceName: string,
  from: number,
  to: number,
) => {
  const seed = seedOf(requestedServiceName, applicationName);
  const agentId = `${applicationName}-agent`;
  const dotCount = 60 + seed;
  // [x(timestamp), y(응답시간), agentIndex, transactionIndex, ?, type(0:success, 1:failed)]
  const dotList = Array.from({ length: dotCount }, (_, index) => {
    const x = from + Math.floor(((to - from) * index) / dotCount);
    const y = 30 + ((index * (seed + 7)) % (seed * 20));
    return [x, y, 1, index, 1, index % 9 === 0 ? 1 : 0];
  });

  return {
    currentServerTime: to,
    from,
    to,
    scatter: {
      metadata: { 1: [agentId, agentId, from] },
      dotList,
    },
    complete: true,
    resultFrom: from,
    resultTo: to,
  };
};

/** `/api/heatmap/applicationData` 응답. */
export const makeHeatmapResponse = (
  applicationName: string,
  requestedServiceName: string,
  from: number,
  to: number,
) => {
  const seed = seedOf(requestedServiceName, applicationName);
  const columns = makeTimestamps(from, to, 20);
  const rows = 10;
  const elapsedStep = 1000;

  let totalSuccessCount = 0;
  let totalFailCount = 0;

  const heatmapData = columns.map((timestamp, column) => ({
    column,
    timestamp,
    cellDataList: Array.from({ length: rows }, (_, row) => {
      const successCount = (seed + column * (row + 1)) % 40;
      const failCount = (seed + column + row) % 5;
      totalSuccessCount += successCount;
      totalFailCount += failCount;

      return {
        row,
        elapsedTime: (row + 1) * elapsedStep,
        successCount,
        failCount,
      };
    }),
  }));

  return {
    size: { width: columns.length, height: rows },
    summary: { totalSuccessCount, totalFailCount },
    heatmapData,
  };
};
