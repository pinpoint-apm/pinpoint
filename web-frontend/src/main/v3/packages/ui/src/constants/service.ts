/**
 * service를 고르지 않았을 때의 service.
 *
 * 백엔드도 `pServiceName` 헤더가 없는 요청을 이 service로 해석한다. 그래서
 * `enableServiceMap`이 꺼져 있는 동안 나가는 모든 요청은 결과적으로 이 service의 조회다.
 */
export const DEFAULT_SERVICE = 'DEFAULT';

export const RESERVED_SERVICE_NAMES = ['DEFAULT', 'TEST', 'ERROR', 'UNKNOWN', 'NULL'];

export const isReservedServiceName = (name: string) =>
  RESERVED_SERVICE_NAMES.includes(name.toUpperCase());
