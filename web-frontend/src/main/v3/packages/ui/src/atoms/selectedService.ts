import { atomWithStorage } from 'jotai/utils';

export const DEFAULT_SERVICE = 'DEFAULT';

export const RESERVED_SERVICE_NAMES = ['DEFAULT', 'TEST', 'ERROR', 'UNKNOWN', 'NULL'];

export const isReservedServiceName = (name: string) =>
  RESERVED_SERVICE_NAMES.includes(name.toUpperCase());

// getOnInit: true → 첫 렌더부터 localStorage 값을 동기로 읽는다.
// 이로써 fetch 인터셉터의 최초 요청도 저장된 service를 반영하고,
// 새로고침 시 hydration으로 인한 불필요한 캐시 무효화를 방지한다.
export const selectedServiceAtom = atomWithStorage<string>(
  'selectedService',
  DEFAULT_SERVICE,
  undefined,
  {
    getOnInit: true,
  },
);
