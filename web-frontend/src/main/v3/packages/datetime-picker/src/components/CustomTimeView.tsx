import React from 'react';
import { Locale, subMinutes } from 'date-fns';
import { addDays, addHours } from 'date-fns';
import { DateRange } from '../types';
import {
  getZonedEndOfDay,
  getZonedStartOfDay,
  getZonedStartOfMonth,
  parseTimeString,
} from '../utils/date';
import AppContext from './context/appContext';
import { formatInTimeZone, toZonedTime } from 'date-fns-tz';
import { getLocaleKey, resolveLocaleKey, type LabelLocale } from '../utils/locale';
import { getUiText } from '../utils/uiText';
import { cn } from '../utils/style';

export interface CustomTimeViewProps {
  show: boolean;
  locale: Locale;
  direction: 'right' | 'left' | 'bottom' | 'none';
  dateFormat?: string;
  customTimes: {
    [key: string]: string[];
  };
  children?: React.ReactNode;
  onClickTimeString?: (dateRange: DateRange, value: string) => void;
}

export const CustomTimeView = ({
  show,
  locale,
  direction,
  dateFormat,
  customTimes,
  children,
  onClickTimeString,
}: CustomTimeViewProps) => {
  const {
    appContext: { seamToken, timeZone },
  } = React.useContext(AppContext);
  const handleClickDateString = (dateString: string) => {
    onClickTimeString?.(
      parseTimeString(dateString, locale, { dateFormat, seamToken, timeZone }),
      dateString,
    );
  };

  return (
    <CustomTimeViewSlider show={show} direction={direction}>
      {children ? (
        children
      ) : (
        <div className="rdp:flex rdp:flex-col rdp:gap-4 rdp:px-5 rdp:py-3">
          <div className="rdp:text-sm rdp:font-bold">
            {getUiText(getLocaleKey(locale)).customTimesTitle}
          </div>
          {Object.keys(customTimes).map((key) => {
            const times = customTimes?.[key];

            return times?.length > 0 ? (
              <div key={key}>
                <div className="rdp:mb-2 rdp:text-xs">{key}</div>
                <div className="rdp:flex rdp:flex-wrap rdp:gap-1.5">
                  {times.map((time) => {
                    return (
                      <button
                        key={time}
                        type="button"
                        className="rich-datetime-picker__more-label"
                        onClick={() => handleClickDateString(time)}
                      >
                        {time}
                      </button>
                    );
                  })}
                </div>
              </div>
            ) : null;
          })}
        </div>
      )}
    </CustomTimeViewSlider>
  );
};

const CustomTimeViewSlider = ({
  show,
  direction,
  children,
}: Pick<CustomTimeViewProps, 'show' | 'children' | 'direction'>) => {
  if (direction === 'none') {
    return children;
  }
  return (
    <div
      className={cn(
        'rich-datetime-picker__more',
        'rdp:transform rdp:transition-all rdp:duration-200',
        {
          'rdp:left-0 rdp:rounded-l rdp:border-r rdp:border-r-rgba2': direction === 'left',
          'rdp:right-0 rdp:rounded-r rdp:border-l rdp:border-l-rgba2': direction === 'right',
          'rdp:left-0 rdp:w-full rdp:rounded-bl rdp:rounded-br rdp:border-t rdp:border-t-rgba2':
            direction === 'bottom',
          'rdp:translate-x-0 rdp:opacity-0': !show,
          'rdp:opacity-100': show,
          'rdp:-translate-x-[100%]': show && direction === 'left',
          'rdp:translate-x-[100%]': show && direction === 'right',
          'rdp:translate-y-[100%]': show && direction === 'bottom',
        },
      )}
      style={{
        transitionProperty: 'opacity, transform',
      }}
    >
      {children}
    </div>
  );
};

// 기본 커스텀타임 예시(섹션 헤더 + 상대시간 토큰)의 로케일별 문구.
// 상대시간 토큰은 각 언어 파서가 실제로 인식하는 문자열이어야 한다.
const CUSTOM_TIME_I18N: Record<
  LabelLocale,
  { relativeHeader: string; fixedHeader: string; unixHeader: string; relative: string[] }
> = {
  en: {
    relativeHeader: 'Relative',
    fixedHeader: 'Fixed',
    unixHeader: 'Unix timestamps',
    relative: ['45m', '12hours', '10d', '2 weeks', 'last month', 'yesterday', 'today'],
  },
  ko: {
    relativeHeader: '상대',
    fixedHeader: '고정',
    unixHeader: 'Unix 타임스탬프',
    relative: ['45분', '12시간', '10일', '2주', '지난달', '어제', '오늘'],
  },
  ja: {
    relativeHeader: '相対',
    fixedHeader: '固定',
    unixHeader: 'Unixタイムスタンプ',
    relative: ['45分', '12時間', '10日', '2週間', '先月', '昨日', '今日'],
  },
  'zh-CN': {
    relativeHeader: '相对',
    fixedHeader: '固定',
    unixHeader: 'Unix 时间戳',
    relative: ['45分钟', '12小时', '10天', '2周', '上个月', '昨天', '今天'],
  },
  'zh-TW': {
    relativeHeader: '相對',
    fixedHeader: '固定',
    unixHeader: 'Unix 時間戳',
    relative: ['45分鐘', '12小時', '10天', '2週', '上個月', '昨天', '今天'],
  },
};

/** 표시 헤더와 무관하게 기본 섹션을 식별하기 위한 안정적인 id. */
type CustomTimeSectionId = 'relative' | 'fixed' | 'unix';

// 모든 로케일의 기본 섹션 헤더 → section id.
// CUSTOM_TIME_I18N에서 파생하므로 로케일을 추가하면 자동으로 반영된다.
// (ja/zh-CN/zh-TW의 '固定'처럼 여러 로케일이 공유하는 헤더는 같은 id로 수렴한다)
const SECTION_ID_BY_HEADER: Record<string, CustomTimeSectionId> = Object.values(
  CUSTOM_TIME_I18N,
).reduce<Record<string, CustomTimeSectionId>>((acc, i18n) => {
  acc[i18n.relativeHeader] = 'relative';
  acc[i18n.fixedHeader] = 'fixed';
  acc[i18n.unixHeader] = 'unix';
  return acc;
}, {});

/**
 * 기본 섹션과 소비자가 지정한 섹션을 병합한다.
 *
 * 섹션 identity는 표시 헤더가 아니라 section id 기준이므로, 영어 키(`Relative`)로 넘겨도
 * 현재 로케일의 대응 기본 섹션(`상대`)을 덮어쓰고 헤더는 로케일 문구가 그대로 표시된다.
 * 이 정규화가 없으면 i18n 로케일에서 키가 충돌하지 않아 영어 섹션이 중복 노출된다(NELO-2237).
 * 알 수 없는 키는 기본 섹션 뒤에 새 섹션으로 추가한다.
 */
export const mergeCustomTimes = (
  defaults: CustomTimeViewProps['customTimes'],
  overrides: CustomTimeViewProps['customTimes'] = {},
): CustomTimeViewProps['customTimes'] => {
  const merged = { ...defaults };
  // 기본 섹션의 section id → 실제 헤더(현재 로케일 문구)
  const headerBySectionId = new Map<CustomTimeSectionId, string>();
  Object.keys(defaults).forEach((header) => {
    const sectionId = SECTION_ID_BY_HEADER[header];
    if (sectionId) headerBySectionId.set(sectionId, header);
  });

  Object.entries(overrides).forEach(([header, times]) => {
    const sectionId = SECTION_ID_BY_HEADER[header];
    const targetHeader = (sectionId && headerBySectionId.get(sectionId)) || header;
    merged[targetHeader] = times;
  });

  return merged;
};

export const getDefaultCustomTimes = (
  locale: Locale,
  seamToken: string,
  timeZone: string,
): CustomTimeViewProps['customTimes'] => {
  const i18n = CUSTOM_TIME_I18N[resolveLocaleKey(getLocaleKey(locale))];
  const now = toZonedTime(new Date(), timeZone);
  const startDayOfMonth = getZonedStartOfMonth(now, timeZone);
  const nextDayOfStartOfMonth = getZonedEndOfDay(
    addDays(getZonedStartOfMonth(now, timeZone), 1),
    timeZone,
  );
  const baseHour = addHours(getZonedStartOfDay(now, timeZone), 9);

  return {
    [i18n.relativeHeader]: i18n.relative,
    [i18n.fixedHeader]: [
      formatInTimeZone(startDayOfMonth, timeZone, 'MMM d', { locale }),
      `${formatInTimeZone(startDayOfMonth, timeZone, 'MMM d', {
        locale,
      })} ${seamToken} ${formatInTimeZone(nextDayOfStartOfMonth, timeZone, 'MMM d', {
        locale,
      })}`,
      formatInTimeZone(startDayOfMonth, timeZone, 'M/d', { locale }),
      `${formatInTimeZone(startDayOfMonth, timeZone, 'M/d', {
        locale,
      })} ${seamToken} ${formatInTimeZone(nextDayOfStartOfMonth, timeZone, 'M/d', {
        locale,
      })}`,
      `${formatInTimeZone(baseHour, timeZone, 'hh:mm a', {
        locale,
      })} ${seamToken} ${formatInTimeZone(addHours(baseHour, 8), timeZone, 'hh:mm a', {
        locale,
      })}`,
    ],
    [i18n.unixHeader]: [`${subMinutes(now, 5).getTime()} ${seamToken} ${now.getTime()}`],
  };
};
