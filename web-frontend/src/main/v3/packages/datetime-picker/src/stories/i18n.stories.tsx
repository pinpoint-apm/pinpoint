import React from 'react';
import type { Meta, StoryObj } from '@storybook/react-vite';
import { RichDatetimePicker, RichDatetimePickerProps } from '../components/RichDatetimePicker';
import { subMinutes } from 'date-fns';
import { DateRange } from '../types';

const meta = {
  title: 'Rich/RichDatetimePicker/i18n',
  component: RichDatetimePicker,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
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
    <div className="rdp:h-[560px] rdp:font-sans">
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

/**
 * 한국어. 라벨 "최근 60분", 캘린더는 date-fns `ko`.
 * 커스텀타임 예시(`45분` 등)는 실제 파싱되는 한국어 상대시간 문자열이다.
 */
export const Korean: Story = {
  args: {
    localeKey: 'ko',
    defaultOpen: true,
  },
  render: TemplateDatetimePicker,
};

/**
 * 일본어. 퀵레인지 라벨은 "過去60分" 형태, 캘린더는 date-fns `ja` 로케일.
 * 좌상단 `?` 툴팁의 커스텀타임 예시는 실제 파싱되는 일본어 상대시간 문자열이다.
 */
export const Japanese: Story = {
  args: {
    localeKey: 'ja',
    defaultOpen: true,
  },
  render: TemplateDatetimePicker,
};

/**
 * 중국어 간체(zh-CN). 라벨 "最近60分钟", 캘린더는 date-fns `zh-CN`.
 */
export const ChineseSimplified: Story = {
  args: {
    localeKey: 'zh-CN',
    defaultOpen: true,
  },
  render: TemplateDatetimePicker,
};

/**
 * 중국어 번체(zh-TW). 라벨 "最近60分鐘", 캘린더는 date-fns `zh-TW`.
 */
export const ChineseTraditional: Story = {
  args: {
    localeKey: 'zh-TW',
    defaultOpen: true,
  },
  render: TemplateDatetimePicker,
};

/**
 * `customTimes` 병합 규칙 (좌상단 `?` → "Type custom times" 툴팁에서 확인).
 *
 * 섹션 identity는 표시 헤더가 아니라 내부 section id(`relative`/`fixed`/`unix`) 기준이다.
 * 따라서 **어느 언어의 기본 헤더로 넘겨도** 대응 기본 섹션의 내용만 교체되고,
 * 헤더는 현재 `localeKey`의 문구가 유지된다. 알 수 없는 키만 새 섹션으로 추가된다.
 *
 * 아래는 `localeKey="ko"`인데 영어 키 `Relative`/`Fixed`로 넘긴 경우다.
 * 렌더 결과는 `상대`(내용 교체) / `고정`(내용 교체) / `Unix 타임스탬프`(기본) / `연관`(추가) 4개 —
 * 영어 `Relative` 섹션이 따로 생기지 않는다.
 */
export const CustomTimesMerge: Story = {
  args: {
    localeKey: 'ko',
    defaultOpen: true,
    customTimes: {
      Relative: ['15m', '30m', '어제'],
      Fixed: ['8/1', '8/1 - 8/2'],
      연관: ['45분', '2주'],
    },
  },
  render: TemplateDatetimePicker,
};
