import { coversPageOnForbidden } from './pagePermission';

describe('coversPageOnForbidden', () => {
  // 기본은 "한 API라도 403이면 그 화면은 권한 없음"이다. 엔드포인트는 따지지 않는다.
  test.each([
    '/inspector/app-name@TOMCAT',
    '/urlStatistic/app-name@TOMCAT',
    '/threadDump/app-name@TOMCAT',
    '/errorAnalysis/app-name@TOMCAT',
    '/transactionDetail/app-name@TOMCAT',
    '/transactionList/app-name@TOMCAT',
    '/systemMetric/host-group',
    '/openTelemetryMetric/app-name@TOMCAT',
    '/config/agentStatistic',
    '/config/agentManagement',
  ])('covers %s', (pathname) => {
    expect(coversPageOnForbidden(pathname)).toBe(true);
  });

  // map API에는 권한 검사가 없다. 403은 언제나 **고른 노드**를 묻는 조회에서 나오므로, 그것으로
  // map을 덮으면 다른 노드를 고를 방법이 사라진다.
  test.each([
    '/serverMap/app-name@TOMCAT',
    '/serverMap/realtime/app-name@TOMCAT',
    '/serviceMap/my-service/app-name@TOMCAT',
    '/serviceMap/realtime/my-service',
    '/filteredMap/my-service/app-name@TOMCAT',
  ])('leaves %s to the panel that asked for it', (pathname) => {
    expect(coversPageOnForbidden(pathname)).toBe(false);
  });

  // 확대 화면은 `/serverMap`의 하위 경로가 아니다. 차트 하나가 곧 페이지라 막히면 남는 것이 없다.
  test.each([
    '/scatterFullScreenMode/app-name@TOMCAT',
    '/scatterFullScreenMode/realtime/app-name@TOMCAT',
    '/heatmapFullScreenMode/app-name@TOMCAT',
  ])('covers %s', (pathname) => {
    expect(coversPageOnForbidden(pathname)).toBe(true);
  });

  // 경로 접두사가 겹치는 것만으로 예외가 되면 `/serverMapSomething`까지 끌려온다.
  test('does not exclude a path that merely starts with an excluded page name', () => {
    expect(coversPageOnForbidden('/serverMapX')).toBe(true);
  });
});
