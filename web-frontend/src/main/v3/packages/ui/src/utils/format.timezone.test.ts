import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';

// format.test.ts 는 date-fns-tz 를 모킹해서 format() 의 분기(포맷 선택, 유효성)를 본다.
// 이 파일은 반대로 실제 date-fns-tz 를 태워서 **찍히는 문자열 자체**를 고정한다.
// 라이브러리를 올릴 때 화면의 시각 표기가 조용히 바뀌는 것을 잡는 것이 목적이라,
// 여기서는 date-fns / date-fns-tz 를 모킹하지 않는다.
const mockSettings: Record<string, unknown> = {};
jest.mock('./localStorage', () => ({
  getLocalStorageValue: jest.fn((key: string) => mockSettings[key]),
}));

import { format } from './format';

const setSettings = (timezone: string, language: string, dateFormat: number) => {
  mockSettings[APP_SETTING_KEYS.TIMEZONE] = timezone;
  mockSettings[APP_SETTING_KEYS.LANGUAGE] = language;
  mockSettings[APP_SETTING_KEYS.DATE_FORMAT] = dateFormat;
};

// 2024-01-15T03:04:05.678Z
const PLAIN = Date.UTC(2024, 0, 15, 3, 4, 5, 678);
// America/New_York 서머타임 시작 직전/직후 (현지 02시가 통째로 건너뛰어진다)
const BEFORE_DST = Date.UTC(2024, 2, 10, 6, 59, 59, 999);
const AFTER_DST = Date.UTC(2024, 2, 10, 7, 0, 0, 0);

describe('format() with the real date-fns-tz', () => {
  describe.each`
    timezone              | language | dateFormat | instant  | expected
    ${'UTC'}              | ${'en'}  | ${0}       | ${PLAIN} | ${'2024.01.15 03:04:05'}
    ${'UTC'}              | ${'en'}  | ${2}       | ${PLAIN} | ${'Jan 15th, 2024 03:04:05'}
    ${'UTC'}              | ${'en'}  | ${5}       | ${PLAIN} | ${'15th Jan 2024 03:04:05 AM'}
    ${'UTC'}              | ${'ko'}  | ${2}       | ${PLAIN} | ${'1월 15일, 2024 03:04:05'}
    ${'UTC'}              | ${'ko'}  | ${5}       | ${PLAIN} | ${'15일 1월 2024 03:04:05 오전'}
    ${'Asia/Seoul'}       | ${'en'}  | ${0}       | ${PLAIN} | ${'2024.01.15 12:04:05'}
    ${'Asia/Seoul'}       | ${'en'}  | ${5}       | ${PLAIN} | ${'15th Jan 2024 12:04:05 PM'}
    ${'Asia/Seoul'}       | ${'ko'}  | ${0}       | ${PLAIN} | ${'2024.01.15 12:04:05'}
    ${'America/New_York'} | ${'en'}  | ${0}       | ${PLAIN} | ${'2024.01.14 22:04:05'}
  `(
    '$timezone / $language / format $dateFormat',
    ({ timezone, language, dateFormat, instant, expected }) => {
      it(`renders ${expected}`, () => {
        setSettings(timezone, language, dateFormat);
        expect(format(instant)).toBe(expected);
      });
    },
  );

  // 서머타임 경계에서 현지 02시대가 사라지는 것까지 맞아야 한다.
  it('follows the DST transition in America/New_York', () => {
    setSettings('America/New_York', 'en', 0);
    expect(format(BEFORE_DST)).toBe('2024.03.10 01:59:59');
    expect(format(AFTER_DST)).toBe('2024.03.10 03:00:00');
  });

  it('keeps the same instant when only the timezone changes', () => {
    setSettings('UTC', 'en', 0);
    expect(format(AFTER_DST)).toBe('2024.03.10 07:00:00');
    setSettings('Asia/Seoul', 'en', 0);
    expect(format(AFTER_DST)).toBe('2024.03.10 16:00:00');
  });

  it('honours an explicit format string over the stored one', () => {
    setSettings('Asia/Seoul', 'en', 0);
    expect(format(PLAIN, 'yyyy-MM-dd')).toBe('2024-01-15');
    expect(format(PLAIN, 'MMM do HH:mm')).toBe('Jan 15th 12:04');
  });

  it.each([[-1], [NaN], [Infinity], [-Infinity]])('returns N/A for %p', (bad) => {
    setSettings('Asia/Seoul', 'en', 0);
    expect(format(bad)).toBe('N/A');
  });

  it('returns N/A for an invalid Date', () => {
    setSettings('Asia/Seoul', 'en', 0);
    expect(format(new Date('nope'))).toBe('N/A');
  });
});
