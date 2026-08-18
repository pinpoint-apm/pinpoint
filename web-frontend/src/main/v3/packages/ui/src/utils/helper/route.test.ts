import {
  getServerImagePath,
  getServerIconPath,
  getApplicationPath,
  getHostGroupPath,
  getFilteredMapPath,
  getFilterTargetApplication,
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

    // servicemap에서 넘어오면 어떤 service를 보던 중이었는지 URL에 남아야 한다.
    // filteredMap은 새 탭으로 열려서 전역 선택값을 믿을 수 없다.
    describe('with a service name', () => {
      const filterState = {
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

      test('Carry the service name as its own segment', () => {
        expect(getFilteredMapPath(filterState, false, 'DEFAULT')).toEqual(
          '/filteredMap/DEFAULT/ACL-PORTAL-DEV@SPRING_BOOT',
        );
        expect(getFilteredMapPath(filterState, false, 'blogService')).toEqual(
          '/filteredMap/blogService/ACL-PORTAL-DEV@SPRING_BOOT',
        );
      });

      // 백엔드가 serviceName 형식을 검증하지 않으므로 '/'나 '@'가 들어올 수 있다.
      // 그대로 실으면 세그먼트가 쪼개져 라우트 매칭이 깨진다.
      test('Encode a service name that would break the path', () => {
        expect(getFilteredMapPath(filterState, false, 'team/a@b')).toEqual(
          '/filteredMap/team%2Fa%40b/ACL-PORTAL-DEV@SPRING_BOOT',
        );
      });

      // servermap에서 넘어올 때는 주어지지 않는다. 경로 형태가 예전과 같아야 한다.
      test('Keep the path unchanged when no service name is given', () => {
        expect(getFilteredMapPath(filterState, false, undefined)).toEqual(
          '/filteredMap/ACL-PORTAL-DEV@SPRING_BOOT',
        );
      });
    });

    // servicemap의 service group(접힌 service)처럼 기준 application을 고를 수 없는 필터.
    test('Return only the page path when no application can be picked', () => {
      const emptyFilterState = {
        fromApplication: '',
        fromServiceType: '',
        toApplication: '',
        toServiceType: '',
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

      expect(getFilteredMapPath(emptyFilterState, false)).toEqual('/filteredMap');
      expect(getFilteredMapPath(emptyFilterState, false, 'DEFAULT')).toEqual(
        '/filteredMap/DEFAULT',
      );
    });
  });

  describe('Test "getFilterTargetApplication"', () => {
    const base = {
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

    test('Pick the node application when the filter is on a node', () => {
      expect(
        getFilterTargetApplication({
          ...base,
          applicationName: 'ACL-PORTAL-DEV',
          serviceType: 'SPRING_BOOT',
        }),
      ).toEqual({ applicationName: 'ACL-PORTAL-DEV', serviceType: 'SPRING_BOOT' });
    });

    test('Pick the link side by sourceIsWas', () => {
      expect(getFilterTargetApplication(base, true)).toEqual({
        applicationName: 'FRONT',
        serviceType: 'TOMCAT',
      });
      expect(getFilterTargetApplication(base, false)).toEqual({
        applicationName: 'ACL-PORTAL-DEV',
        serviceType: 'SPRING_BOOT',
      });
    });

    // filteredMap은 기준 application 없이 조회가 성립하지 않는다. 호출자가 화면을 열지 않는다.
    test('Return null when neither side carries an application', () => {
      expect(
        getFilterTargetApplication({
          ...base,
          fromApplication: '',
          fromServiceType: '',
          toApplication: '',
          toServiceType: '',
        }),
      ).toBeNull();
    });

    test('Return null when the service type is missing', () => {
      expect(getFilterTargetApplication({ ...base, toServiceType: '' }, false)).toBeNull();
    });

    // servicemap의 Application→Service 링크. 출발지가 WAS라 기준은 잡히지만, 도착지가 service
    // group이라 필터가 반쪽만 걸린다. Application→Application만 filteredMap으로 연결한다.
    test('Return null when only one side of the link carries an application', () => {
      const toServiceGroup = { ...base, toApplication: '', toServiceType: '' };
      expect(getFilterTargetApplication(toServiceGroup, true)).toBeNull();
      expect(getFilterTargetApplication(toServiceGroup, false)).toBeNull();

      const fromServiceGroup = { ...base, fromApplication: '', fromServiceType: '' };
      expect(getFilterTargetApplication(fromServiceGroup, true)).toBeNull();
      expect(getFilterTargetApplication(fromServiceGroup, false)).toBeNull();
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
