import {
  getServerImagePath,
  getServerIconPath,
  getApplicationPath,
  getHostGroupPath,
  getFilteredMapPath,
  getTransactionListPath,
  getTransactionDetailPath,
  getServiceMapPath,
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

  describe('Test "getServiceMapPath"', () => {
    const application = { applicationName: 'appName', serviceType: 'TOMCAT' };
    const dateRange = { from: 'from', to: 'to' };

    // servicemap은 어떤 service를 보는지가 URL의 진실의 원천이라 DEFAULT도 예외 없이 싣는다.
    test('Always carry the service name, DEFAULT included', () => {
      expect(getServiceMapPath('DEFAULT')).toEqual('/serviceMap/DEFAULT');
      expect(getServiceMapPath('blogService')).toEqual('/serviceMap/blogService');
    });

    test('Append the application as its own segment', () => {
      expect(getServiceMapPath('DEFAULT', application)).toEqual(
        '/serviceMap/DEFAULT/appName@TOMCAT',
      );
      expect(getServiceMapPath('DEFAULT', application, dateRange)).toEqual(
        '/serviceMap/DEFAULT/appName@TOMCAT?from=from&to=to',
      );
    });

    // application 없이도 map을 그리므로, 보고 있던 기간이 초기화되면 안 된다.
    test('Keep the date range even without an application', () => {
      expect(getServiceMapPath('blogService', null, dateRange)).toEqual(
        '/serviceMap/blogService?from=from&to=to',
      );
    });

    test('Omit the query string when only one of from/to is given', () => {
      expect(getServiceMapPath('blogService', null, { from: 'from' })).toEqual(
        '/serviceMap/blogService',
      );
    });

    // 인코딩하지 않으면 '/'가 세그먼트를 쪼개 라우트 매칭이 깨지고, '@'는 application
    // 세그먼트의 구분자와 구별되지 않는다.
    test('Encode the service name so it cannot break the path segment', () => {
      expect(getServiceMapPath('a/b')).toEqual('/serviceMap/a%2Fb');
      expect(getServiceMapPath('a@b', application)).toEqual('/serviceMap/a%40b/appName@TOMCAT');
      expect(getServiceMapPath('a b')).toEqual('/serviceMap/a%20b');
    });
  });

  describe('Test "getTransactionListPath"', () => {
    const application = { applicationName: 'appName', serviceType: 'TOMCAT' };
    const dateRange = { from: 'from', to: 'to' };

    test('Return the page path only when application is not given', () => {
      expect(getTransactionListPath()).toEqual('/transactionList');
      expect(getTransactionListPath(null, dateRange, 'svc')).toEqual('/transactionList');
    });

    test('Omit the service segment when service name is not given', () => {
      expect(getTransactionListPath(application)).toEqual('/transactionList/appName@TOMCAT');
      expect(getTransactionListPath(application, dateRange)).toEqual(
        '/transactionList/appName@TOMCAT?from=from&to=to',
      );
    });

    test('Carry the service name as its own segment, like servicemap', () => {
      expect(getTransactionListPath(application, undefined, 'svc')).toEqual(
        '/transactionList/svc/appName@TOMCAT',
      );
      expect(getTransactionListPath(application, dateRange, 'svc')).toEqual(
        '/transactionList/svc/appName@TOMCAT?from=from&to=to',
      );
    });

    test('Omit the query string when only one of from/to is given', () => {
      expect(getTransactionListPath(application, { from: 'from' }, 'svc')).toEqual(
        '/transactionList/svc/appName@TOMCAT',
      );
    });

    // 백엔드가 service 이름 형식을 검증하지 않으므로 '/'나 '@'가 들어올 수 있다. 인코딩하지
    // 않으면 '/'가 세그먼트를 쪼개 라우트 매칭이 깨지고, '@'는 application 세그먼트의 구분자와
    // 구별되지 않는다.
    test('Encode the service name so it cannot break the path segment', () => {
      expect(getTransactionListPath(application, undefined, 'a/b')).toEqual(
        '/transactionList/a%2Fb/appName@TOMCAT',
      );
      expect(getTransactionListPath(application, undefined, 'a@b')).toEqual(
        '/transactionList/a%40b/appName@TOMCAT',
      );
      expect(getTransactionListPath(application, undefined, 'a b')).toEqual(
        '/transactionList/a%20b/appName@TOMCAT',
      );
    });
  });

  describe('Test "getTransactionDetailPath"', () => {
    const application = { applicationName: 'appName', serviceType: 'TOMCAT' };
    const dateRange = { from: 'from', to: 'to' };

    test('Return the page path only when application is not given', () => {
      expect(getTransactionDetailPath()).toEqual('/transactionDetail');
      expect(getTransactionDetailPath(null, dateRange, 'svc')).toEqual('/transactionDetail');
    });

    test('Omit the service segment when service name is not given', () => {
      expect(getTransactionDetailPath(application)).toEqual('/transactionDetail/appName@TOMCAT');
      expect(getTransactionDetailPath(application, dateRange)).toEqual(
        '/transactionDetail/appName@TOMCAT?from=from&to=to',
      );
    });

    test('Carry the service name as its own segment, like servicemap', () => {
      expect(getTransactionDetailPath(application, undefined, 'svc')).toEqual(
        '/transactionDetail/svc/appName@TOMCAT',
      );
    });

    test('Encode the service name so it cannot break the path segment', () => {
      expect(getTransactionDetailPath(application, undefined, 'a/b')).toEqual(
        '/transactionDetail/a%2Fb/appName@TOMCAT',
      );
    });
  });
});
