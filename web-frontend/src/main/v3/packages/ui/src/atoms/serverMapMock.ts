import { GetServerMap } from '@pinpoint-fe/ui/src/constants';

export const serverMapData: GetServerMap.Response = {
  applicationMapData: {
    range: {
      from: 1699620228000,
      to: 1699620528000,
      fromDateTime: '2023-11-10 21:43:48',
      toDateTime: '2023-11-10 21:48:48',
    },
    timestamp: [
      1699620180000, 1699620240000, 1699620300000, 1699620360000, 1699620420000, 1699620480000,
    ],
    linkDataArray: [
      {
        key: 'app1_SPRING_BOOT^USER~app1^SPRING_BOOT',
        from: 'app1_SPRING_BOOT^USER',
        to: 'app1^SPRING_BOOT',
        toAgents: [
          {
            id: 'agent1',
            name: '',
          },
        ],
        sourceInfo: {
          applicationName: 'app1_SPRING_BOOT',
          serviceType: 'USER',
          serviceTypeCode: 2,
          isWas: false,
        },
        targetInfo: {
          applicationName: 'app1',
          serviceType: 'SPRING_BOOT',
          serviceTypeCode: 1210,
          isWas: true,
        },
        filter: {
          applicationName: 'app1',
          serviceTypeCode: 1210,
          serviceTypeName: 'SPRING_BOOT',
        },
        totalCount: 180,
        errorCount: 0,
        slowCount: 0,
        responseStatistics: {
          Tot: 180,
          Sum: 713,
          Avg: 3,
          Max: 0,
        },
        histogram: {
          '1s': 180,
          '3s': 0,
          '5s': 0,
          Slow: 0,
          Error: 0,
        },
        timeSeriesHistogram: [
          {
            key: '1s',
            values: [30, 30, 30, 30, 29, 31],
          },
          {
            key: '3s',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: '5s',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Slow',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Error',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Avg',
            values: [3, 4, 4, 3, 4, 3],
          },
          {
            key: 'Max',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Sum',
            values: [115, 120, 121, 116, 118, 123],
          },
          {
            key: 'Tot',
            values: [30, 30, 30, 30, 29, 31],
          },
        ],
        hasAlert: false,
      },
      {
        key: 'app1^SPRING_BOOT~acl^MYSQL',
        from: 'app1^SPRING_BOOT',
        to: 'acl^MYSQL',
        fromAgents: [
          {
            id: 'agent1',
            name: '',
          },
        ],
        sourceInfo: {
          applicationName: 'app1',
          serviceType: 'SPRING_BOOT',
          serviceTypeCode: 1210,
          isWas: true,
        },
        targetInfo: {
          applicationName: 'acl',
          serviceType: 'MYSQL',
          serviceTypeCode: 2101,
          isWas: false,
        },
        filter: {
          applicationName: 'app1',
          serviceTypeCode: 1210,
          serviceTypeName: 'SPRING_BOOT',
        },
        totalCount: 360,
        errorCount: 0,
        slowCount: 0,
        responseStatistics: {
          Tot: 360,
          Sum: 227,
          Avg: 0,
          Max: 0,
        },
        histogram: {
          '1s': 360,
          '3s': 0,
          '5s': 0,
          Slow: 0,
          Error: 0,
        },
        timeSeriesHistogram: [
          {
            key: '1s',
            values: [60, 60, 60, 60, 58, 62],
          },
          {
            key: '3s',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: '5s',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Slow',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Error',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Avg',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Max',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Sum',
            values: [36, 43, 43, 34, 32, 39],
          },
          {
            key: 'Tot',
            values: [60, 60, 60, 60, 58, 62],
          },
        ],
        hasAlert: false,
      },
      {
        key: 'app1^SPRING_BOOT~app2^REDIS',
        from: 'app1^SPRING_BOOT',
        to: 'app2^REDIS',
        fromAgents: [
          {
            id: 'agent1',
            name: '',
          },
        ],
        sourceInfo: {
          applicationName: 'app1',
          serviceType: 'SPRING_BOOT',
          serviceTypeCode: 1210,
          isWas: true,
        },
        targetInfo: {
          applicationName: 'app2',
          serviceType: 'REDIS',
          serviceTypeCode: 8201,
          isWas: false,
        },
        filter: {
          applicationName: 'app1',
          serviceTypeCode: 1210,
          serviceTypeName: 'SPRING_BOOT',
        },
        totalCount: 720,
        errorCount: 0,
        slowCount: 0,
        responseStatistics: {
          Tot: 720,
          Sum: 19,
          Avg: 0,
          Max: 0,
        },
        histogram: {
          '1s': 720,
          '3s': 0,
          '5s': 0,
          Slow: 0,
          Error: 0,
        },
        timeSeriesHistogram: [
          {
            key: '100ms',
            values: [120, 120, 120, 120, 116, 124],
          },
          {
            key: '300ms',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: '500ms',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Slow',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Error',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Avg',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Max',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Sum',
            values: [1, 3, 3, 4, 4, 4],
          },
          {
            key: 'Tot',
            values: [120, 120, 120, 120, 116, 124],
          },
        ],
        hasAlert: false,
      },
    ],
    nodeDataArray: [
      {
        key: 'app2^REDIS',
        applicationName: 'app2',
        category: 'REDIS',
        serviceType: 'REDIS',
        serviceTypeCode: 8201,
        isWas: false,
        isQueue: false,
        isAuthorized: true,
        totalCount: 720,
        errorCount: 0,
        slowCount: 0,
        hasAlert: false,
        responseStatistics: {
          Tot: 720,
          Sum: 19,
          Avg: 0,
          Max: 0,
        },
        histogram: {
          '1s': 720,
          '3s': 0,
          '5s': 0,
          Slow: 0,
          Error: 0,
        },
        apdexScore: 1,
        apdexFormula: {
          satisfiedCount: 720,
          toleratingCount: 0,
          totalSamples: 720,
        },
        timeSeriesHistogram: [
          {
            key: '100ms',
            values: [120, 120, 120, 120, 116, 124],
          },
          {
            key: '300ms',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: '500ms',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Slow',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Error',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Avg',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Max',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Sum',
            values: [1, 3, 3, 4, 4, 4],
          },
          {
            key: 'Tot',
            values: [120, 120, 120, 120, 116, 124],
          },
        ],
        instanceCount: 1,
        instanceErrorCount: 0,
        agents: [
          {
            id: 'UNKNOWN',
            name: '',
          },
        ],
      },
      {
        key: 'acl^MYSQL',
        applicationName: 'acl',
        category: 'MYSQL',
        serviceType: 'MYSQL',
        serviceTypeCode: 2101,
        isWas: false,
        isQueue: false,
        isAuthorized: true,
        totalCount: 360,
        errorCount: 0,
        slowCount: 0,
        hasAlert: false,
        responseStatistics: {
          Tot: 360,
          Sum: 227,
          Avg: 0,
          Max: 0,
        },
        histogram: {
          '1s': 360,
          '3s': 0,
          '5s': 0,
          Slow: 0,
          Error: 0,
        },
        apdexScore: 1,
        apdexFormula: {
          satisfiedCount: 360,
          toleratingCount: 0,
          totalSamples: 360,
        },
        timeSeriesHistogram: [
          {
            key: '1s',
            values: [60, 60, 60, 60, 58, 62],
          },
          {
            key: '3s',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: '5s',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Slow',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Error',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Avg',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Max',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Sum',
            values: [36, 43, 43, 34, 32, 39],
          },
          {
            key: 'Tot',
            values: [60, 60, 60, 60, 58, 62],
          },
        ],
        instanceCount: 1,
        instanceErrorCount: 0,
        agents: [
          {
            id: 'agent2',
            name: '',
          },
        ],
      },
      {
        key: 'app1_SPRING_BOOT^USER',
        applicationName: 'USER',
        category: 'USER',
        serviceType: 'USER',
        serviceTypeCode: 2,
        isWas: false,
        isQueue: false,
        isAuthorized: true,
        totalCount: 180,
        errorCount: 0,
        slowCount: 0,
        hasAlert: false,
        responseStatistics: {
          Tot: 180,
          Sum: 713,
          Avg: 3,
          Max: 0,
        },
        histogram: {
          '1s': 180,
          '3s': 0,
          '5s': 0,
          Slow: 0,
          Error: 0,
        },
        apdexScore: 1,
        apdexFormula: {
          satisfiedCount: 180,
          toleratingCount: 0,
          totalSamples: 180,
        },
        timeSeriesHistogram: [
          {
            key: '1s',
            values: [30, 30, 30, 30, 29, 31],
          },
          {
            key: '3s',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: '5s',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Slow',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Error',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Avg',
            values: [3, 4, 4, 3, 4, 3],
          },
          {
            key: 'Max',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Sum',
            values: [115, 120, 121, 116, 118, 123],
          },
          {
            key: 'Tot',
            values: [30, 30, 30, 30, 29, 31],
          },
        ],
        instanceCount: 0,
        instanceErrorCount: 0,
        agents: [],
      },
      {
        key: 'app1^SPRING_BOOT',
        applicationName: 'app1',
        category: 'SPRING_BOOT',
        serviceType: 'SPRING_BOOT',
        serviceTypeCode: 1210,
        isWas: true,
        isQueue: false,
        isAuthorized: true,
        totalCount: 180,
        errorCount: 0,
        slowCount: 0,
        hasAlert: false,
        responseStatistics: {
          Tot: 180,
          Sum: 713,
          Avg: 3,
          Max: 0,
        },
        histogram: {
          '1s': 180,
          '3s': 0,
          '5s': 0,
          Slow: 0,
          Error: 0,
        },
        apdexScore: 1,
        apdexFormula: {
          satisfiedCount: 180,
          toleratingCount: 0,
          totalSamples: 180,
        },
        timeSeriesHistogram: [
          {
            key: '1s',
            values: [30, 30, 30, 30, 29, 31],
          },
          {
            key: '3s',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: '5s',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Slow',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Error',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Avg',
            values: [3, 4, 4, 3, 4, 3],
          },
          {
            key: 'Max',
            values: [0, 0, 0, 0, 0, 0],
          },
          {
            key: 'Sum',
            values: [115, 120, 121, 116, 118, 123],
          },
          {
            key: 'Tot',
            values: [30, 30, 30, 30, 29, 31],
          },
        ],
        instanceCount: 1,
        instanceErrorCount: 0,
        agents: [
          {
            id: 'agent1',
            name: '',
          },
        ],
      },
    ],
  },
};

export const resultData = {
  key: 'app1^SPRING_BOOT',
  applicationName: 'app1',
  category: 'SPRING_BOOT',
  serviceType: 'SPRING_BOOT',
  serviceTypeCode: 1210,
  isWas: true,
  isQueue: false,
  isAuthorized: true,
  totalCount: 180,
  errorCount: 0,
  slowCount: 0,
  hasAlert: false,
  responseStatistics: {
    Tot: 180,
    Sum: 713,
    Avg: 3,
    Max: 0,
  },
  histogram: {
    '1s': 180,
    '3s': 0,
    '5s': 0,
    Slow: 0,
    Error: 0,
  },
  apdexScore: 1,
  apdexFormula: {
    satisfiedCount: 180,
    toleratingCount: 0,
    totalSamples: 180,
  },
  timeSeriesHistogram: [
    {
      key: '1s',
      values: [30, 30, 30, 30, 29, 31],
    },
    {
      key: '3s',
      values: [0, 0, 0, 0, 0, 0],
    },
    {
      key: '5s',
      values: [0, 0, 0, 0, 0, 0],
    },
    {
      key: 'Slow',
      values: [0, 0, 0, 0, 0, 0],
    },
    {
      key: 'Error',
      values: [0, 0, 0, 0, 0, 0],
    },
    {
      key: 'Avg',
      values: [3, 4, 4, 3, 4, 3],
    },
    {
      key: 'Max',
      values: [0, 0, 0, 0, 0, 0],
    },
    {
      key: 'Sum',
      values: [115, 120, 121, 116, 118, 123],
    },
    {
      key: 'Tot',
      values: [30, 30, 30, 30, 29, 31],
    },
  ],
  instanceCount: 1,
  instanceErrorCount: 0,
  agents: [
    {
      id: 'agent1',
      name: '',
    },
  ],
};
