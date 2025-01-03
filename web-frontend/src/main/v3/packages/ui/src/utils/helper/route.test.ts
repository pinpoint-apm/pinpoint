import {
  getServerImagePath,
  getServerIconPath,
  getApplicationPath,
  getHostGroupPath,
  getFilteredMapPath,
} from './route';

describe('Test route helper utils', () => {
  describe('Test "getServerImagePath"', () => {
    test('Return server image path when input application object', () => {
      const application = {
        serviceType: 'node',
      };
      const result = getServerImagePath(application);
      expect(result).toEqual('/img/servers/node.png');
    });

    test('Return server UNKNOWN image path when input application object', () => {
      const application = {};
      const result = getServerImagePath(application);
      expect(result).toEqual('/img/servers/UNKNOWN.png');
    });
  });

  describe('Test "getServerIconPath"', () => {
    test('Return icon image path when input application object', () => {
      const application = {
        serviceType: 'node',
      };
      const result = getServerIconPath(application);
      expect(result).toEqual('/img/icons/node.png');
    });

    test('Return icon UNKNOWN image path when input application object', () => {
      const application = {};
      const result = getServerIconPath(application);
      expect(result).toEqual('/img/icons/UNKNOWN.png');
    });
  });

  describe('Test "getApplicationPath"', () => {
    const pagePath = '/serverMap';

    test('Return application path', () => {
      const application = {
        serviceType: 'node',
        applicationName: 'pinpoint',
      };
      const result = getApplicationPath(pagePath)(application);
      expect(result).toEqual(`${pagePath}/pinpoint@node`);
    });

    test('Return application path with searchParameters when input queryParam object(but only inlcudes "to" and "from"', () => {
      const application = {
        serviceType: 'node',
        applicationName: 'pinpoint',
      };
      const queryParam = {
        from: '2023-11-10-15-14-14',
        to: '2023-11-10-15-19-14',
        inbound: '2',
        outbound: '2',
        wasOnly: 'true',
        bidirectional: 'true',
      };
      const result = getApplicationPath(pagePath)(application, queryParam);
      expect(result).toEqual(
        `${pagePath}/pinpoint@node?from=2023-11-10-15-14-14&to=2023-11-10-15-19-14`,
      );
    });

    test('Return only page path when input abnormal application', () => {
      const application = {
        serviceType: 'node',
      };
      const result = getApplicationPath(pagePath)(application);
      expect(result).toEqual(pagePath);

      const application2 = {
        applicationName: 'pinpoint',
      };
      const result2 = getApplicationPath(pagePath)(application2);
      expect(result2).toEqual(pagePath);

      const application3 = null;
      const result3 = getApplicationPath(pagePath)(application3);
      expect(result3).toEqual(pagePath);
    });
  });

  describe('Test "getHostGroupPath"', () => {
    const pagePath = '/serverMap';

    test('Return hostGroup path', () => {
      const hostGroup = 'pinpointHost';
      const result = getHostGroupPath(pagePath)(hostGroup);
      expect(result).toEqual(`${pagePath}/${hostGroup}`);
    });

    test('Return hostGroup path with searchParameters when input queryParam object(but only inlcudes "to" and "from"', () => {
      const hostGroup = 'pinpointHost';
      const queryParam = {
        from: '2023-11-10-15-14-14',
        to: '2023-11-10-15-19-14',
      };
      const result = getHostGroupPath(pagePath)(hostGroup, queryParam);
      expect(result).toEqual(
        `${pagePath}/${hostGroup}?from=2023-11-10-15-14-14&to=2023-11-10-15-19-14`,
      );
    });

    test('Return only page path when input abnormal hostGroup name', () => {
      const hostGroup = '';
      const result = getHostGroupPath(pagePath)(hostGroup);
      expect(result).toEqual(pagePath);

      const hostGroup2 = null;
      const result2 = getHostGroupPath(pagePath)(hostGroup2);
      expect(result2).toEqual(pagePath);
    });
  });

  describe('Test "getFilteredMapPath"', () => {
    test('Return FilterMap path: using toApplication and toServiceType when applicationName is not exist and sourceIsWas is false', () => {
      const filterState = {
        fromApplication: '',
        fromServiceType: '',
        toApplication: '',
        toServiceType: '',
        transactionResult: null,
        applicationName: 'applicationName',
        serviceType: 'serviceType',
        agentName: '',
        responseFrom: 0,
        responseTo: 'max',
        url: '',
        fromAgentName: '',
        toAgentName: '',
        agents: ['agent'],
      };
      const sourceIsWas = false;

      const result = getFilteredMapPath(filterState, sourceIsWas);
      expect(result).toEqual('/filteredMap/applicationName@serviceType');
    });

    test('Return FilterMap path: using fromApplication and fromServiceType when applicationName is not exist and sourceIsWas is true', () => {
      const filterState = {
        fromApplication: 'fromApplication',
        fromServiceType: 'fromServiceType',
        toApplication: 'toApplication',
        toServiceType: 'toServiceType',
        transactionResult: null,
        applicationName: '',
        serviceType: '',
        agentName: '',
        responseFrom: 0,
        responseTo: 'max',
        url: '',
        fromAgentName: '',
        toAgentName: '',
        fromAgents: ['fromAgent'],
      };
      const sourceIsWas = true;

      const result = getFilteredMapPath(filterState, sourceIsWas);
      expect(result).toEqual('/filteredMap/fromApplication@fromServiceType');
    });

    test('Return FilterMap path: using toApplication and toServiceType when applicationName is not exist and sourceIsWas is false', () => {
      const filterState = {
        fromApplication: 'fromApplication',
        fromServiceType: 'fromServiceType',
        toApplication: 'toApplication',
        toServiceType: 'toServiceType',
        transactionResult: null,
        applicationName: '',
        serviceType: '',
        agentName: '',
        responseFrom: 0,
        responseTo: 'max',
        url: '',
        fromAgentName: '',
        toAgentName: '',
        fromAgents: ['fromAgent'],
      };
      const sourceIsWas = false;

      const result = getFilteredMapPath(filterState, sourceIsWas);
      expect(result).toEqual('/filteredMap/toApplication@toServiceType');
    });
  });
});
