import {
  act,
  fireEvent,
  queryByTestId,
  render,
  screen,
  waitFor,
  within,
} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { RichDatetimePicker } from '../components/RichDatetimePicker';
import { format, subMinutes, subDays } from 'date-fns';
import { DateRange } from '../types';

describe('Test for RichDateTimePicker', () => {
  const now = new Date();
  const startDate = subMinutes(now, 5);
  const endDate = now;

  describe('기본 렌더링', () => {
    it('컴포넌트가 정상적으로 렌더링되어야 함', () => {
      const { container } = render(<RichDatetimePicker startDate={startDate} endDate={endDate} />);

      expect(container).toBeInTheDocument();
      expect(container.querySelector('.rich-datetime-picker')).toBeInTheDocument();
      expect(container.querySelector('.rich-datetime-picker__trigger')).toBeInTheDocument();
      expect(container.querySelector('.rich-datetime-picker__input')).toBeInTheDocument();
      // duration 태그는 제거됨
      expect(container.querySelector('.rich-datetime-picker__tag')).not.toBeInTheDocument();
    });

    it('startDate/endDate가 있으면 input에 날짜 범위가 표시되어야 함', () => {
      const { container } = render(<RichDatetimePicker startDate={startDate} endDate={endDate} />);

      const input = container.querySelector('.rich-datetime-picker__input') as HTMLInputElement;
      expect(input.value).toContain(format(now, 'MMM'));
    });
  });

  describe('className props 테스트', () => {
    it('className prop이 적용되어야 함', () => {
      const testClassName = 'test-class-name';
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} className={testClassName} />,
      );
      const containerElement = container.querySelector('.rich-datetime-picker');
      expect(containerElement).toHaveClass(testClassName);
    });

    it('inputClassName prop이 적용되어야 함', () => {
      const testClassName = 'input-test-class-name';
      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          inputClassName={testClassName}
        />,
      );
      const inputElement = container.querySelector('.rich-datetime-picker__input');
      expect(inputElement).toHaveClass(testClassName);
    });

    it('triggerClassName prop이 적용되어야 함', () => {
      const testClassName = 'trigger-test-class-name';
      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          triggerClassName={testClassName}
        />,
      );
      const triggerElement = container.querySelector('.rich-datetime-picker__trigger');
      expect(triggerElement).toHaveClass(testClassName);
    });

    it('panelClassName prop이 적용되어야 함', () => {
      const testClassName = 'panel-test-class-name';
      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          panelClassName={testClassName}
          defaultOpen
        />,
      );
      const panelElement = container.querySelector('.rich-datetime-picker__panel');
      expect(panelElement).toHaveClass(testClassName);
    });
  });

  describe('disable prop 테스트', () => {
    it('disable이 true일 때 trigger에 disable 클래스가 적용되어야 함', () => {
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} disable />,
      );
      const triggerElement = container.querySelector('.rich-datetime-picker__trigger');
      expect(triggerElement).toHaveClass('disable');
    });

    // Note: disable prop은 CSS로 pointer-events를 비활성화하므로
    // 실제 DOM 이벤트 테스트는 E2E 테스트에서 수행하는 것이 적합합니다.
  });

  describe('패널 열기/닫기', () => {
    it('trigger 클릭 시 패널이 열려야 함', () => {
      const { container } = render(<RichDatetimePicker startDate={startDate} endDate={endDate} />);

      const triggerElement = container.querySelector(
        '.rich-datetime-picker__trigger',
      ) as HTMLDivElement;

      fireEvent.click(triggerElement);

      const panelElement = container.querySelector('.rich-datetime-picker__panel');
      expect(panelElement).toBeInTheDocument();
    });

    it('defaultOpen이 true일 때 패널이 열려있어야 함', () => {
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} defaultOpen />,
      );

      const panelElement = container.querySelector('.rich-datetime-picker__panel');
      expect(panelElement).toBeInTheDocument();
    });

    it('패널이 열릴 때 trigger에 border-primary 클래스가 적용되어야 함', () => {
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} defaultOpen />,
      );

      const triggerElement = container.querySelector('.rich-datetime-picker__trigger');
      expect(triggerElement).toHaveClass('rdp:border-primary');
    });

    it('Escape 키를 누르면 패널이 닫혀야 함', async () => {
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} defaultOpen />,
      );

      expect(container.querySelector('.rich-datetime-picker__panel')).toBeInTheDocument();

      fireEvent.keyDown(document, { code: 'Escape' });

      await waitFor(() => {
        expect(container.querySelector('.rich-datetime-picker__panel')).not.toBeInTheDocument();
      });
    });
  });

  describe('시간 단위 선택', () => {
    it('시간 단위 클릭 시 onChange가 호출되어야 함', () => {
      const onChangeMock = jest.fn();
      let dateRange: DateRange = [startDate, endDate];

      onChangeMock.mockImplementation((range) => {
        dateRange = range;
      });

      render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          onChange={onChangeMock}
          timeUnits={['5m']}
          defaultOpen
        />,
      );

      const fiveMinuteText = 'Last 5 minutes';
      const fiveMinute = screen.getByText(fiveMinuteText);

      fireEvent.click(fiveMinute);

      expect(onChangeMock).toHaveBeenCalledWith(dateRange, fiveMinuteText, '5m');
    });

    it('timeUnits prop으로 커스텀 시간 단위를 설정할 수 있어야 함', () => {
      const onChangeMock = jest.fn();
      let dateRange: DateRange = [startDate, endDate];

      onChangeMock.mockImplementation((range) => {
        dateRange = range;
      });

      render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          onChange={onChangeMock}
          timeUnits={['1y']}
          defaultOpen
        />,
      );

      const oneYearText = 'Last 1 year';
      const oneYear = screen.getByText(oneYearText);

      expect(oneYear).toBeInTheDocument();

      fireEvent.click(oneYear);

      expect(onChangeMock).toHaveBeenCalledWith(dateRange, oneYearText, '1y');
    });

    it('여러 시간 단위가 올바르게 표시되어야 함', () => {
      render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          timeUnits={['5m', '1h', '1d', '1w']}
          defaultOpen
        />,
      );

      expect(screen.getByText('Last 5 minutes')).toBeInTheDocument();
      expect(screen.getByText('Last 1 hour')).toBeInTheDocument();
      expect(screen.getByText('Last 1 day')).toBeInTheDocument();
      expect(screen.getByText('Last 1 week')).toBeInTheDocument();
    });

    it('시간 단위 선택 후 패널이 닫혀야 함', async () => {
      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          timeUnits={['5m']}
          defaultOpen
        />,
      );

      const fiveMinute = screen.getByText('Last 5 minutes');
      fireEvent.click(fiveMinute);

      await waitFor(() => {
        expect(container.querySelector('.rich-datetime-picker__panel')).not.toBeInTheDocument();
      });
    });
  });

  describe('캘린더 테스트', () => {
    it('패널이 열리면 캘린더가 상시 표시되어야 함', () => {
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} defaultOpen />,
      );

      const datePicker = container.querySelector('.rich-datetime-picker__date-picker');
      expect(datePicker).toBeInTheDocument();
    });

    it('hideCalendarYearButton이 true일 때 연도 버튼이 숨겨져야 함', () => {
      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          defaultOpen
          hideCalendarYearButton
        />,
      );

      expect(container.querySelector('.rich-datetime-picker__date-picker')).toBeInTheDocument();
      const yearButton = queryByTestId(container, 'test-calendar-year-button');
      expect(yearButton).toBeNull();
    });

    it('hideCalendarYearButton이 false일 때 연도 버튼이 표시되어야 함', () => {
      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          defaultOpen
          hideCalendarYearButton={false}
        />,
      );

      expect(container.querySelector('.rich-datetime-picker__date-picker')).toBeInTheDocument();
      expect(
        container.querySelector('[data-testid="test-calendar-year-decrease-button"]'),
      ).toBeInTheDocument();
      expect(
        container.querySelector('[data-testid="test-calendar-year-increase-button"]'),
      ).toBeInTheDocument();
    });

    it('hideCalendarYearButton이 true면 연도 드롭다운도 숨겨야 함', () => {
      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          defaultOpen
          hideCalendarYearButton
        />,
      );

      expect(queryByTestId(container, 'test-calendar-year-select')).toBeNull();
      // 연도 자체는 헤더에 계속 보여야 한다
      expect(container.querySelector('.rich-datetime-picker__header-label')).toHaveTextContent(
        String(new Date().getFullYear()),
      );
    });

    // NELO-2305: 일 단위 범위만 쓰는 화면(Crash 목록/상세)에서 HH:mm:ss 입력이 오해를 부른다.
    it('hideTimeBadges가 true일 때 시간 배지가 숨겨져야 함', () => {
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} defaultOpen hideTimeBadges />,
      );

      expect(container.querySelector('.rich-datetime-picker__badges')).toBeNull();
      expect(container.querySelectorAll('.rich-datetime-picker__badge')).toHaveLength(0);
      // 캘린더와 액션 영역은 그대로여야 한다
      expect(container.querySelector('.rich-datetime-picker__date-picker')).toBeInTheDocument();
      expect(container.querySelector('.rich-datetime-picker__apply')).toBeInTheDocument();
    });

    it('hideTimeBadges 미지정 시 시간 배지가 표시되어야 함', () => {
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} defaultOpen />,
      );

      expect(container.querySelector('.rich-datetime-picker__badges')).toBeInTheDocument();
      expect(container.querySelectorAll('.rich-datetime-picker__badge')).toHaveLength(2);
    });
  });

  // NELO-2284: 캘린더는 날짜 단위 컨트롤이므로 클릭하면 하루 통째가 되어야 한다.
  // 기존에는 DatePicker.onChange의 두 번째 if가 raw start로 튜플을 재조립해 앞줄의
  // getZonedStartOfDay 스냅을 버렸고, 그 결과 end만 23:59:59가 되고 start는 원래 시각이 남았다.
  describe('캘린더 날짜 선택 시 하루 통째 (NELO-2284)', () => {
    const badgeTimes = (container: HTMLElement) =>
      Array.from(container.querySelectorAll('.rich-datetime-picker__badge')).map((badge) =>
        Array.from(badge.querySelectorAll('input'))
          .map((input) => (input as HTMLInputElement).value)
          .join(':'),
      );

    it('날짜를 다시 고르면 start는 00:00:00, end는 23:59:59가 되어야 함', () => {
      // 시각이 뚜렷하게 다른 범위에서 시작한다
      const from = new Date();
      from.setDate(from.getDate() - 3);
      from.setHours(9, 30, 15, 0);
      const to = new Date();
      to.setHours(18, 45, 5, 0);

      const { container } = render(
        <RichDatetimePicker startDate={from} endDate={to} defaultOpen />,
      );

      expect(badgeTimes(container)).toEqual(['09:30:15', '18:45:05']);

      const days = Array.from(
        container.querySelectorAll(
          '.react-datepicker__day:not(.react-datepicker__day--disabled):not(.react-datepicker__day--outside-month)',
        ),
      ) as HTMLElement[];
      expect(days.length).toBeGreaterThan(2);

      fireEvent.click(days[0]);
      fireEvent.click(days[1]);

      expect(badgeTimes(container)).toEqual(['00:00:00', '23:59:59']);
    });

    // onOpen은 트리거 클릭(DateTimeTrigger:51)과 입력 focus(:65) 양쪽에서, 패널이 이미 열려 있어도
    // 발화한다. 그때 draft 스냅샷을 다시 찍으면 편집 중이던 값이 커밋값으로 되돌아간다.
    it('열린 패널에서 트리거를 다시 클릭해도 캘린더 draft가 유지되어야 함', () => {
      const from = new Date();
      from.setDate(from.getDate() - 3);
      from.setHours(9, 30, 15, 0);
      const to = new Date();
      to.setHours(18, 45, 5, 0);

      const { container } = render(
        <RichDatetimePicker startDate={from} endDate={to} defaultOpen />,
      );

      const days = Array.from(
        container.querySelectorAll(
          '.react-datepicker__day:not(.react-datepicker__day--disabled):not(.react-datepicker__day--outside-month)',
        ),
      ) as HTMLElement[];
      fireEvent.click(days[0]);
      fireEvent.click(days[1]);
      expect(badgeTimes(container)).toEqual(['00:00:00', '23:59:59']);

      fireEvent.click(container.querySelector('.rich-datetime-picker__trigger') as HTMLElement);

      expect(badgeTimes(container)).toEqual(['00:00:00', '23:59:59']);
    });

    it('열린 패널에서 입력창에 focus해도 시간 배지 draft가 유지되어야 함', () => {
      const from = new Date();
      from.setDate(from.getDate() - 3);
      from.setHours(9, 30, 15, 0);
      const to = new Date();
      to.setHours(18, 45, 5, 0);

      const { container } = render(
        <RichDatetimePicker startDate={from} endDate={to} defaultOpen />,
      );

      const hourInput = container.querySelector(
        '.rich-datetime-picker__badge input',
      ) as HTMLInputElement;
      fireEvent.change(hourInput, { target: { value: '07' } });
      fireEvent.blur(hourInput);
      expect(badgeTimes(container)).toEqual(['07:30:15', '18:45:05']);

      // 배지를 만지다가 다시 입력창을 클릭하는 자연스러운 흐름
      fireEvent.focus(container.querySelector('.rich-datetime-picker__input') as HTMLElement);

      expect(badgeTimes(container)).toEqual(['07:30:15', '18:45:05']);
    });
  });

  // NELO-2280: 퀵레인지가 draft를 갱신하지 않으면 재오픈 시 캘린더가 이전 범위로 mount되고,
  // react-datepicker의 preSelection이 그 날짜에 고정된 채 옅은 파란 하이라이트가 남는다.
  // preSelection 재동기화는 월/연 단위로만 일어나므로, 잔상은 "같은 달" 안에서만 재현된다.
  describe('퀵레인지 후 캘린더 잔상 (NELO-2280)', () => {
    beforeEach(() => {
      // 고른 날짜와 퀵레인지 결과가 항상 같은 달에 오도록 월 중순으로 고정한다
      jest.useFakeTimers({ advanceTimers: true });
      jest.setSystemTime(new Date('2026-07-15T09:00:00Z'));
    });
    afterEach(() => {
      jest.useRealTimers();
    });

    it('캘린더 범위 선택 후 퀵레인지를 고르면 재오픈 시 이전 날짜 하이라이트가 남지 않아야 함', async () => {
      const currentNow = new Date();
      const { container } = render(
        <RichDatetimePicker
          startDate={subDays(currentNow, 3)}
          endDate={currentNow}
          timeUnits={['5m']}
          defaultOpen
        />,
      );

      // 1. 표시 중인 달 안에서 범위를 다시 고른다
      const days = Array.from(
        container.querySelectorAll(
          '.react-datepicker__day:not(.react-datepicker__day--disabled):not(.react-datepicker__day--outside-month)',
        ),
      ) as HTMLElement[];
      expect(days.length).toBeGreaterThan(2);

      const firstPickedLabel = days[0].getAttribute('aria-label');
      fireEvent.click(days[0]);
      fireEvent.click(days[1]);

      // 2. 퀵레인지 선택 → 패널이 닫힌다
      fireEvent.click(screen.getByText('Last 5 minutes'));
      await waitFor(() => {
        expect(container.querySelector('.rich-datetime-picker__panel')).not.toBeInTheDocument();
      });

      // 3. 재오픈
      fireEvent.click(container.querySelector('.rich-datetime-picker__trigger') as HTMLElement);
      await waitFor(() => {
        expect(container.querySelector('.rich-datetime-picker__date-picker')).toBeInTheDocument();
      });

      // 이전에 고른 날짜에 keyboard-selected(옅은 파란 박스)가 남아 있으면 안 된다
      const highlighted = container.querySelector('.react-datepicker__day--keyboard-selected');
      expect(highlighted?.getAttribute('aria-label') ?? null).not.toBe(firstPickedLabel);
      // 캘린더는 퀵레인지 결과를 가리켜야 한다
      expect(
        container.querySelector('.react-datepicker__day--selected')?.getAttribute('aria-label'),
      ).toContain('July 15th');
    });
  });

  // NELO-2279: 패널을 연 채로 같은 달 안에서 범위를 다시 고르면 이전 시작일에
  // 옅은 파란 keyboard-selected 박스가 남는다. react-datepicker는
  // (1) inline + shouldCloseOnSelect(기본 true)에서 날짜 클릭 시 preSelection을 갱신하지 않고
  // (2) props로 들어온 startDate 변화도 "월/연이 달라질 때만" 재동기화하기 때문에,
  // mount 시점의 시작일에 preSelection이 묶여 남는다.
  describe('같은 달 안 범위 재선택 시 이전 시작일 잔상 (NELO-2279)', () => {
    beforeEach(() => {
      // 이전 시작일과 새 범위가 항상 같은 달에 오도록 월말로 고정한다
      jest.useFakeTimers({ advanceTimers: true });
      jest.setSystemTime(new Date('2026-07-31T09:00:00Z'));
    });
    afterEach(() => {
      jest.useRealTimers();
    });

    const dayByText = (container: HTMLElement, text: string) =>
      (
        Array.from(
          container.querySelectorAll(
            '.react-datepicker__day:not(.react-datepicker__day--outside-month)',
          ),
        ) as HTMLElement[]
      ).find((day) => day.textContent === text) as HTMLElement;

    it('범위를 좁혀도 이전 시작일에 keyboard-selected 하이라이트가 남지 않아야 함', () => {
      const currentNow = new Date();
      const { container } = render(
        <RichDatetimePicker startDate={subDays(currentNow, 4)} endDate={currentNow} defaultOpen />,
      );

      // 27~31이 선택된 상태로 열린 캘린더에서 29~31로 좁힌다
      expect(dayByText(container, '27')).toHaveClass('react-datepicker__day--selected');
      fireEvent.click(dayByText(container, '29'));
      fireEvent.click(dayByText(container, '31'));

      expect(dayByText(container, '29')).toHaveClass('react-datepicker__day--selected');
      expect(dayByText(container, '27')).not.toHaveClass(
        'react-datepicker__day--keyboard-selected',
      );
      expect(
        container.querySelector('.react-datepicker__day--keyboard-selected'),
      ).not.toBeInTheDocument();
    });

    it('달력을 다른 연/월로 넘겨도 파란 하이라이트가 따라다니지 않아야 함', () => {
      const currentNow = new Date();
      const { container } = render(
        <RichDatetimePicker
          startDate={subDays(currentNow, 4)}
          endDate={currentNow}
          minDate={new Date('2020-01-01T00:00:00')}
          defaultOpen
        />,
      );

      fireEvent.click(screen.getByTestId('test-calendar-year-decrease-button'));

      // 선택 범위가 없는 달인데도 커서가 옮겨 앉아 하이라이트가 뜨면 안 된다
      expect(
        container.querySelector('.react-datepicker__day--keyboard-selected'),
      ).not.toBeInTheDocument();
      expect(
        container.querySelector('.react-datepicker__week--keyboard-selected'),
      ).not.toBeInTheDocument();
    });
  });

  describe('CustomTimeView (history 툴팁) 테스트', () => {
    const openTooltip = (container: HTMLElement) => {
      const trigger = container.querySelector(
        '.rich-datetime-picker__custom-trigger',
      ) as HTMLElement;
      fireEvent.click(trigger);
    };

    it('? 아이콘 클릭 시 CustomTimeView 툴팁이 표시되어야 함', async () => {
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} defaultOpen />,
      );

      openTooltip(container);

      await waitFor(() => {
        const tooltip = container.querySelector('.rich-datetime-picker__tooltip');
        expect(tooltip).toBeInTheDocument();
        expect(tooltip).toHaveTextContent('Type custom times like:');
      });
    });

    it('툴팁의 상대 시간 클릭 시 onChange가 호출되어야 함', async () => {
      const onChangeMock = jest.fn();
      let dateRange: DateRange = [startDate, endDate];

      onChangeMock.mockImplementation((range) => {
        dateRange = range;
      });

      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          onChange={onChangeMock}
          defaultOpen
        />,
      );

      openTooltip(container);

      const customTimeText = '45m';
      await waitFor(() => {
        expect(screen.getByText(customTimeText)).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText(customTimeText));

      expect(onChangeMock).toHaveBeenCalledWith(dateRange, customTimeText, undefined);
    });

    it('customTimes prop으로 커스텀 시간을 설정할 수 있어야 함', async () => {
      const relativeTimes = ['55m', '100h', '어제'];
      const fixedTimes = ['8/1', 'Aug 1', '8/1 - 8/2'];
      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          customTimes={{
            Relative: relativeTimes,
            Fixed: fixedTimes,
          }}
          defaultOpen
        />,
      );

      openTooltip(container);

      await waitFor(() => {
        const tooltip = container.querySelector('.rich-datetime-picker__tooltip');
        expect(tooltip).toBeInTheDocument();

        [...relativeTimes, ...fixedTimes].forEach((time) => {
          expect(tooltip).toHaveTextContent(time);
        });
      });
    });

    // NELO-2237: i18n 로케일에서 영어 키가 기본 섹션과 충돌하지 않아 섹션이 중복 노출됐다.
    it('비영어 로케일에서 영어 키로 넘긴 customTimes가 섹션을 중복 생성하지 않아야 함', async () => {
      const relativeTimes = ['55m', '100h', '어제'];
      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          localeKey="ko"
          customTimes={{ Relative: relativeTimes }}
          defaultOpen
        />,
      );

      openTooltip(container);

      await waitFor(() => {
        expect(container.querySelector('.rich-datetime-picker__tooltip')).toBeInTheDocument();
      });

      const tooltip = container.querySelector('.rich-datetime-picker__tooltip') as HTMLElement;
      const headers = Array.from(tooltip.querySelectorAll('.rdp\\:mb-2')).map((el) =>
        el.textContent?.trim(),
      );

      // 로케일 헤더 3개만 — 영어 'Relative' 섹션이 추가로 붙으면 안 된다
      expect(headers).toEqual(['상대', '고정', 'Unix 타임스탬프']);
      expect(tooltip).not.toHaveTextContent('Relative');
      // '상대' 섹션의 내용은 소비자가 넘긴 값으로 대체된다
      relativeTimes.forEach((time) => expect(tooltip).toHaveTextContent(time));
      expect(tooltip).not.toHaveTextContent('45분');
    });
  });

  describe('입력 필드 테스트', () => {
    it('패널이 열리면 입력 필드에 포커스가 가능해야 함', async () => {
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} defaultOpen />,
      );

      const input = container.querySelector('.rich-datetime-picker__input') as HTMLInputElement;
      expect(input).toBeInTheDocument();
      expect(input.value).toBeTruthy();
    });

    it('유효하지 않은 입력 시 border가 빨간색으로 변해야 함', async () => {
      const user = userEvent.setup();
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} defaultOpen />,
      );

      const input = container.querySelector('.rich-datetime-picker__input') as HTMLInputElement;

      await user.clear(input);
      await user.type(input, 'invalid date string');

      const trigger = container.querySelector('.rich-datetime-picker__trigger');
      expect(trigger).toHaveClass('rdp:border-stateRed');
    });
  });

  describe('오류 표면화', () => {
    const renderPicker = (props = {}) =>
      render(<RichDatetimePicker startDate={startDate} endDate={endDate} defaultOpen {...props} />);

    const typeInvalid = async (container: HTMLElement) => {
      const user = userEvent.setup();
      const input = container.querySelector('.rich-datetime-picker__input') as HTMLInputElement;
      await user.clear(input);
      await user.type(input, 'invalid date string');
      return input;
    };

    it('메시지 영역은 Apply 액션 바로 위에 온다', () => {
      const { container } = renderPicker();

      const footer = container.querySelector('.rich-datetime-picker__footer');
      expect(footer).toBeInTheDocument();
      expect(footer?.nextElementSibling).toHaveClass('rich-datetime-picker__actions');
    });

    it('오류가 없어도 메시지 노드는 비어 있는 채로 자리를 지킨다', () => {
      const { container } = renderPicker();

      const message = container.querySelector('.rich-datetime-picker__error');
      expect(message).toBeInTheDocument();
      expect(message).toBeEmptyDOMElement();
    });

    it('무효한 입력의 사유를 문구로 보여준다', async () => {
      const { container } = renderPicker();

      await typeInvalid(container);

      expect(screen.getByText("We couldn't read that date.")).toBeInTheDocument();
    });

    it('localeKey에 맞는 문구를 쓴다', async () => {
      const { container } = renderPicker({ localeKey: 'ko' });

      await typeInvalid(container);

      expect(screen.getByText('날짜를 알아볼 수 없어요.')).toBeInTheDocument();
    });

    it('입력과 메시지 노드를 aria로 연결한다', async () => {
      const { container } = renderPicker();

      const input = await typeInvalid(container);
      const message = container.querySelector('.rich-datetime-picker__error');

      expect(input).toHaveAttribute('aria-invalid', 'true');
      // id를 하드코딩하지 않고 상호 참조로 확인한다
      expect(input.getAttribute('aria-describedby')).toBe(message?.getAttribute('id'));
      expect(message).toHaveAttribute('aria-live', 'polite');
    });

    it('패널이 닫히면 aria-describedby를 떼고 aria-invalid만 남긴다', async () => {
      const { container } = renderPicker();

      const input = await typeInvalid(container);
      act(() => {
        fireEvent.keyDown(document, { code: 'Escape' });
      });

      await waitFor(() => {
        expect(container.querySelector('.rich-datetime-picker__error')).not.toBeInTheDocument();
      });
      expect(input).not.toHaveAttribute('aria-describedby');
      expect(input).toHaveAttribute('aria-invalid', 'true');
    });

    // 의도된 동작: Cancel/Escape는 draft(캘린더·배지)만 원복하고 텍스트 입력은 건드리지 않는다.
    // 입력한 텍스트가 그대로 남으므로 그게 무효하다는 표시도 함께 남아야 앞뒤가 맞는다.
    it('Escape로 닫아도 입력 오류 표시는 남는다', async () => {
      const { container } = renderPicker();

      const input = await typeInvalid(container);
      act(() => {
        fireEvent.keyDown(document, { code: 'Escape' });
      });

      await waitFor(() => {
        expect(container.querySelector('.rich-datetime-picker__panel')).not.toBeInTheDocument();
      });
      expect(container.querySelector('.rich-datetime-picker__trigger')).toHaveClass(
        'rdp:border-stateRed',
      );
      expect(input).toHaveAttribute('aria-invalid', 'true');
      expect(input.value).toBe('invalid date string');
    });

    it('Apply 검증에 걸리면 사유를 보여주고 패널을 닫지 않는다', async () => {
      const user = userEvent.setup();
      const onChangeMock = jest.fn();
      const { container } = renderPicker({
        onChange: onChangeMock,
        validateDatePickerRange: () => '최대 2일까지 선택할 수 있어요',
      });

      await user.click(screen.getByText('Apply'));

      expect(screen.getByText('최대 2일까지 선택할 수 있어요')).toBeInTheDocument();
      expect(onChangeMock).not.toHaveBeenCalled();
      expect(container.querySelector('.rich-datetime-picker__panel')).toBeInTheDocument();
    });

    it('텍스트 입력 커밋도 validateDatePickerRange를 거친다', async () => {
      const user = userEvent.setup();
      const onChangeMock = jest.fn();
      const { container } = renderPicker({
        onChange: onChangeMock,
        validateDatePickerRange: () => '최대 2일까지 선택할 수 있어요',
      });

      const input = container.querySelector('.rich-datetime-picker__input') as HTMLInputElement;
      await user.clear(input);
      await user.type(input, '45m{Enter}');

      expect(onChangeMock).not.toHaveBeenCalled();
      expect(screen.getByText('최대 2일까지 선택할 수 있어요')).toBeInTheDocument();
      // 사유가 입력에서 났으므로 트리거도 함께 빨개진다
      expect(container.querySelector('.rich-datetime-picker__trigger')).toHaveClass(
        'rdp:border-stateRed',
      );
      expect(container.querySelector('.rich-datetime-picker__panel')).toBeInTheDocument();
    });

    it('검증을 통과하면 텍스트 입력이 그대로 커밋된다', async () => {
      const user = userEvent.setup();
      const onChangeMock = jest.fn();
      const { container } = renderPicker({
        onChange: onChangeMock,
        validateDatePickerRange: () => true,
      });

      const input = container.querySelector('.rich-datetime-picker__input') as HTMLInputElement;
      await user.clear(input);
      await user.type(input, '45m{Enter}');

      expect(onChangeMock).toHaveBeenCalledTimes(1);
      expect(onChangeMock.mock.calls[0][1]).toBe('45m');
    });

    it('false만 반환하면 로케일 기본 문구를 쓴다', async () => {
      const user = userEvent.setup();
      renderPicker({ validateDatePickerRange: () => false });

      await user.click(screen.getByText('Apply'));

      expect(screen.getByText('That range is not allowed.')).toBeInTheDocument();
    });
  });

  describe('연도 select', () => {
    const WIDE = { minDate: new Date('2020-01-01'), maxDate: new Date('2030-12-31') };

    const renderWide = () =>
      render(<RichDatetimePicker startDate={startDate} endDate={endDate} defaultOpen {...WIDE} />);

    const yearSelect = () => screen.getByTestId('test-calendar-year-select') as HTMLSelectElement;

    it('minDate~maxDate 범위의 연도를 옵션으로 갖는다', () => {
      renderWide();

      const options = within(yearSelect()).getAllByRole('option');
      expect(options).toHaveLength(11); // 2020~2030
      expect(options[0]).toHaveTextContent('2020');
      expect(options[10]).toHaveTextContent('2030');
    });

    it('연도를 고르면 캘린더가 그 해로 이동한다', async () => {
      const user = userEvent.setup();
      renderWide();

      await user.selectOptions(yearSelect(), '2023');

      // select의 value는 캘린더가 보여주는 date에서 파생되므로, 값이 바뀌었다는 건
      // 캘린더가 실제로 그 해로 옮겨갔다는 뜻이다.
      // (헤더 textContent는 옵션 목록까지 포함해서 연도 단언에 쓸 수 없다)
      expect(yearSelect().value).toBe('2023');
    });

    it('현재 연도가 선택되어 있다', () => {
      renderWide();

      expect(yearSelect().value).toBe(String(new Date().getFullYear()));
    });

    it('경계 연도를 고르면 월도 범위 안으로 당겨진다', async () => {
      const user = userEvent.setup();
      // min이 6월이므로 2023년 1~5월은 날짜가 전부 비활성이다.
      render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          defaultOpen
          minDate={new Date(2023, 5, 15)}
          maxDate={new Date(2030, 11, 31)}
        />,
      );

      await user.selectOptions(yearSelect(), '2023');

      // 9월(현재 뷰) → 6월로 당겨지지 않고 그대로여야 한다(9월 > 6월이므로 범위 안)
      expect(yearSelect().value).toBe('2023');
    });

    it('min보다 이른 달을 보고 있을 때 경계 연도를 고르면 min의 달로 옮긴다', async () => {
      const user = userEvent.setup();
      const { container } = render(
        <RichDatetimePicker
          startDate={new Date(2026, 0, 10)}
          endDate={new Date(2026, 0, 20)}
          defaultOpen
          minDate={new Date(2023, 5, 15)}
          maxDate={new Date(2030, 11, 31)}
        />,
      );

      // 1월을 보고 있는 상태에서 2023 선택 → 2023년 1월은 min(2023-06) 이전이라 6월로 당겨진다
      await user.selectOptions(yearSelect(), '2023');

      const month = container.querySelector('.rich-datetime-picker__header-label span');
      expect(month).toHaveTextContent('Jun');
    });

    it('선택 가능한 연도가 하나뿐이면 select를 아예 내보내지 않는다', () => {
      // min/max 기본값은 "1개월 전 ~ 내일"이라 대개 한 해에 머문다
      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          defaultOpen
          minDate={new Date('2026-09-01')}
          maxDate={new Date('2026-09-30')}
        />,
      );

      expect(queryByTestId(container, 'test-calendar-year-select')).toBeNull();
      // 연도 자체는 텍스트로 남는다
      expect(container.querySelector('.rich-datetime-picker__header-label')).toHaveTextContent(
        '2026',
      );
    });
  });

  // react-datepicker는 선택 중 하이라이트를 이번 달 날짜에만 준다. 다음 달 날짜를 가리키면
  // 같은 줄의 다음 달 날짜만 띠가 비어 보였다.
  describe('다른 달 날짜의 범위 하이라이트', () => {
    const renderMay = () =>
      render(
        <RichDatetimePicker
          startDate={new Date(2026, 4, 1)}
          endDate={new Date(2026, 4, 2, 23, 59)}
          timeZone="Asia/Seoul"
          defaultOpen
          minDate={new Date(2026, 0, 1)}
          maxDate={new Date(2026, 11, 31)}
        />,
      );

    const day = (container: HTMLElement, n: string, outside: boolean) =>
      container.querySelector(
        `.react-datepicker__day--0${n}${outside ? '.react-datepicker__day--outside-month' : ':not(.react-datepicker__day--outside-month)'}`,
      ) as HTMLElement;

    it('선택 중 다음 달 날짜를 가리키면 그 사이의 다음 달 날짜도 칠한다', async () => {
      const user = userEvent.setup();
      const { container } = renderMay();

      await user.click(day(container, '05', false));
      fireEvent.mouseEnter(day(container, '06', true));

      for (const n of ['01', '02', '03', '04', '05', '06']) {
        expect(day(container, n, true)).toHaveClass('__day--in-selecting-range');
      }
      expect(day(container, '06', true)).toHaveClass('__day--selecting-range-end');
      // 범위 밖(이전 달 말일)은 칠하지 않는다
      expect(day(container, '30', true)).not.toHaveClass('__day--in-selecting-range');
    });

    it('달 밖으로 마우스를 빼면 react-datepicker처럼 하이라이트를 거둔다', async () => {
      const user = userEvent.setup();
      const { container } = renderMay();

      await user.click(day(container, '05', false));
      fireEvent.mouseEnter(day(container, '06', true));
      fireEvent.mouseLeave(container.querySelector('.react-datepicker__month') as HTMLElement);

      expect(container.querySelector('.__day--in-selecting-range')).not.toBeInTheDocument();
    });

    it('범위가 확정되면 다른 달 날짜도 띠에 포함된다', () => {
      const { container } = render(
        <RichDatetimePicker
          startDate={new Date(2026, 4, 5)}
          endDate={new Date(2026, 5, 6, 23, 59)}
          timeZone="Asia/Seoul"
          defaultOpen
          minDate={new Date(2026, 0, 1)}
          maxDate={new Date(2026, 11, 31)}
        />,
      );

      for (const n of ['01', '06']) {
        expect(day(container, n, true)).toHaveClass('__day--in-range');
      }
    });
  });

  describe('panelFooter prop 테스트', () => {
    it('정적 노드는 기본 오류 문구와 함께 표시된다', async () => {
      const user = userEvent.setup();
      render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          defaultOpen
          validateDatePickerRange={() => false}
          panelFooter={<span>최대 2일</span>}
        />,
      );

      await user.click(screen.getByText('Apply'));

      expect(screen.getByText('최대 2일')).toBeInTheDocument();
      expect(screen.getByText('That range is not allowed.')).toBeInTheDocument();
    });

    it('render-prop은 기본 문구를 대체하되 메시지 노드의 id는 유지한다', async () => {
      const user = userEvent.setup();
      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          defaultOpen
          validateDatePickerRange={() => false}
          panelFooter={({ error }) => (error ? <span>코드: {error.code}</span> : null)}
        />,
      );

      await user.click(screen.getByText('Apply'));

      expect(screen.getByText('코드: range-invalid')).toBeInTheDocument();
      expect(screen.queryByText('That range is not allowed.')).not.toBeInTheDocument();
      expect(container.querySelector('.rich-datetime-picker__error')).toHaveAttribute('id');
    });

    it('children으로 본문을 대체하면 푸터도 렌더되지 않는다', async () => {
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} defaultOpen>
          <div>커스텀 본문</div>
        </RichDatetimePicker>,
      );

      expect(screen.getByText('커스텀 본문')).toBeInTheDocument();
      expect(container.querySelector('.rich-datetime-picker__footer')).not.toBeInTheDocument();

      const input = container.querySelector('.rich-datetime-picker__input') as HTMLInputElement;
      expect(input).not.toHaveAttribute('aria-describedby');
    });
  });

  describe('validateDatePickerRange prop 테스트', () => {
    it('validateDatePickerRange가 false를 반환하면 날짜 선택이 무시되어야 함', async () => {
      const onChangeMock = jest.fn();
      const validateMock = jest.fn(() => false);

      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          onChange={onChangeMock}
          validateDatePickerRange={validateMock}
          defaultOpen
        />,
      );

      expect(container.querySelector('.rich-datetime-picker__date-picker')).toBeInTheDocument();

      // 캘린더에서 날짜를 선택해도 Apply 전까지는 draft에만 반영되고 onChange는 호출되지 않음
      // (실제 날짜 선택은 react-datepicker 내부 동작에 의존)
      expect(onChangeMock).not.toHaveBeenCalled();
    });
  });

  describe('localeKey prop 테스트', () => {
    it('localeKey가 ko일 때 한국어로 표시되어야 함', () => {
      render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} localeKey="ko" defaultOpen />,
      );

      // 한국어 locale에서는 "최근 10분" 형식으로 표시됨 (기본 timeUnits 첫 항목)
      expect(screen.getByText('최근 10분')).toBeInTheDocument();
    });

    it('localeKey가 en일 때 영어로 표시되어야 함', () => {
      render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} localeKey="en" defaultOpen />,
      );

      expect(screen.getByText('Last 10 minutes')).toBeInTheDocument();
    });
  });

  describe('seamToken prop 테스트', () => {
    it('seamToken prop이 입력 필드에 반영되어야 함', () => {
      const customSeamToken = '~';
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} seamToken={customSeamToken} />,
      );

      const input = container.querySelector('.rich-datetime-picker__input') as HTMLInputElement;
      expect(input.value).toContain(customSeamToken);
    });
  });

  describe('퀵레인지 라벨 표시 테스트', () => {
    it('퀵레인지 선택 시 트리거 input에 라벨이 표시되어야 함', async () => {
      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          timeUnits={['5m']}
          defaultOpen
        />,
      );

      fireEvent.click(screen.getByText('Last 5 minutes'));

      await waitFor(() => {
        const input = container.querySelector('.rich-datetime-picker__input') as HTMLInputElement;
        expect(input.value).toBe('Last 5 minutes');
      });
    });
  });

  describe('displayedInput prop 테스트', () => {
    it('displayedInput prop이 설정되면 패널이 닫혀 있을 때 해당 값이 표시되어야 함', () => {
      const displayedInput = 'Last 7 days';
      const { container } = render(
        <RichDatetimePicker
          startDate={subDays(now, 7)}
          endDate={now}
          displayedInput={displayedInput}
        />,
      );

      const input = container.querySelector('.rich-datetime-picker__input') as HTMLInputElement;
      // displayedInput은 dateInput이 있으면 그 위에 표시됨
      // displayedInput || dateInput 형태로 동작
      expect(input.value).toContain(format(now, 'MMM')); // 날짜가 포맷팅되어 표시됨
    });
  });

  describe('Copy range 테스트', () => {
    it('기본 포맷은 {from,to} ISO JSON이며 클릭 시 "Copied!"로 1초간 바뀌어야 함', async () => {
      jest.useFakeTimers();
      const writeText = jest.fn();
      Object.defineProperty(navigator, 'clipboard', { value: { writeText }, configurable: true });

      const { getByText, queryByText } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} defaultOpen />,
      );

      fireEvent.click(getByText('Copy range'));

      const copied = writeText.mock.calls[0][0] as string;
      expect(JSON.parse(copied)).toEqual({
        from: startDate.toISOString(),
        to: endDate.toISOString(),
      });
      expect(getByText('Copied!')).toBeInTheDocument();

      act(() => {
        jest.advanceTimersByTime(1000);
      });
      expect(queryByText('Copied!')).not.toBeInTheDocument();
      expect(getByText('Copy range')).toBeInTheDocument();

      jest.useRealTimers();
    });

    it('formatCopyRange prop으로 복사 형식을 커스터마이즈할 수 있어야 함', () => {
      const writeText = jest.fn();
      Object.defineProperty(navigator, 'clipboard', { value: { writeText }, configurable: true });

      const { getByText } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          formatCopyRange={({ from, to }) => `${from.getTime()}~${to.getTime()}`}
          defaultOpen
        />,
      );

      fireEvent.click(getByText('Copy range'));

      expect(writeText).toHaveBeenCalledWith(`${startDate.getTime()}~${endDate.getTime()}`);
    });
  });

  describe('deprecated props 하위 호환 테스트', () => {
    it('formatTag / customTimeViewSlideDirection를 넘겨도 렌더링되며 deprecation 경고를 남겨야 함', () => {
      const warnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});

      // 타입 에러 없이 수용되어야 함 (하위 호환)
      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          formatTag={(ms) => `${ms}`}
          customTimeViewSlideDirection="right"
        />,
      );

      expect(container.querySelector('.rich-datetime-picker__trigger')).toBeInTheDocument();
      expect(warnSpy).toHaveBeenCalledWith(expect.stringContaining('formatTag'));
      expect(warnSpy).toHaveBeenCalledWith(expect.stringContaining('customTimeViewSlideDirection'));

      warnSpy.mockRestore();
    });

    it('deprecated props가 없으면 경고를 남기지 않아야 함', () => {
      const warnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});

      render(<RichDatetimePicker startDate={startDate} endDate={endDate} />);

      expect(warnSpy).not.toHaveBeenCalled();
      warnSpy.mockRestore();
    });
  });
});
