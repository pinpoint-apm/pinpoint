import React from 'react';
import '@testing-library/jest-dom';
import { fireEvent, queryByTestId, render, screen, waitFor } from '@testing-library/react';
import { RichDatetimePicker } from '../components/RichDatetimePicker';
import { subMinutes } from 'date-fns';
import { DateRange } from '..';

describe('Test for RichDateTimePicker', () => {
  const now = new Date();
  const startDate = subMinutes(now, 5);
  const endDate = now;

  describe('test <RichDatetimePicker>', () => {
    it('renders component and checks existence', async () => {
      const { container } = render(<RichDatetimePicker startDate={startDate} endDate={endDate} />);

      expect(container).toBeInTheDocument();
    });

    it('test for prop: `classNmae` ', () => {
      const testClassName = 'test-class-name';
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} className={testClassName} />,
      );
      const containerElement = container.querySelector('.rich-datetime-picker');
      expect(containerElement).toHaveClass(testClassName);
    });

    it('test for prop: `inputClassNmae` ', () => {
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

    it('test for prop: `triggerClassName` ', () => {
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

    // it('test for prop: `datePickerClassName` ', () => {
    //   const testClassName = 'datepicker-test-class-name';
    //   const { container } = render(
    //     <RichDatetimePicker
    //       startDate={startDate}
    //       endDate={endDate}
    //       datePickerClassName={testClassName}
    //     />,
    //   );
    //   const triggerElement = container.querySelector('.rich-datetime-picker__trigger');
    //   expect(triggerElement).toHaveClass(testClassName);
    // });

    // it('test for prop: `dateFormat` ', () => {
    //   const { container } = render(
    //     <RichDatetimePicker
    //       dateFormat="yyyy/MM/dd hh:mm:ss"
    //       startDate={startDate}
    //       endDate={endDate}
    //     />,
    //   );
    //   const inputElement = container.querySelector(
    //     '.rich-datetime-picker__input',
    //   ) as HTMLInputElement;
    //   const inputText = inputElement.value;
    //   const reslutDateString = `${format(startDate, 'yyyy/MM/dd hh:mm:ss')} - ${format(
    //     endDate,
    //     'yyyy/MM/dd hh:mm:ss',
    //   )}`;
    //   expect(inputText).toBe(reslutDateString);
    // });
  });

  describe('test time units', () => {
    it('show panel when clicking trigger', () => {
      const { container } = render(<RichDatetimePicker startDate={startDate} endDate={endDate} />);

      const triggerElement = container.querySelector(
        '.rich-datetime-picker__trigger',
      ) as HTMLDivElement;

      fireEvent.click(triggerElement);

      const panelElement = container.querySelector('.rich-datetime-picker__panel');

      expect(panelElement).toBeInTheDocument();
    });

    it('onChange is called when click time units', () => {
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
          defaultOpen
        />,
      );

      const fiveMinuteText = 'Past 5 Minutes';
      const fiveMinute = screen.getByText(fiveMinuteText);

      fireEvent.click(fiveMinute);

      expect(onChangeMock).toHaveBeenCalledWith(dateRange, fiveMinuteText, '5m');
    });

    // it('test `timeUnits` prop with year', () => {
    //   const onChangeMock = jest.fn();
    //   let dateRange: DateRange = [startDate, endDate];

    //   onChangeMock.mockImplementation((range) => {
    //     dateRange = range;
    //   });

    //   render(
    //     <RichDatetimePicker
    //       startDate={startDate}
    //       endDate={endDate}
    //       onChange={onChangeMock}
    //       timeUnits={['1y']}
    //       defaultOpen
    //     />,
    //   );
    //   const oneYearText = 'Past 1 Year';
    //   const oneYear = screen.getByText(oneYearText);

    //   expect(oneYear).toBeInTheDocument();

    //   fireEvent.click(oneYear);

    //   expect(onChangeMock).toHaveBeenCalledWith(dateRange, oneYearText, '1y');
    // });
  });

  describe('test Calendar', () => {
    it('show Calender when clicking calendar button', async () => {
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} defaultOpen />,
      );
      const calendarButton = screen.getByText('Select from calendar...');

      fireEvent.click(calendarButton);

      await waitFor(() => {
        const moreView = container.querySelector('.rich-datetime-picker__date-picker');
        expect(moreView).toBeInTheDocument();
      });
    });

    it('test for prop: `hideCalendarYearButton`', async () => {
      const { container } = render(
        <RichDatetimePicker
          startDate={startDate}
          endDate={endDate}
          defaultOpen
          hideCalendarYearButton
        />,
      );
      const calendarButton = screen.getByText('Select from calendar...');

      fireEvent.click(calendarButton);

      await waitFor(() => {
        const moreView = container.querySelector('.rich-datetime-picker__date-picker');
        expect(moreView).toBeInTheDocument();
      });
      const yearButton = queryByTestId(container, 'test-calendar-year-button');
      expect(yearButton).toBeNull();
    });
  });

  describe('test CustomTimeView', () => {
    it('show CustomTimeView when mouse entering more button', async () => {
      const { container } = render(
        <RichDatetimePicker startDate={startDate} endDate={endDate} defaultOpen />,
      );
      const more = screen.getByText('More');

      fireEvent.mouseEnter(more);

      await waitFor(() => {
        const moreView = container.querySelector('.rich-datetime-picker__more');
        expect(moreView).toBeInTheDocument();
      });
    });

    it('onChange is called when click Relative times', async () => {
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
      const more = screen.getByText('More');

      fireEvent.mouseEnter(more);

      await waitFor(() => {
        const moreView = container.querySelector('.rich-datetime-picker__more');
        expect(moreView).toBeInTheDocument();
      });

      const customTimeText = '45m';
      const customTime = screen.getByText(customTimeText);

      fireEvent.click(customTime);

      expect(onChangeMock).toHaveBeenCalledWith(dateRange, customTimeText, undefined);
    });

    it('test for prop: `customTimes`', async () => {
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
      const more = screen.getByText('More');

      fireEvent.mouseEnter(more);

      await waitFor(() => {
        const moreView = container.querySelector('.rich-datetime-picker__more');
        expect(moreView).toBeInTheDocument();

        relativeTimes.forEach((time) => {
          expect(moreView).toHaveTextContent(time);
        });

        fixedTimes.forEach((time) => {
          expect(moreView).toHaveTextContent(time);
        });
      });
    });
  });
});
