import { atom } from 'jotai';
import { atomWithStorage, createJSONStorage } from 'jotai/utils';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';
import { getLocalStorageValue, setLocalStorageValue } from '@pinpoint-fe/ui/src/utils/localStorage';

export const DEFAULT_SERVICE = 'DEFAULT';

export const RESERVED_SERVICE_NAMES = ['DEFAULT', 'TEST', 'ERROR', 'UNKNOWN', 'NULL'];

export const isReservedServiceName = (name: string) =>
  RESERVED_SERVICE_NAMES.includes(name.toUpperCase());

// sessionStorage에 저장해 브라우저 탭마다 selectedService를 독립적으로 유지한다.
// localStorage는 origin 단위로 공유되고 storage 이벤트로 탭 간 실시간 동기화까지 되어,
// 한 탭에서 service를 바꾸면 다른 탭도 따라 바뀌었다. sessionStorage는 탭(브라우징 컨텍스트)
// 단위라 값이 공유되지 않고 storage 이벤트도 전파되지 않아 탭별 선택이 가능하다.
// (링크로 연 같은-origin 탭은 sessionStorage를 복사받아 선택을 승계한다.)
//
// 다만 sessionStorage만 쓰면 브라우저를 재기동하거나 새 탭을 열 때마다 DEFAULT로 돌아간다.
// 그래서 "마지막으로 선택한 service"를 localStorage에도 함께 기록해 두고, sessionStorage가
// 비어 있는 첫 진입에서만 그 값을 기본값으로 승계한다. 진실의 원천은 여전히 sessionStorage이므로
// 이미 열려 있는 탭들은 서로의 선택에 영향을 받지 않는다.

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

// 마지막 선택을 기억하는 것은 부가 기능이라, localStorage 접근이 막힌 환경에서는 조용히 포기한다.
// (sessionStorage와 달리 없어도 탭 안에서의 동작에는 아무 영향이 없다.)
const readLastSelectedService = () => {
  try {
    return getLocalStorageValue(APP_SETTING_KEYS.LAST_SELECTED_SERVICE);
  } catch {
    return undefined;
  }
};

const writeLastSelectedService = (service: string) => {
  try {
    setLocalStorageValue(APP_SETTING_KEYS.LAST_SELECTED_SERVICE, service);
  } catch {
    // 저장에 실패해도 이 탭의 선택은 sessionStorage로 유지된다.
  }
};

export const SELECTED_SERVICE_STORAGE_KEY = 'pp.selectedService';

const sessionStorageForService = createJSONStorage<string>(getSessionStringStorage);

const lastSelectedService = readLastSelectedService();

// sessionStorage가 비어 있을 때만 쓰이는 기본값이다. 즉 이 탭에서 이미 고른 service가 있으면
// 그 값이 이기고, 새 탭·브라우저 재기동처럼 빈 상태로 시작할 때만 마지막 선택을 승계한다.
const initialSelectedService =
  typeof lastSelectedService === 'string' ? lastSelectedService : DEFAULT_SERVICE;

// 승계한 값은 이 탭의 sessionStorage에 곧바로 심어 둔다. 심어 두지 않으면 다른 탭이 service를
// 바꾼 뒤 이 탭을 새로고침했을 때 선택이 따라 바뀌어 탭별 독립성이 깨진다.
if (getSessionStringStorage().getItem(SELECTED_SERVICE_STORAGE_KEY) === null) {
  sessionStorageForService.setItem(SELECTED_SERVICE_STORAGE_KEY, initialSelectedService);
}

// getOnInit: true → 첫 렌더부터 sessionStorage 값을 동기로 읽는다.
// 이로써 fetch 인터셉터의 최초 요청도 저장된 service를 반영하고,
// 새로고침 시 hydration으로 인한 불필요한 캐시 무효화를 방지한다.
const storedSelectedServiceAtom = atomWithStorage<string>(
  SELECTED_SERVICE_STORAGE_KEY,
  initialSelectedService,
  sessionStorageForService,
  {
    getOnInit: true,
  },
);

// 선택할 때마다 마지막 선택을 localStorage에도 기록해 다음 첫 진입의 기본값으로 쓴다.
export const selectedServiceAtom = atom(
  (get) => get(storedSelectedServiceAtom),
  (_get, set, service: string) => {
    set(storedSelectedServiceAtom, service);
    writeLastSelectedService(service);
  },
);
