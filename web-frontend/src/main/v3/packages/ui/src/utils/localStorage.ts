import { APP_SETTING_KEYS, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';

const parseSafe = (value: string | null) => {
  if (value === null || value === undefined || value === 'undefined') {
    return undefined;
  }
  try {
    return JSON.parse(value);
  } catch {
    return undefined;
  }
};

// 저장소를 막아 둔 sandbox iframe(MCP Apps 등)에서는 window.localStorage 접근만으로 throw 한다.
// 그때는 저장된 값이 없는 것처럼 동작해 각 호출부의 기본값(시스템 타임존 등)을 쓰게 한다.
export const getLocalStorageValue = (key: APP_SETTING_KEYS | EXPERIMENTAL_CONFIG_KEYS | string) => {
  try {
    return parseSafe(window.localStorage.getItem(key));
  } catch {
    return undefined;
  }
};

export const setLocalStorageValue = (
  key: APP_SETTING_KEYS | EXPERIMENTAL_CONFIG_KEYS | string,
  value: unknown,
) => {
  try {
    window.localStorage.setItem(key, JSON.stringify(value));
  } catch {
    // 저장하지 못해도 화면은 메모리의 값으로 계속 동작한다.
  }
};

export const removeLocalStorageValue = (
  key: APP_SETTING_KEYS | EXPERIMENTAL_CONFIG_KEYS | string,
) => {
  try {
    window.localStorage.removeItem(key);
  } catch {
    // 지우지 못해도 다음 읽기에서 만료 여부를 다시 판단한다.
  }
};
