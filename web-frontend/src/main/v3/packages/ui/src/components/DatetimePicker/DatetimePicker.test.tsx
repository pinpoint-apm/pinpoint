import React from 'react';
import { act, render, screen } from '@testing-library/react';
import { RichDatetimePickerProps } from '@pinpoint-fe/datetime-picker';

let mockTimezone = 'Asia/Seoul';
let pickerProps: RichDatetimePickerProps;

// 실제 picker 대신 받은 라벨을 그대로 그린다. 감싸는 쪽이 어떤 라벨을 내려주는지만 본다.
jest.mock('@pinpoint-fe/datetime-picker', () => ({
  RichDatetimePicker: (props: RichDatetimePickerProps) => {
    pickerProps = props;
    return <div data-testid="label">{props.displayedInput}</div>;
  },
}));
jest.mock('react-fast-marquee', () => ({ __esModule: true, default: () => null }));
jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  useSearchParameters: () => ({
    application: { applicationName: 'app', serviceType: 'TOMCAT' },
  }),
  useLanguage: () => ['en'],
  useDateFormat: () => ['MMM do, hh:mm a'],
  useTimezone: () => [mockTimezone],
}));
// 실제 `lib`는 ECharts(ESM)까지 끌고 와 jest가 읽지 못한다. 여기서 쓰는 것은 `cn`뿐이다.
jest.mock('../../lib', () => ({
  cn: (...classNames: unknown[]) => classNames.filter(Boolean).join(' '),
}));
jest.mock('../Toast', () => ({ useReactToastifyToast: () => ({ warn: jest.fn() }) }));
jest.mock('../ui', () => {
  const Pass = ({ children }: { children?: React.ReactNode }) => <>{children}</>;
  return {
    Button: ({ children }: { children?: React.ReactNode }) => <button>{children}</button>,
    Tooltip: Pass,
    TooltipContent: () => null,
    TooltipProvider: Pass,
    TooltipTrigger: Pass,
  };
});

import { DatetimePicker } from './DatetimePicker';

// 이전 범위(20분)와, '5분 전'을 고르면 picker가 넘겨주는 범위. 둘 다 Asia/Seoul 기준 문자열이다.
const BEFORE = { from: '2026-10-08-15-47-59', to: '2026-10-08-16-07-59' };
const FIVE_MINUTES = {
  from: new Date('2026-10-08T07:18:03Z'),
  to: new Date('2026-10-08T07:23:03Z'),
};
const FIVE_MINUTES_KEY = { from: '2026-10-08-16-18-03', to: '2026-10-08-16-23-03' };

/** URL이 바뀐 것처럼 from/to만 다시 내려준다. 호출부들은 `formattedDates`를 그대로 URL에 싣는다. */
const renderPicker = () => {
  const onChange = jest.fn();
  const utils = render(<DatetimePicker {...BEFORE} onChange={onChange} />);
  const navigate = (range: { from: string; to: string }) =>
    utils.rerender(<DatetimePicker {...range} onChange={onChange} />);
  const pick = (text: string) =>
    act(() => {
      pickerProps.onChange?.([FIVE_MINUTES.from, FIVE_MINUTES.to], text);
    });
  return { onChange, navigate, pick };
};

const label = () => screen.getByTestId('label').textContent;

describe('DatetimePicker', () => {
  beforeEach(() => {
    mockTimezone = 'Asia/Seoul';
  });

  test('shows the label of the range that was picked', () => {
    const { onChange, navigate, pick } = renderPicker();

    pick('Last 5 minutes');
    expect(onChange.mock.calls[0][0].formattedDates).toEqual(FIVE_MINUTES_KEY);
    navigate(FIVE_MINUTES_KEY);

    expect(label()).toBe('Last 5 minutes');
  });

  // 뒤로 가기로 범위가 돌아가면 그 라벨은 남의 이름이다. 날짜가 보여야 한다.
  test('drops the label when the range changes from outside the picker', () => {
    const { navigate, pick } = renderPicker();
    pick('Last 5 minutes');
    navigate(FIVE_MINUTES_KEY);

    navigate(BEFORE);

    expect(label()).toBe('');
  });

  // 앞으로 가기로 그 범위에 돌아와도 라벨은 돌아오지 않는다. 그 사이 시간이 흘렀으면 더는 '5분 전'이 아니다.
  test('does not show the label again when the range comes back', () => {
    const { navigate, pick } = renderPicker();
    pick('Last 5 minutes');
    navigate(FIVE_MINUTES_KEY);
    navigate(BEFORE);

    navigate(FIVE_MINUTES_KEY);

    expect(label()).toBe('');
  });

  // 고른 뒤 URL이 바뀌기까지 이전 범위가 들어온다. 그동안 감추면 라벨이 깜빡인다.
  test('keeps the new label while the range has not followed yet', () => {
    const { pick } = renderPicker();

    pick('Last 5 minutes');

    expect(label()).toBe('Last 5 minutes');
  });

  // 범위를 문자열로 비교하므로 설정 시간대가 브라우저 시간대와 달라도 같은 범위로 본다.
  test('matches the range in the configured timezone', () => {
    mockTimezone = 'America/New_York';
    const { onChange, navigate, pick } = renderPicker();

    pick('Last 5 minutes');
    navigate(onChange.mock.calls[0][0].formattedDates);

    expect(label()).toBe('Last 5 minutes');
  });

  test('shows no label for a range picked as absolute dates', () => {
    const { navigate, pick } = renderPicker();

    pick('');
    navigate(FIVE_MINUTES_KEY);

    expect(label()).toBe('');
  });
});
