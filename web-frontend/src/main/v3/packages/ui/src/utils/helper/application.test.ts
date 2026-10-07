import {
  getApplicationTypeAndName,
  getApplicationKey,
  getServiceNameFromPath,
  getServiceNameSegmentPage,
  parseSystemMetricPath,
  hasServiceNameInPath,
  parseServiceScopedPath,
  registerAppRoutes,
} from './application';
import { APP_PATH, ApplicationType } from '@pinpoint-fe/ui/src/constants';

describe('Test application helper utils', () => {
  describe('Test "getApplicationTypeAndName"', () => {
    test('Extract application name and service type from valid path', () => {
      const path = '/appName@serviceType';
      const result = getApplicationTypeAndName(path);
      expect(result).toEqual({
        applicationName: 'appName',
        serviceType: 'serviceType',
      });
    });

    test('Extract application name and service type from path with caret separator', () => {
      const path = '/appName^serviceType';
      const result = getApplicationTypeAndName(path);
      expect(result).toEqual({
        applicationName: 'appName',
        serviceType: 'serviceType',
      });
    });

    test('Extract application name and service type from path without leading slash', () => {
      const path = 'appName@serviceType';
      const result = getApplicationTypeAndName(path);
      expect(result).toEqual({
        applicationName: 'appName',
        serviceType: 'serviceType',
      });
    });

    test('Return null when path does not match pattern', () => {
      const path = '/invalid-path';
      const result = getApplicationTypeAndName(path);
      expect(result).toBeNull();
    });

    test('Return null when path is empty', () => {
      const path = '';
      const result = getApplicationTypeAndName(path);
      expect(result).toBeNull();
    });

    test('Return null when path has no separator', () => {
      const path = '/appName';
      const result = getApplicationTypeAndName(path);
      expect(result).toBeNull();
    });

    test('Handle path with multiple segments', () => {
      const path = '/parent/appName@serviceType';
      const result = getApplicationTypeAndName(path);
      expect(result).toEqual({
        applicationName: 'appName',
        serviceType: 'serviceType',
      });
    });
  });

  describe('Test "hasServiceNameInPath"', () => {
    test.each([
      // 세그먼트 표기 — application이 없어도 serviceName이 실린다.
      [`${APP_PATH.SERVICE_MAP}/blogService`, true],
      [`${APP_PATH.SERVICE_MAP}/blogService/appName@TOMCAT`, true],
      [`${APP_PATH.TRANSACTION_LIST}/svc/appName@TOMCAT`, true],
      [`${APP_PATH.TRANSACTION_DETAIL}/svc/appName@TOMCAT`, true],
      // serviceName 세그먼트가 생기기 전 형태
      [`${APP_PATH.SERVICE_MAP}/appName@TOMCAT`, false],
      [`${APP_PATH.TRANSACTION_LIST}/appName@TOMCAT`, false],
      [`${APP_PATH.TRANSACTION_LIST}`, false],
      // 사이드 메뉴의 application 단위 화면들
      [`${APP_PATH.INSPECTOR}/svc`, true],
      [`${APP_PATH.INSPECTOR}/svc/appName@TOMCAT`, true],
      [`${APP_PATH.URL_STATISTIC}/svc/appName@TOMCAT`, true],
      [`${APP_PATH.ERROR_ANALYSIS}/svc/appName@TOMCAT`, true],
      [`${APP_PATH.OPEN_TELEMETRY_METRIC}/svc/appName@TOMCAT`, true],
      [`${APP_PATH.THREAD_DUMP}/svc/appName@TOMCAT`, true],
      [`${APP_PATH.INSPECTOR}/appName@TOMCAT`, false],
      // '@'가 그대로 남아 있으면 application 세그먼트다(serviceName은 '%40'으로 실린다).
      [`${APP_PATH.INSPECTOR}/svc@appName@TOMCAT`, false],
      // 아직 serviceName을 싣지 않는 화면
      [`${APP_PATH.SERVER_MAP}/appName@TOMCAT`, false],
      // systemMetric은 모양으로 구별하지 않는다 — 첫 세그먼트를 serviceName으로 읽고, 설정이 꺼져
      // 있을 때의 해석은 `parseSystemMetricPath`와 `pickServiceName`이 맡는다.
      [`${APP_PATH.SYSTEM_METRIC}/svc/hostGroup`, true],
      ['', false],
    ])('Return %p → %p', (pathname, expected) => {
      expect(hasServiceNameInPath(pathname)).toBe(expected);
    });
  });

  describe('Test "getServiceNameFromPath"', () => {
    // serviceName은 application과 별도 세그먼트로 실린다.
    test('Return the service name carried as its own segment', () => {
      expect(getServiceNameFromPath('/serviceMap/blogService')).toBe('blogService');
      expect(getServiceNameFromPath('/serviceMap/blogService/appName@TOMCAT')).toBe('blogService');
      expect(getServiceNameFromPath('/serviceMap/DEFAULT/appName@TOMCAT')).toBe('DEFAULT');
      expect(getServiceNameFromPath('/transactionList/svc/appName@TOMCAT')).toBe('svc');
      expect(getServiceNameFromPath('/transactionDetail/svc/appName@TOMCAT')).toBe('svc');
    });

    // serviceName 세그먼트가 생기기 전 형태. 첫 세그먼트를 service 이름으로 오해하면 안 된다.
    test('Return undefined on a path that carries only an application', () => {
      expect(getServiceNameFromPath('/transactionList/appName@TOMCAT')).toBeUndefined();
      expect(getServiceNameFromPath('/transactionDetail/appName@TOMCAT')).toBeUndefined();
      expect(getServiceNameFromPath('/serviceMap/appName@TOMCAT')).toBeUndefined();
      expect(getServiceNameFromPath('/serviceMap/appName^TOMCAT')).toBeUndefined();
    });

    test('Decode a service name that was encoded to survive the path', () => {
      expect(getServiceNameFromPath('/serviceMap/team%2Fa%40b')).toBe('team/a@b');
    });

    test('Return undefined on a bare servicemap path', () => {
      expect(getServiceNameFromPath('/serviceMap')).toBeUndefined();
      expect(getServiceNameFromPath('/serviceMap/')).toBeUndefined();
    });

    // `/serviceMap/realtime`은 `/serviceMap`의 하위 경로다.
    // 'realtime' 세그먼트를 service 이름으로 읽으면 모든 조회가 없는 service로 나간다.
    test('Read the service name after the realtime segment, not the segment itself', () => {
      expect(getServiceNameFromPath('/serviceMap/realtime/blogService/appName@TOMCAT')).toBe(
        'blogService',
      );
      expect(getServiceNameFromPath('/serviceMap/realtime/DEFAULT/appName@TOMCAT')).toBe('DEFAULT');
      expect(getServiceNameFromPath('/serviceMap/realtime/team%2Fa%40b/appName@TOMCAT')).toBe(
        'team/a@b',
      );
      expect(getServiceNameFromPath('/serviceMap/realtime')).toBeUndefined();
      expect(getServiceNameFromPath('/serviceMap/realtime/appName@TOMCAT')).toBeUndefined();
    });

    // 스캐터/히트맵의 확대 버튼은 새 탭을 여는데, 그 화면도 같은 service로 조회해야 한다.
    // `/{page}/realtime`은 `/{page}`의 하위 경로이므로 'realtime'을 service 이름으로 읽으면 안 된다.
    test('Read the service name on a fullScreenMode path', () => {
      expect(getServiceNameFromPath('/scatterFullScreenMode/blogService/appName@TOMCAT')).toBe(
        'blogService',
      );
      expect(getServiceNameFromPath('/heatmapFullScreenMode/blogService/appName@TOMCAT')).toBe(
        'blogService',
      );
      expect(
        getServiceNameFromPath('/scatterFullScreenMode/realtime/blogService/appName@TOMCAT'),
      ).toBe('blogService');
      expect(
        getServiceNameFromPath('/heatmapFullScreenMode/realtime/team%2Fa%40b/appName@TOMCAT'),
      ).toBe('team/a@b');
      // 세그먼트가 생기기 전 형태의 링크·북마크.
      expect(getServiceNameFromPath('/scatterFullScreenMode/appName@TOMCAT')).toBeUndefined();
      expect(
        getServiceNameFromPath('/heatmapFullScreenMode/realtime/appName@TOMCAT'),
      ).toBeUndefined();
    });

    test('Return undefined on a path that does not carry a service name', () => {
      expect(getServiceNameFromPath('/serverMap/svc@appName@TOMCAT')).toBeUndefined();
    });

    // filteredMap은 servicemap에서 넘어오면 serviceName을 싣고, servermap에서 넘어오면 싣지 않는다.
    // 그 구분이 유지되어야 화면이 돌아갈 map을 정할 수 있다.
    test('Read the service name on a filteredMap path only when one is carried', () => {
      expect(getServiceNameFromPath('/filteredMap/DEFAULT/appName@TOMCAT')).toBe('DEFAULT');
      expect(getServiceNameFromPath('/filteredMap/blogService/appName@TOMCAT')).toBe('blogService');
      expect(getServiceNameFromPath('/filteredMap/team%2Fa%40b/appName@TOMCAT')).toBe('team/a@b');
      expect(getServiceNameFromPath('/filteredMap/appName@TOMCAT')).toBeUndefined();
      expect(getServiceNameFromPath('/filteredMap')).toBeUndefined();
    });

    // 경로 빌더가 인코딩해서 넣은 값을 되돌린다.
    test('Decode the encoded service name', () => {
      expect(getServiceNameFromPath('/transactionList/a%2Fb/appName@TOMCAT')).toBe('a/b');
      expect(getServiceNameFromPath('/transactionList/a%40b/appName@TOMCAT')).toBe('a@b');
      expect(getServiceNameFromPath('/transactionList/a%20b/appName@TOMCAT')).toBe('a b');
    });

    // 경로는 사용자가 직접 편집할 수 있다. 렌더 중에 호출되므로 던지면 화면이 죽는다.
    test('Fall back to the raw value on a malformed encoding instead of throwing', () => {
      expect(getServiceNameFromPath('/transactionList/100%/appName@TOMCAT')).toBe('100%');
    });
  });

  describe('Test "parseServiceScopedPath"', () => {
    test('Split a path that carries both a service name and an application', () => {
      expect(
        parseServiceScopedPath(APP_PATH.SERVICE_MAP, '/serviceMap/svc/appName@TOMCAT'),
      ).toEqual({
        serviceName: 'svc',
        encodedServiceName: 'svc',
        applicationSegment: 'appName@TOMCAT',
        application: { applicationName: 'appName', serviceType: 'TOMCAT' },
      });
    });

    // 리다이렉트 목적지는 인코딩된 그대로의 세그먼트로 만들어야 두 번 인코딩되지 않는다.
    test('Keep the raw segment alongside the decoded service name', () => {
      const result = parseServiceScopedPath(APP_PATH.SERVICE_MAP, '/serviceMap/team%2Fa%40b');

      expect(result.serviceName).toBe('team/a@b');
      expect(result.encodedServiceName).toBe('team%2Fa%40b');
      expect(result.application).toBeNull();
    });

    // serviceName 세그먼트가 생기기 전 형태. 첫 세그먼트를 application으로 읽어야 한다.
    test('Read the first segment as an application on a path without a service name', () => {
      const result = parseServiceScopedPath(APP_PATH.SERVICE_MAP, '/serviceMap/appName@TOMCAT');

      expect(result.serviceName).toBeUndefined();
      expect(result.encodedServiceName).toBeUndefined();
      expect(result.application).toEqual({ applicationName: 'appName', serviceType: 'TOMCAT' });
    });

    // 같은 규칙이 하위 경로에도 그대로 적용된다. 'realtime'을 service 이름으로 읽으면 안 된다.
    test('Split a realtime path against its own page prefix', () => {
      expect(
        parseServiceScopedPath(
          APP_PATH.SERVICE_MAP_REALTIME,
          '/serviceMap/realtime/svc/appName@TOMCAT',
        ),
      ).toEqual({
        serviceName: 'svc',
        encodedServiceName: 'svc',
        applicationSegment: 'appName@TOMCAT',
        application: { applicationName: 'appName', serviceType: 'TOMCAT' },
      });
    });

    test('Return empty segments on a bare page path', () => {
      const result = parseServiceScopedPath(APP_PATH.SERVICE_MAP_REALTIME, '/serviceMap/realtime');

      expect(result.serviceName).toBeUndefined();
      expect(result.application).toBeNull();
    });
  });

  describe('Test "parseSystemMetricPath"', () => {
    test('Read the first segment as the service name when the service map is enabled', () => {
      expect(parseSystemMetricPath('/systemMetric/a%2Fb/hostGroup', true)).toEqual({
        encodedServiceName: 'a%2Fb',
        hostGroupName: 'hostGroup',
      });
      expect(parseSystemMetricPath('/systemMetric/svc', true)).toEqual({
        encodedServiceName: 'svc',
        hostGroupName: undefined,
      });
      expect(parseSystemMetricPath('/systemMetric', true)).toEqual({
        encodedServiceName: undefined,
        hostGroupName: undefined,
      });
    });

    // service 개념이 없으면 예전처럼 마지막 세그먼트가 hostGroup이다.
    test('Read the last segment as the host group when the service map is disabled', () => {
      expect(parseSystemMetricPath('/systemMetric/hostGroup', false)).toEqual({
        encodedServiceName: undefined,
        hostGroupName: 'hostGroup',
      });
      expect(parseSystemMetricPath('/systemMetric/svc/hostGroup', false)).toEqual({
        encodedServiceName: undefined,
        hostGroupName: 'hostGroup',
      });
      expect(parseSystemMetricPath('/systemMetric/', false).hostGroupName).toBeUndefined();
    });
  });

  describe('Test "getServiceNameSegmentPage"', () => {
    // 로더가 리다이렉트 목적지를 만들 때 쓴다. 더 긴 경로가 먼저 매칭되어야
    // 실시간 화면의 리다이렉트가 비실시간 화면으로 새지 않는다.
    test('Return the longest matching page prefix', () => {
      expect(getServiceNameSegmentPage('/serviceMap/realtime/svc/appName@TOMCAT')).toBe(
        '/serviceMap/realtime',
      );
      expect(getServiceNameSegmentPage('/serviceMap/svc/appName@TOMCAT')).toBe('/serviceMap');
      expect(getServiceNameSegmentPage('/heatmapFullScreenMode/realtime/svc/appName@TOMCAT')).toBe(
        '/heatmapFullScreenMode/realtime',
      );
      expect(getServiceNameSegmentPage('/scatterFullScreenMode/appName@TOMCAT')).toBe(
        '/scatterFullScreenMode',
      );
      expect(getServiceNameSegmentPage('/inspector/svc/appName@TOMCAT')).toBe('/inspector');
      expect(getServiceNameSegmentPage('/errorAnalysis/svc')).toBe('/errorAnalysis');
      expect(getServiceNameSegmentPage('/config/alarm/svc')).toBe('/config/alarm');
    });

    test('Return undefined on a page that does not carry a service name segment', () => {
      expect(getServiceNameSegmentPage('/serverMap/appName@TOMCAT')).toBeUndefined();
      expect(getServiceNameSegmentPage('/config/general')).toBeUndefined();
    });
  });

  // 어느 화면이 serviceName을 싣는지는 등록된 라우트 정의의 `:serviceName`이 정한다.
  // 테스트 전체에는 앱과 같은 모양의 라우트가 등록돼 있다(`jest.setupAfterEnv.cjs`).
  describe('Test "registerAppRoutes"', () => {
    test('Read the service name on any route that declares a serviceName param', () => {
      registerAppRoutes([{ path: '/appPage/:serviceName?/:application?' }]);

      expect(getServiceNameSegmentPage('/appPage/svc/appName@TOMCAT')).toBe('/appPage');
      expect(getServiceNameFromPath('/appPage/svc/appName@TOMCAT')).toBe('svc');
      // 등록되지 않은 화면은 그 자리를 읽지 않는다.
      expect(getServiceNameFromPath('/inspector/svc/appName@TOMCAT')).toBeUndefined();
    });

    // 레이아웃 라우트 아래의 상대 경로도 부모 경로와 이어 붙여 판단한다.
    test('Join nested relative route paths', () => {
      registerAppRoutes([
        { path: '/section', children: [{ path: 'page/:serviceName?' }, { path: 'other/:id' }] },
      ]);

      expect(getServiceNameSegmentPage('/section/page/svc')).toBe('/section/page');
      expect(getServiceNameFromPath('/section/page/a%2Fb')).toBe('a/b');
      expect(getServiceNameSegmentPage('/section/other/svc')).toBeUndefined();
    });

    // 하위 경로끼리의 우선순위는 react-router의 경로 랭킹이 정한다 — 등록 순서와 무관하다.
    test('Prefer the more specific route regardless of the declaration order', () => {
      registerAppRoutes([
        { path: '/page/:serviceName?/:application?' },
        { path: '/page/realtime/:serviceName?/:application?' },
      ]);

      expect(getServiceNameSegmentPage('/page/realtime/svc')).toBe('/page/realtime');
      expect(getServiceNameFromPath('/page/realtime/svc')).toBe('svc');
    });

    test('Do not take another param that only shares the name prefix', () => {
      registerAppRoutes([{ path: '/page/:serviceNameX' }]);

      expect(getServiceNameSegmentPage('/page/svc')).toBeUndefined();
    });

    // 등록을 빠뜨리면 깨지지 않고 전역 선택값으로 폴백한다(serviceName을 읽지 않는다).
    test('Read no service name before any route is registered', () => {
      registerAppRoutes([]);

      expect(getServiceNameFromPath('/serviceMap/svc/appName@TOMCAT')).toBeUndefined();
    });

    // 라우트 매칭의 params는 디코딩된 값이다. 원본 세그먼트로 읽어야 '@'가 든 service 이름과
    // application 세그먼트를 구별한다.
    test('Keep telling an encoded service name from an application segment', () => {
      expect(getServiceNameFromPath('/inspector/a%40b/appName@TOMCAT')).toBe('a@b');
      expect(getServiceNameFromPath('/inspector/appName@TOMCAT')).toBeUndefined();
    });
  });

  describe('Test "getApplicationKey"', () => {
    test('Return application key from application object', () => {
      const application: ApplicationType = {
        applicationName: 'appName',
        serviceType: 'serviceType',
      };
      const result = getApplicationKey(application);
      expect(result).toBe('appName^serviceType');
    });

    test('Return application key with empty strings when application is undefined', () => {
      const result = getApplicationKey(undefined);
      expect(result).toBe('undefined^undefined');
    });

    test('Return application key with partial data', () => {
      const application: Partial<ApplicationType> = {
        applicationName: 'appName',
      };
      const result = getApplicationKey(application as ApplicationType);
      expect(result).toBe('appName^undefined');
    });

    test('Handle application with special characters', () => {
      const application: ApplicationType = {
        applicationName: 'app-name',
        serviceType: 'service_type',
      };
      const result = getApplicationKey(application);
      expect(result).toBe('app-name^service_type');
    });
  });
});
