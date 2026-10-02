import { enUS } from 'date-fns/locale';
import { diagnoseDateInput } from '../utils/diagnoseInput';
import { parseTimeString } from '../utils/date';
import type { PickerErrorCode } from '../types';

const TZ = 'Asia/Seoul';
const BASE = { locale: enUS, timeZone: TZ, seamToken: '-' } as const;

const diagnose = (input: string, overrides = {}) =>
  diagnoseDateInput({ input, ...BASE, ...overrides });

describe('diagnoseDateInput', () => {
  describe('빈 입력', () => {
    it.each(['', '   '])('allowEmpty면 %o를 오류로 보지 않는다', (input) => {
      expect(diagnose(input, { allowEmpty: true }).code).toBeNull();
    });

    // date-fns의 parse는 문자열과 포맷이 둘 다 비면 referenceDate를 그대로 돌려준다.
    // 이 분기가 없으면 빈 입력이 "오늘 하루"로 유효 판정돼 그대로 커밋된다.
    it.each(['', '   '])('커밋 시에는 %o를 empty로 잡는다', (input) => {
      expect(diagnose(input).code).toBe<PickerErrorCode>('empty');
    });
  });

  describe('형태 오류', () => {
    // 조각이 비어도 파싱은 '성공'한다 — 빈 조각이 오늘로 되살아나기 때문에
    // 파싱 성공/실패만 보면 절대 잡히지 않는 케이스다.
    it.each(['Sep 1st - ', ' - Sep 1st'])('한쪽이 빈 %o를 incomplete로 잡는다', (input) => {
      expect(diagnose(input).code).toBe<PickerErrorCode>('incomplete');
    });

    it('seamToken이 날짜 내부 하이픈과 충돌하면 seam-conflict', () => {
      expect(diagnose('2024-05-05 - 2024-05-06').code).toBe<PickerErrorCode>('seam-conflict');
    });

    it('seamToken을 ~로 바꾸면 같은 입력이 충돌하지 않는다', () => {
      const result = diagnose('2024/05/05 ~ 2024/05/06', { seamToken: '~' });
      expect(result.code).toBeNull();
    });

    it.each(['zzz', 'not a date'])('%o는 unparseable', (input) => {
      expect(diagnose(input).code).toBe<PickerErrorCode>('unparseable');
    });
  });

  describe('의미 오류', () => {
    it('from이 to보다 늦으면 reversed', () => {
      const result = diagnose('2024/05/06 ~ 2024/05/05', { seamToken: '~' });
      expect(result.code).toBe<PickerErrorCode>('reversed');
    });

    it('timeZone이 달라도 reversed 판정은 같다', () => {
      const input = '2024/05/06 ~ 2024/05/05';
      const seoul = diagnose(input, { seamToken: '~' });
      const utc = diagnose(input, { seamToken: '~', timeZone: 'UTC' });
      expect(seoul.code).toBe(utc.code);
    });
  });

  describe('minDate / maxDate', () => {
    const range = '2024/05/05 ~ 2024/05/06';

    it('minDate 당일이면 통과한다 (일 경계로 비교하므로 시각은 무시)', () => {
      // minDate에 09:00이 붙어 있어도 캘린더는 그 날 00:00 선택을 허용한다.
      const minDate = new Date('2024-05-05T09:00:00+09:00');
      expect(diagnose(range, { seamToken: '~', minDate }).code).toBeNull();
    });

    it('minDate보다 하루 이르면 out-of-bounds', () => {
      const minDate = new Date('2024-05-06T00:00:00+09:00');
      expect(diagnose(range, { seamToken: '~', minDate }).code).toBe<PickerErrorCode>(
        'out-of-bounds',
      );
    });

    it('maxDate보다 늦으면 out-of-bounds', () => {
      const maxDate = new Date('2024-05-05T00:00:00+09:00');
      expect(diagnose(range, { seamToken: '~', maxDate }).code).toBe<PickerErrorCode>(
        'out-of-bounds',
      );
    });
  });

  describe('패턴 문자열', () => {
    it.each(['45m', 'yesterday', 'today', 'last month'])('%o는 유효하다', (input) => {
      expect(diagnose(input).code).toBeNull();
    });

    it('unix 타임스탬프 범위는 seam 분기로 오진되지 않는다', () => {
      expect(diagnose('1714867200000-1714953600000').code).toBeNull();
    });
  });

  describe('반환값', () => {
    it('text는 trim된 문자열이다', () => {
      expect(diagnose('  45m  ').text).toBe('45m');
    });

    it('dates가 trim된 문자열의 parseTimeString 결과와 같다', () => {
      const input = '  2024/05/05 ~ 2024/05/06  ';
      const result = diagnose(input, { seamToken: '~' });
      const expected = parseTimeString('2024/05/05 ~ 2024/05/06', enUS, {
        seamToken: '~',
        timeZone: TZ,
      });

      expect(result.dates[0]?.getTime()).toBe(expected[0]?.getTime());
      expect(result.dates[1]?.getTime()).toBe(expected[1]?.getTime());
    });
  });
});
