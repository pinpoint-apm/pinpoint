import { pickServiceName } from './serviceName';

const pick = (enableServiceMap: boolean, pathname: string, selectedService = 'my-service') =>
  pickServiceName({ enableServiceMap, pathname, selectedService });

describe('pickServiceName', () => {
  // 1) 설정이 꺼져 있으면 service 개념 자체가 없다. 경로에 실려 있어도 읽지 않는다.
  test('is undefined when enableServiceMap is off', () => {
    expect(pick(false, '/serviceMap/blogService/app@TOMCAT')).toBeUndefined();
    expect(pick(false, '/serverMap/app@TOMCAT')).toBeUndefined();
  });

  // 2) 경로가 진실의 원천이다 — 전역 선택값(탭 간 공유 저장소)보다 앞선다.
  test('prefers the service name carried by the path', () => {
    expect(pick(true, '/serviceMap/blogService/app@TOMCAT')).toBe('blogService');
    expect(pick(true, '/transactionList/url-service/app@TOMCAT')).toBe('url-service');
  });

  // 3) 아직 serviceName을 싣지 않는 화면은 전역 선택값으로 폴백한다.
  test('falls back to the globally selected service', () => {
    expect(pick(true, '/serverMap/app@TOMCAT')).toBe('my-service');
    expect(pick(true, '/inspector/app@TOMCAT')).toBe('my-service');
    // serviceName 세그먼트가 생기기 전 형태의 옛 링크도 여기로 온다.
    expect(pick(true, '/transactionList/app@TOMCAT')).toBe('my-service');
  });

  // 4) 둘 다 없으면 DEFAULT. 백엔드가 헤더 없는 요청을 해석하는 값과 같다.
  test('falls back to DEFAULT when nothing carries a service', () => {
    expect(pick(true, '/serverMap/app@TOMCAT', '')).toBe('DEFAULT');
    expect(pickServiceName({ enableServiceMap: true, pathname: '/serverMap/app@TOMCAT' })).toBe(
      'DEFAULT',
    );
  });

  test('keeps an encoded service name in the path decoded once', () => {
    expect(pick(true, '/serviceMap/team%2Fa/app@TOMCAT')).toBe('team/a');
  });
});
