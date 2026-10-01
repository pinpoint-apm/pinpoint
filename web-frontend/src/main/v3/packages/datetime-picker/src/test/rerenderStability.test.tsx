import { fireEvent, render, screen } from '@testing-library/react';
import { subMinutes } from 'date-fns';
import { RichDatetimePicker } from '../components/RichDatetimePicker';

/**
 * 소비자가 렌더할 때마다 같은 시각의 새 Date 를 만들어 넘겨도(문자열을 매번 parse 하는 경우)
 * 피커 상태가 초기화되면 안 된다. 값이 바뀐 것이 아니기 때문이다.
 */
describe('같은 시각의 새 Date 로 다시 렌더될 때', () => {
  const end = new Date('2026-10-01T05:00:00Z').getTime();
  const start = subMinutes(end, 20).getTime();
  const MESSAGE = 'Out of date range.';

  const renderPicker = () => {
    const ui = () => (
      <RichDatetimePicker
        startDate={new Date(start)}
        endDate={new Date(end)}
        validateDatePickerRange={() => MESSAGE}
        defaultOpen
      />
    );
    const result = render(ui());
    return { ...result, rerenderFresh: () => result.rerender(ui()) };
  };

  it('Apply 검증 오류 문구가 유지된다', () => {
    const { rerenderFresh } = renderPicker();

    fireEvent.click(screen.getByRole('button', { name: 'Apply' }));
    expect(screen.getByText(MESSAGE)).toBeInTheDocument();

    rerenderFresh();
    expect(screen.getByText(MESSAGE)).toBeInTheDocument();
  });

  it('텍스트 입력 검증 오류 문구와 입력 중인 텍스트가 유지된다', () => {
    const { container, rerenderFresh } = renderPicker();
    const input = container.querySelector('.rich-datetime-picker__input') as HTMLInputElement;

    fireEvent.change(input, { target: { value: '45m' } });
    fireEvent.keyDown(input, { key: 'Enter', code: 'Enter' });
    expect(screen.getByText(MESSAGE)).toBeInTheDocument();

    rerenderFresh();
    expect(screen.getByText(MESSAGE)).toBeInTheDocument();
    expect(input.value).toBe('45m');
  });
});
