import React from 'react';
import type { Meta, StoryObj } from '@storybook/react-vite';
import { RichDatetimePicker, RichDatetimePickerProps } from '../components/RichDatetimePicker';
import { format, subMinutes, subDays } from 'date-fns';
import { DateRange } from '../types';

// More on how to set up at: https://storybook.js.org/docs/react/writing/introduction#default-export
const meta = {
  title: 'Rich/RichDatetimePicker/Basic',
  component: RichDatetimePicker,
  parameters: {
    // Optional parameter to center the component in the Canvas. More info: https://storybook.js.org/docs/react/configure/story-layout
    layout: 'centered',
  },
  // This component will have an automatically generated Autodocs entry: https://storybook.js.org/docs/react/writing-docs/autodocs
  tags: ['autodocs'],
  // More on argTypes: https://storybook.js.org/docs/react/api/argtypes
  argTypes: {
    // backgroundColor: { control: 'color' },
  },
} satisfies Meta<typeof RichDatetimePicker>;

export default meta;
type Story = StoryObj<typeof meta>;

const TemplateDatetimePicker = (args: RichDatetimePickerProps) => {
  const now = new Date();
  const [startDate, setStartDate] = React.useState<Date | null>(subMinutes(now, 5));
  const [endDate, setEndDate] = React.useState<Date | null>(now);
  const handleChange = (params: DateRange) => {
    setStartDate(params[0]);
    setEndDate(params[1]);
  };
  return (
    <div className="rdp:h-[400px] rdp:font-sans">
      <RichDatetimePicker
        {...args}
        startDate={startDate}
        endDate={endDate}
        onChange={handleChange}
        className="rdp:w-85"
        // dateFormat="MMM dd, HH:mm"
      />
    </div>
  );
};

export const Default: Story = {
  args: {
    //
  },
  render: TemplateDatetimePicker,
};

export const OpenAtInitialization: Story = {
  args: {
    defaultOpen: true,
  },
  render: TemplateDatetimePicker,
};

export const CustomizeListItems: Story = {
  args: {
    localeKey: 'ko',
    children: (props) => {
      return props?.map(({ timeUnitToMilliseconds, formattedTimeUnit }, i) => {
        return (
          <div className="rdp:flex rdp:gap-2" key={i}>
            <div className="rdp:w-12">{formattedTimeUnit}</div>
            현재로부터 {timeUnitToMilliseconds}ms 전
          </div>
        );
      });
    },
  },
  render: TemplateDatetimePicker,
};

export const CutomizeToken: Story = {
  args: {
    seamToken: '~',
  },
  render: TemplateDatetimePicker,
};

/**
 * `customTimes`는 내장 기본 섹션에 **병합**된다(교체가 아니다).
 * 기본 섹션 헤더와 일치하는 키(`고정`)는 내용만 교체되고, 그 외 키(`연관`)는 새 섹션으로 추가된다.
 * 헤더 매칭은 언어에 무관하다 — 자세한 규칙은 i18n 스토리의 `CustomTimesMerge` 참고.
 */
export const CustomizeRelativeTimes: Story = {
  args: {
    localeKey: 'ko',
    customTimes: {
      연관: ['15m', '30m', 'yesterday'],
      고정: [`${format(new Date(), 'yyyy-MM-dd')}`, `${format(new Date(), 'MM/dd')}`],
    },
  },
  render: TemplateDatetimePicker,
};

/**
 * 연도 드롭다운. 목록은 `minDate`~`maxDate` 범위의 연도로 만든다.
 *
 * 기본값이 "1개월 전 ~ 내일"이라 **범위를 주지 않으면 연도가 한 해뿐이고 트리거가 비활성된다.**
 * 여러 해를 오가려면 아래처럼 범위를 넓게 준다.
 */
export const YearSelect: Story = {
  args: {
    minDate: new Date(new Date().getFullYear() - 5, 0, 1),
    maxDate: new Date(new Date().getFullYear() + 5, 11, 31),
    defaultOpen: true,
  },
  render: TemplateDatetimePicker,
};

export const PanelContainer: Story = {
  args: {
    getPanelContainer: () => document.querySelector('#panel-container'),
  },
  render: TemplateDatetimePicker,
};

/**
 * 범위 검증. `validateDatePickerRange`가 문자열을 반환하면 그 문구가 Apply 버튼 위에 표시된다.
 * `false`만 반환하면 로케일 기본 문구가 쓰인다.
 */
export const ValidateDateRange: Story = {
  args: {
    validateDatePickerRange: ([from, to]) => {
      if (from && to && subDays(to, 2) > from) {
        return 'Search duration may not be greater than 2 days.';
      }
      return true;
    },
  },
  render: TemplateDatetimePicker,
};

/**
 * `panelFooter`로 Apply 위 영역을 채운다.
 * 정적 노드는 기본 오류 문구와 함께 보이고, 함수를 넘기면 오류 표현을 통째로 가져간다.
 */
export const PanelFooter: Story = {
  args: {
    panelFooter: <span style={{ color: 'rgba(0,0,0,.45)' }}>조회는 최대 2일까지 가능해요</span>,
    validateDatePickerRange: ([from, to]) =>
      !(from && to && subDays(to, 2) > from) || '최대 2일까지 선택할 수 있어요',
  },
  render: TemplateDatetimePicker,
};

export const PanelFooterRenderProp: Story = {
  args: {
    validateDatePickerRange: () => false,
    panelFooter: ({ error }) =>
      error ? (
        <span style={{ color: '#F84302' }}>
          [{error.code}] {error.message}
        </span>
      ) : (
        <span style={{ color: 'rgba(0,0,0,.45)' }}>날짜를 고르고 Apply를 눌러보세요</span>
      ),
  },
  render: TemplateDatetimePicker,
};

/**
 * 다크모드 데모.
 *
 * 라이브러리는 다크 테마를 내장하지 않는다(패널은 흰 배경 고정). 대신:
 * - 트리거는 색을 고정하지 않고 호스트의 텍스트 색(currentColor)을 상속하므로,
 *   컨테이너에 밝은 색만 주면 텍스트·아이콘이 함께 밝아진다.
 * - 패널은 사용처에서 아래 `DARK_PANEL_CSS`처럼 클래스 오버라이드로 다크 처리한다.
 *   (검정 고정 토큰인 muted 텍스트·hover·border만 밝은 alpha로 덮으면 되고,
 *    나머지는 대부분 `color: inherit`이라 패널 색만 바꾸면 따라온다.)
 * - 캘린더 날짜·요일·비활성일·네비게이션 화살표도 패널 `color`만 주면 따라온다
 *   (글자는 `color: inherit`, 화살표 SVG는 `fill="currentColor"`).
 * - 커스텀타임 툴팁은 패널과 **별개의 표면**이라 패널 색을 덮어도 따라오지 않는다.
 *   배경·글자색이 짝으로 고정돼 있어 안 덮어도 읽히긴 하지만, 다크로 맞추려면
 *   `.rich-datetime-picker__tooltip`을 따로 덮어야 한다.
 *   stock react-datepicker CSS가 `.react-datepicker__day`에 `color: #000`을 **직접** 박아둬서
 *   루트에만 상속을 걸면 날짜에 닿지 않는데, 그건 `datepicker.scss`에서 끊어뒀다.
 *
 * 주의: 패널을 `getPanelContainer`로 다른 컨테이너에 포털하면 아래 래퍼(`.rdp-dark-demo`)
 * 밖으로 나가므로, 그 경우엔 포털 대상 컨테이너 기준으로 셀렉터를 잡아야 한다.
 */
const DARK_PANEL_CSS = `
.rdp-dark-demo .rich-datetime-picker__panel {
  background-color: #1f1f23;
  color: #e5e7eb;
}
.rdp-dark-demo .rich-datetime-picker__quick-col { border-right-color: rgba(255,255,255,0.15); }
.rdp-dark-demo .rich-datetime-picker__badge { border-color: rgba(255,255,255,0.15); }
.rdp-dark-demo .rich-datetime-picker .rich-datetime-picker__custom-trigger,
.rdp-dark-demo .rich-datetime-picker .rich-datetime-picker__copy,
.rdp-dark-demo .rich-datetime-picker__badge-icon,
.rdp-dark-demo .rich-datetime-picker__badge-sep {
  color: rgba(255,255,255,0.45);
}
.rdp-dark-demo .rich-datetime-picker .rich-datetime-picker__custom-trigger:hover,
.rdp-dark-demo .rich-datetime-picker .rich-datetime-picker__copy:hover {
  color: #e5e7eb;
}
/* 커스텀타임 툴팁은 패널과 별개의 표면이라 배경·글자색을 따로 덮는다. */
.rdp-dark-demo .rich-datetime-picker__tooltip {
  background-color: #26262b;
  color: #e5e7eb;
  border-color: rgba(255,255,255,0.15);
}
.rdp-dark-demo .rich-datetime-picker .rich-datetime-picker__quick-item:hover,
.rdp-dark-demo .rich-datetime-picker .rich-datetime-picker__quick-item.active,
.rdp-dark-demo .rich-datetime-picker__item:hover {
  background-color: rgba(255,255,255,0.08);
}
/* 캘린더 루트: react-datepicker 기본 CSS가 background:#fff 를 강제하므로 투명으로 덮는다.
   날짜·요일 글자색은 라이브러리가 이미 inherit이라 패널 color만 주면 따라온다. */
.rdp-dark-demo .react-datepicker {
  background-color: transparent;
  color: inherit;
  border-color: rgba(255,255,255,0.15);
}
.rdp-dark-demo .rich-datetime-picker__day:hover:not(.react-datepicker__day--disabled) {
  background-color: rgba(255,255,255,0.08);
  outline-color: #9338ff;
}
`;

const TemplateDarkDatetimePicker = (args: RichDatetimePickerProps) => {
  const now = new Date();
  const [startDate, setStartDate] = React.useState<Date | null>(subMinutes(now, 5));
  const [endDate, setEndDate] = React.useState<Date | null>(now);
  const handleChange = (params: DateRange) => {
    setStartDate(params[0]);
    setEndDate(params[1]);
  };
  return (
    <div
      className="rdp-dark-demo rdp:h-[400px] rdp:font-sans"
      style={{ background: '#18181b', color: '#e5e7eb', padding: 24 }}
    >
      <style>{DARK_PANEL_CSS}</style>
      <RichDatetimePicker
        {...args}
        startDate={startDate}
        endDate={endDate}
        onChange={handleChange}
        className="rdp:w-85"
      />
    </div>
  );
};

export const DarkMode: Story = {
  args: {
    defaultOpen: true,
  },
  parameters: {
    backgrounds: { default: 'dark' },
  },
  render: TemplateDarkDatetimePicker,
};

// 일(day) 단위 범위만 다루는 화면: 캘린더 상단 HH:mm:ss 배지를 숨기고 퀵레인지도 일 단위로 (NELO-2305)
export const HideTimeBadges: Story = {
  args: {
    defaultOpen: true,
    hideTimeBadges: true,
    timeUnits: ['24h', '7d', '14d', '28d'],
  },
  render: TemplateDatetimePicker,
};

// 트리거 좌측 아이콘을 숨긴다. `() => null`을 반환하면 아이콘 래퍼까지 렌더되지 않는다.
export const HideIcon: Story = {
  args: {
    renderIcon: () => null,
  },
  render: TemplateDatetimePicker,
};

// 기본 시계 아이콘 대신 커스텀 아이콘(여기선 이모지)을 노출한다.
export const CustomIcon: Story = {
  args: {
    renderIcon: () => <span aria-hidden="true">📅</span>,
  },
  render: TemplateDatetimePicker,
};

// 인자로 전달되는 기본 시계 아이콘을 그대로 감싸 패딩·색상만 조정한다.
export const WrapDefaultIcon: Story = {
  args: {
    renderIcon: (icon) => (
      <span className="rdp:text-primary" style={{ paddingRight: 8 }}>
        {icon}
      </span>
    ),
  },
  render: TemplateDatetimePicker,
};
