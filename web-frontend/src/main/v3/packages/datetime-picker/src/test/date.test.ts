import { formatInTimeZone } from 'date-fns-tz';
import {
  convertToMilliseconds,
  convertToTimeUnit,
  getDateFromPatternedString,
  getFormattedTimeUnit,
  getMatchedPatternKey,
  getQuickRangeLabel,
  getZonedEndOfDay,
  getZonedStartOfDay,
  isDayFormat,
  parseTimeString,
  setZonedTimeOfDay,
} from '../utils/date';
import { getLocale, getLocaleKey } from '../utils/locale';
import { getUiText } from '../utils/uiText';
import { getDefaultCustomTimes, mergeCustomTimes } from '../components/CustomTimeView';
import type { LocaleKey, TimeUnitFormat } from '../types';

const DAY_MS = 24 * 60 * 60 * 1000;
const FULL = 'yyyy-MM-dd HH:mm:ss.SSS';

describe('setZonedTimeOfDay', () => {
  it('replaces time-of-day while preserving the calendar day in the given timeZone', () => {
    // 2026-07-13 09:30:00 KST
    const base = new Date('2026-07-13T00:30:00Z'); // = 09:30 KST
    const next = setZonedTimeOfDay(base, 'Asia/Seoul', { h: 17, m: 18, s: 58 });

    expect(formatInTimeZone(next, 'Asia/Seoul', 'yyyy-MM-dd HH:mm:ss')).toBe('2026-07-13 17:18:58');
  });

  it('keeps the wall-clock day correct across timezones (no local-time corruption)', () => {
    // 2026-07-13 23:00 in America/New_York -> next day in UTC
    const base = new Date('2026-07-14T03:00:00Z'); // = 2026-07-13 23:00 EDT
    const next = setZonedTimeOfDay(base, 'America/New_York', { h: 1, m: 2, s: 3 });

    expect(formatInTimeZone(next, 'America/New_York', 'yyyy-MM-dd HH:mm:ss')).toBe(
      '2026-07-13 01:02:03',
    );
  });

  it('zero-pads single-digit time segments', () => {
    const base = new Date('2026-01-01T12:00:00Z');
    const next = setZonedTimeOfDay(base, 'UTC', { h: 5, m: 3, s: 9 });

    expect(formatInTimeZone(next, 'UTC', 'HH:mm:ss')).toBe('05:03:09');
  });
});

// 캘린더 선택은 하루 통째(00:00:00.000 ~ 23:59:59.999)를 만들어야 한다 (NELO-2284).
describe('getZonedStartOfDay / getZonedEndOfDay (하루 경계)', () => {
  it('spans the full day down to milliseconds in the given timeZone', () => {
    const date = new Date('2026-07-13T09:45:05.750Z'); // 18:45:05.750 KST

    expect(formatInTimeZone(getZonedStartOfDay(date, 'Asia/Seoul'), 'Asia/Seoul', FULL)).toBe(
      '2026-07-13 00:00:00.000',
    );
    expect(formatInTimeZone(getZonedEndOfDay(date, 'Asia/Seoul'), 'Asia/Seoul', FULL)).toBe(
      '2026-07-13 23:59:59.999',
    );
  });

  it('keeps the day correct in a non-local timeZone', () => {
    const date = new Date('2026-07-14T03:00:00Z'); // 2026-07-13 23:00 EDT

    expect(
      formatInTimeZone(getZonedStartOfDay(date, 'America/New_York'), 'America/New_York', FULL),
    ).toBe('2026-07-13 00:00:00.000');
    expect(
      formatInTimeZone(getZonedEndOfDay(date, 'America/New_York'), 'America/New_York', FULL),
    ).toBe('2026-07-13 23:59:59.999');
  });
});

describe('convertToMilliseconds', () => {
  it.each<[TimeUnitFormat, number]>([
    ['1s', 1000],
    ['30s', 30 * 1000],
    ['1m', 60 * 1000],
    ['1h', 60 * 60 * 1000],
    ['1d', DAY_MS],
    ['1w', 7 * DAY_MS],
    ['1mo', 30 * DAY_MS],
    ['1y', 365 * DAY_MS],
  ])('converts %s -> %d ms', (unit, expected) => {
    expect(convertToMilliseconds(unit, 'UTC')).toBe(expected);
  });

  it('passes a numeric input through unchanged', () => {
    expect(convertToMilliseconds(12345, 'UTC')).toBe(12345);
  });

  it('throws on an unknown unit', () => {
    expect(() => convertToMilliseconds('5x' as TimeUnitFormat, 'UTC')).toThrow();
  });
});

describe('convertToTimeUnit', () => {
  it.each<[number, TimeUnitFormat]>([
    [500, '1s'],
    [1000, '1s'],
    [90 * 1000, '1m'],
    [60 * 60 * 1000, '1h'],
    [DAY_MS, '1d'],
    [2 * DAY_MS, '2d'],
    [7 * DAY_MS, '1w'],
    [30 * DAY_MS, '1mo'],
    [365 * DAY_MS, '1y'],
  ])('converts %d ms -> %s', (ms, expected) => {
    expect(convertToTimeUnit(ms)).toBe(expected);
  });

  it('round-trips y/mo consistently with convertToMilliseconds', () => {
    // 두 변환기가 동일한 단위 정의(mo=30d, y=365d)를 공유해야 왕복이 안정적이다
    expect(convertToTimeUnit(convertToMilliseconds('1y', 'UTC'))).toBe('1y');
    expect(convertToTimeUnit(convertToMilliseconds('1mo', 'UTC'))).toBe('1mo');
  });
});

describe('getFormattedTimeUnit', () => {
  it('defaults to convertToTimeUnit', () => {
    expect(getFormattedTimeUnit(1000)).toBe('1s');
  });

  it('uses a custom formatter when provided', () => {
    expect(getFormattedTimeUnit(1000, (ms) => `custom:${ms}`)).toBe('custom:1000');
  });
});

describe('getQuickRangeLabel', () => {
  it.each<[TimeUnitFormat, string]>([
    ['60m', 'Last 60 minutes'],
    ['1m', 'Last 1 minute'],
    ['1h', 'Last 1 hour'],
    ['7d', 'Last 7 days'],
    ['today', 'Today'],
  ])('en: %s -> %s', (unit, expected) => {
    expect(getQuickRangeLabel(unit, 'en')).toBe(expected);
  });

  it.each<[TimeUnitFormat, string]>([
    ['7d', '최근 7일'],
    ['60m', '최근 60분'],
    ['today', '오늘'],
  ])('ko: %s -> %s', (unit, expected) => {
    expect(getQuickRangeLabel(unit, 'ko')).toBe(expected);
  });

  it.each<[TimeUnitFormat, string]>([
    ['60m', '過去60分'],
    ['3h', '過去3時間'],
    ['today', '今日'],
  ])('ja: %s -> %s', (unit, expected) => {
    expect(getQuickRangeLabel(unit, 'ja')).toBe(expected);
  });

  it.each<[TimeUnitFormat, string]>([
    ['60m', '最近60分钟'],
    ['7d', '最近7天'],
    ['today', '今天'],
  ])('zh-CN: %s -> %s', (unit, expected) => {
    expect(getQuickRangeLabel(unit, 'zh-CN')).toBe(expected);
  });

  it.each<[TimeUnitFormat, string]>([
    ['60m', '最近60分鐘'],
    ['7d', '最近7天'],
    ['today', '今天'],
  ])('zh-TW: %s -> %s', (unit, expected) => {
    expect(getQuickRangeLabel(unit, 'zh-TW')).toBe(expected);
  });

  it("treats legacy 'zh' as Simplified Chinese", () => {
    expect(getQuickRangeLabel('60m', 'zh')).toBe('最近60分钟');
  });

  it('returns the raw token for an unknown unit', () => {
    expect(getQuickRangeLabel('5x' as TimeUnitFormat, 'en')).toBe('5x');
  });
});

describe('getLocale (i18n)', () => {
  it.each<LocaleKey>(['en', 'ko', 'ja', 'zh', 'zh-CN', 'zh-TW'])(
    'returns a locale for %s',
    (key) => {
      expect(getLocale(key).code).toBeTruthy();
    },
  );

  it('distinguishes Traditional (zh-TW) from Simplified (zh-CN)', () => {
    expect(getLocale('zh-TW').code).toBe('zh-TW');
    expect(getLocale('zh-CN').code).toBe('zh-CN');
    expect(getLocaleKey(getLocale('zh-TW'))).toBe('zh-TW');
    expect(getLocaleKey(getLocale('zh-CN'))).toBe('zh-CN');
  });
});

describe('getUiText', () => {
  it.each<[LocaleKey, string, string]>([
    ['en', 'Apply', 'Copy range'],
    ['ko', '적용', '범위 복사'],
    ['ja', '適用', '範囲をコピー'],
    ['zh-CN', '应用', '复制范围'],
    ['zh-TW', '套用', '複製範圍'],
  ])('%s: apply/copyRange 문구', (key, apply, copyRange) => {
    expect(getUiText(key).apply).toBe(apply);
    expect(getUiText(key).copyRange).toBe(copyRange);
  });

  it("treats legacy 'zh' as Simplified", () => {
    expect(getUiText('zh').apply).toBe('应用');
  });
});

describe('getDefaultCustomTimes (i18n)', () => {
  it('localizes section headers and relative-time example tokens', () => {
    const en = getDefaultCustomTimes(getLocale('en'), '-', 'UTC');
    expect(Object.keys(en)).toContain('Relative');
    expect(en['Relative']).toContain('45m');

    const ja = getDefaultCustomTimes(getLocale('ja'), '-', 'UTC');
    expect(Object.keys(ja)).toContain('相対');
    expect(ja['相対']).toContain('45分');

    const zhCN = getDefaultCustomTimes(getLocale('zh-CN'), '-', 'UTC');
    expect(zhCN['相对']).toContain('45分钟');

    const zhTW = getDefaultCustomTimes(getLocale('zh-TW'), '-', 'UTC');
    expect(zhTW['相對']).toContain('45分鐘');
  });

  it('example tokens actually parse back to a range', () => {
    const ja = getDefaultCustomTimes(getLocale('ja'), '-', 'UTC');
    // '45分' → minute 패턴으로 파싱되어 범위가 나와야 한다
    expect(getDateFromPatternedString(ja['相対'][0], 'UTC')).toBeDefined();
  });

  it('produces exactly three sections per locale', () => {
    expect(Object.keys(getDefaultCustomTimes(getLocale('en'), '-', 'UTC'))).toEqual([
      'Relative',
      'Fixed',
      'Unix timestamps',
    ]);
    expect(Object.keys(getDefaultCustomTimes(getLocale('ko'), '-', 'UTC'))).toEqual([
      '상대',
      '고정',
      'Unix 타임스탬프',
    ]);
  });
});

// NELO-2237: i18n 이후 기본 헤더가 로케일별로 바뀌면서, 영어 키로 넘긴 소비자 섹션이
// 더 이상 충돌하지 않아 덮어쓰기 대신 추가되어 영어 섹션이 중복 노출됐다.
describe('mergeCustomTimes', () => {
  const koDefaults = () => getDefaultCustomTimes(getLocale('ko'), '-', 'UTC');

  it('lets an English section key override the matching localized default section', () => {
    const merged = mergeCustomTimes(koDefaults(), { Relative: ['15m', '30m'] });

    // 섹션이 늘어나지 않고, 헤더는 로케일 문구를 유지한다
    expect(Object.keys(merged)).toEqual(['상대', '고정', 'Unix 타임스탬프']);
    expect(merged['상대']).toEqual(['15m', '30m']);
    expect(merged['Relative']).toBeUndefined();
  });

  it('overrides across every default header alias, whatever locale it came from', () => {
    const merged = mergeCustomTimes(koDefaults(), {
      相対: ['a'], // ja relative
      Fixed: ['b'], // en fixed
      'Unix 时间戳': ['c'], // zh-CN unix
    });

    expect(Object.keys(merged)).toEqual(['상대', '고정', 'Unix 타임스탬프']);
    expect(merged['상대']).toEqual(['a']);
    expect(merged['고정']).toEqual(['b']);
    expect(merged['Unix 타임스탬프']).toEqual(['c']);
  });

  it('appends unknown section keys after the defaults', () => {
    const merged = mergeCustomTimes(koDefaults(), { 연관: ['15m'] });

    expect(Object.keys(merged)).toEqual(['상대', '고정', 'Unix 타임스탬프', '연관']);
    expect(merged['연관']).toEqual(['15m']);
  });

  it('keeps the defaults untouched when no overrides are given', () => {
    // getDefaultCustomTimes는 현재 시각에 의존하므로 한 번만 만들어 비교한다
    const defaults = koDefaults();

    expect(mergeCustomTimes(defaults, undefined)).toEqual(defaults);
    expect(mergeCustomTimes(defaults, {})).toEqual(defaults);
  });

  // 헤더→id 매핑은 last-wins라, 서로 다른 섹션이 같은 헤더 문자열을 쓰면 조용히 하나가 먹힌다.
  // 로케일을 추가할 때 이 규칙이 깨지지 않도록 잠가둔다.
  it('has no header collision across sections of different locales', () => {
    const locales: LocaleKey[] = ['en', 'ko', 'ja', 'zh-CN', 'zh-TW'];
    const sectionIdByHeader = new Map<string, string>();

    locales.forEach((key) => {
      const [relative, fixed, unix] = Object.keys(
        getDefaultCustomTimes(getLocale(key), '-', 'UTC'),
      );
      (
        [
          [relative, 'relative'],
          [fixed, 'fixed'],
          [unix, 'unix'],
        ] as const
      ).forEach(([header, id]) => {
        const seen = sectionIdByHeader.get(header);
        // 같은 헤더가 다른 section id로 매핑되면 병합이 잘못된 섹션을 덮어쓴다
        expect(seen ?? id).toBe(id);
        sectionIdByHeader.set(header, id);
      });
    });
  });

  it('still overrides by exact key for the en locale (pre-i18n behavior)', () => {
    const enDefaults = getDefaultCustomTimes(getLocale('en'), '-', 'UTC');
    const merged = mergeCustomTimes(enDefaults, { Relative: ['15m'] });

    expect(Object.keys(merged)).toEqual(['Relative', 'Fixed', 'Unix timestamps']);
    expect(merged['Relative']).toEqual(['15m']);
  });
});

describe('getMatchedPatternKey', () => {
  it.each([
    ['30m', 'minute'],
    ['5초', 'second'],
    ['2시간', 'hour'],
    ['yesterday', 'yesterday'],
    ['어제', 'yesterday'],
    ['오늘', 'today'],
    ['last month', 'lastMonth'],
    ['1700000000000 - 1700000005000', 'unixTimestampRange'],
  ])('matches %s -> %s', (input, expected) => {
    expect(getMatchedPatternKey(input)).toBe(expected);
  });

  it('matches the previously-broken Korean tokens 지난달 / 작년', () => {
    // 회귀: ko.ts의 \/s* -> \s* 오타 수정
    expect(getMatchedPatternKey('지난달')).toBe('lastMonth');
    expect(getMatchedPatternKey('지난 달')).toBe('lastMonth');
    expect(getMatchedPatternKey('작년')).toBe('lastYear');
    expect(getMatchedPatternKey('지난해')).toBe('lastYear');
  });

  it.each([
    // Japanese
    ['5秒', 'second'],
    ['30分', 'minute'],
    ['3時間', 'hour'],
    ['7日', 'day'],
    ['2週間', 'week'],
    ['3か月', 'month'],
    ['1年', 'year'],
    ['昨日', 'yesterday'],
    ['今日', 'today'],
    ['先月', 'lastMonth'],
    ['去年', 'lastYear'],
  ])('matches Japanese %s -> %s', (input, expected) => {
    expect(getMatchedPatternKey(input)).toBe(expected);
  });

  it.each([
    // Chinese — Simplified
    ['30分钟', 'minute'],
    ['3小时', 'hour'],
    ['7天', 'day'],
    ['3个月', 'month'],
    ['昨天', 'yesterday'],
    ['今天', 'today'],
    ['上个月', 'lastMonth'],
    ['去年', 'lastYear'],
    // Chinese — Traditional
    ['30分鐘', 'minute'],
    ['3小時', 'hour'],
    ['2週', 'week'],
    ['3個月', 'month'],
    ['上個月', 'lastMonth'],
  ])('matches Chinese %s -> %s', (input, expected) => {
    expect(getMatchedPatternKey(input)).toBe(expected);
  });

  it.each([
    // 퀵레인지 라벨 접두어를 입력에서도 그대로 받아준다 (past 하위호환 포함)
    ['past 5m', 'minute'],
    ['last 5m', 'minute'],
    ['last 7 days', 'day'],
    ['Last 3 hours', 'hour'],
    ['최근 5분', 'minute'],
    ['최근 7일', 'day'],
    ['지난 3시간', 'hour'],
    ['過去5分', 'minute'],
    ['過去3時間', 'hour'],
    ['最近5分钟', 'minute'],
    ['最近7天', 'day'],
    ['近3小時', 'hour'],
  ])('matches label-prefixed %s -> %s', (input, expected) => {
    expect(getMatchedPatternKey(input)).toBe(expected);
  });

  it('returns undefined for an unmatched string', () => {
    expect(getMatchedPatternKey('nonsense')).toBeUndefined();
  });
});

describe('getDateFromPatternedString', () => {
  const ref = new Date('2026-07-13T12:00:00Z');

  it('resolves a relative minute range', () => {
    const range = getDateFromPatternedString('5m', 'UTC', ref);
    expect(range?.[0]?.getTime()).toBe(ref.getTime() - 5 * 60 * 1000);
    expect(range?.[1]?.getTime()).toBe(ref.getTime());
  });

  it.each([
    '5m',
    '5分',
    '5分钟',
    '5分鐘',
    // 라벨 접두어가 붙은 입력도 동일 범위로 해석된다
    'past 5m',
    'last 5m',
    '최근 5분',
    '過去5分',
    '最近5分钟',
  ])('resolves the same minute range across languages (%s)', (token) => {
    const range = getDateFromPatternedString(token, 'UTC', ref);
    expect(range?.[0]?.getTime()).toBe(ref.getTime() - 5 * 60 * 1000);
    expect(range?.[1]?.getTime()).toBe(ref.getTime());
  });

  it('resolves 지난달 to the previous calendar month (timezone-aware)', () => {
    const [from, to] = getDateFromPatternedString('지난달', 'UTC', ref) as [Date, Date];
    expect(formatInTimeZone(from, 'UTC', 'yyyy-MM-dd HH:mm:ss')).toBe('2026-06-01 00:00:00');
    expect(formatInTimeZone(to, 'UTC', 'yyyy-MM-dd HH:mm:ss')).toBe('2026-06-30 23:59:59');
  });

  it('resolves a unix timestamp range', () => {
    const [from, to] = getDateFromPatternedString('1700000000000 - 1700000005000', 'UTC', ref) as [
      Date,
      Date,
    ];
    expect(from.getTime()).toBe(1700000000000);
    expect(to.getTime()).toBe(1700000005000);
  });

  it('returns undefined for an unmatched string', () => {
    expect(getDateFromPatternedString('nonsense', 'UTC', ref)).toBeUndefined();
  });
});

describe('isDayFormat', () => {
  it.each(['Jul 5', '7/5', '12. 30'])('accepts %s', (input) => {
    expect(isDayFormat(input)).toBe(true);
  });

  it.each(['12X 30', 'Jul 6th, 14:30', '14:30', '2026/07/05'])('rejects %s', (input) => {
    // '12X 30'은 이스케이프되지 않은 `.` 버그가 있었을 때 통과하던 케이스
    expect(isDayFormat(input)).toBe(false);
  });
});

describe('getZonedStartOfDay / getZonedEndOfDay', () => {
  it('snaps to the start/end of the day in the given timeZone', () => {
    const d = new Date('2026-07-13T05:00:00Z'); // 14:00 KST
    expect(
      formatInTimeZone(getZonedStartOfDay(d, 'Asia/Seoul'), 'Asia/Seoul', 'yyyy-MM-dd HH:mm:ss'),
    ).toBe('2026-07-13 00:00:00');
    expect(
      formatInTimeZone(getZonedEndOfDay(d, 'Asia/Seoul'), 'Asia/Seoul', 'yyyy-MM-dd HH:mm:ss'),
    ).toBe('2026-07-13 23:59:59');
  });
});

describe('parseTimeString', () => {
  const locale = getLocale('en');
  const ref = new Date('2026-07-01T08:15:00Z');

  it('snaps both endpoints of an all-day range', () => {
    const [from, to] = parseTimeString('7/5 - 7/6', locale, {
      timeZone: 'UTC',
      referenceDate: [ref, ref],
    }) as [Date, Date];
    expect(formatInTimeZone(from, 'UTC', 'HH:mm:ss')).toBe('00:00:00');
    expect(formatInTimeZone(to, 'UTC', 'HH:mm:ss')).toBe('23:59:59');
  });

  it('snaps the "from" endpoint based on "from", not "to" (regression)', () => {
    // from은 day 포맷 -> start-of-day 스냅되어야 하고, to는 시각이 있어 스냅되지 않아야 한다.
    // 예전 버그(양쪽 모두 to.trim()으로 검사)에서는 from이 referenceDate 시각(08:15)을 유지했다.
    const [from, to] = parseTimeString('7/5 - 14:30', locale, {
      timeZone: 'UTC',
      referenceDate: [ref, ref],
    }) as [Date, Date];
    expect(formatInTimeZone(from, 'UTC', 'HH:mm:ss')).toBe('00:00:00');
    expect(formatInTimeZone(to, 'UTC', 'HH:mm')).toBe('14:30');
  });
});
