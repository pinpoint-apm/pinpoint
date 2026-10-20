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

/**
 * 백엔드 `ServiceNameRequest` 검증 규칙과 맞춘다.
 * - 길이: 3자 이상(예약어를 2자로 추가할 여지를 남긴다), `PinpointConstants.SERVICE_NAME_MAX_LEN` 이하
 * - 문자: `IdValidateUtils.ID_PATTERN_VALUE` (`[a-zA-Z0-9._\-]+`)
 */
export const SERVICE_NAME_MIN_LENGTH = 3;

export const SERVICE_NAME_MAX_LENGTH = 254;

export const SERVICE_NAME_PATTERN = /^[a-zA-Z0-9._-]+$/;

export const isValidServiceNamePattern = (name: string) => SERVICE_NAME_PATTERN.test(name);

export const isValidServiceNameLength = (name: string) =>
  name.length >= SERVICE_NAME_MIN_LENGTH && name.length <= SERVICE_NAME_MAX_LENGTH;
