import { atomWithStorage, createJSONStorage } from 'jotai/utils';

export const DEFAULT_SERVICE = 'DEFAULT';

export const RESERVED_SERVICE_NAMES = ['DEFAULT', 'TEST', 'ERROR', 'UNKNOWN', 'NULL'];

export const isReservedServiceName = (name: string) =>
  RESERVED_SERVICE_NAMES.includes(name.toUpperCase());

// sessionStorage에 저장해 브라우저 탭마다 selectedService를 독립적으로 유지한다.
// localStorage는 origin 단위로 공유되고 storage 이벤트로 탭 간 실시간 동기화까지 되어,
// 한 탭에서 service를 바꾸면 다른 탭도 따라 바뀌었다. sessionStorage는 탭(브라우징 컨텍스트)
// 단위라 값이 공유되지 않고 storage 이벤트도 전파되지 않아 탭별 선택이 가능하다.
// (새 탭·재시작은 값이 없어 DEFAULT로 시작하고, 링크로 연 같은-origin 탭은 복사받아 승계한다.)

/**
 * 스토리지 접근 자체가 던지는 환경이 있다(서드파티 스토리지가 차단된 iframe, 일부 프라이버시
 * 설정). jotai의 기본 getter는 이를 감싸 폴백하지만, getter를 직접 넘기면 그 방어가 사라진다.
 * `getOnInit`이 첫 렌더에서 읽으므로 예외가 그대로 전파되면 화면이 아예 뜨지 않는다.
 *
 * createJSONStorage의 타입은 getter가 undefined를 돌려주는 것을 허용하지 않으므로(런타임은
 * 허용한다) 메모리 fallback을 준다. 탭을 벗어나면 값이 유지되지 않지만, 그런 환경에서는
 * sessionStorage도 어차피 유지되지 않는다.
 *
 * `getStringStorage()`는 접근마다 호출되므로 fallback은 반드시 모듈 레벨 싱글턴이어야 한다.
 * 매번 새 Map을 만들면 같은 탭 안에서도 값이 남지 않는다.
 */
const memoryStringStorage = (() => {
  const store = new Map<string, string>();

  return {
    getItem: (key: string) => store.get(key) ?? null,
    setItem: (key: string, newValue: string) => {
      store.set(key, newValue);
    },
    removeItem: (key: string) => {
      store.delete(key);
    },
  };
})();

const getSessionStringStorage = () => {
  try {
    return sessionStorage;
  } catch {
    return memoryStringStorage;
  }
};

// getOnInit: true → 첫 렌더부터 sessionStorage 값을 동기로 읽는다.
// 이로써 fetch 인터셉터의 최초 요청도 저장된 service를 반영하고,
// 새로고침 시 hydration으로 인한 불필요한 캐시 무효화를 방지한다.
export const selectedServiceAtom = atomWithStorage<string>(
  'selectedService',
  DEFAULT_SERVICE,
  createJSONStorage(getSessionStringStorage),
  {
    getOnInit: true,
  },
);
