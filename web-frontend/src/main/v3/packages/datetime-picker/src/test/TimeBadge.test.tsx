import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { formatInTimeZone } from 'date-fns-tz';
import { TimeBadge } from '../components/TimeBadge';

const tz = 'Asia/Seoul';

describe('TimeBadge', () => {
  it('displays the time segments of the value in the given timeZone', () => {
    const value = new Date('2026-07-13T08:18:58Z'); // 17:18:58 KST
    render(<TimeBadge value={value} timeZone={tz} onChange={jest.fn()} label="from" />);

    expect(screen.getByLabelText('from hours')).toHaveValue('17');
    expect(screen.getByLabelText('from minutes')).toHaveValue('18');
    expect(screen.getByLabelText('from seconds')).toHaveValue('58');
  });

  it('commits a new time preserving the calendar day', async () => {
    const user = userEvent.setup();
    const onChange = jest.fn();
    const value = new Date('2026-07-13T08:18:58Z'); // 17:18:58 KST
    render(<TimeBadge value={value} timeZone={tz} onChange={onChange} />);

    const hours = screen.getByLabelText('hours');
    await user.clear(hours);
    await user.type(hours, '09');

    const next = onChange.mock.calls.at(-1)![0] as Date;
    expect(formatInTimeZone(next, tz, 'yyyy-MM-dd HH:mm:ss')).toBe('2026-07-13 09:18:58');
  });

  it('steps the value with ArrowUp / ArrowDown and wraps', () => {
    const onChange = jest.fn();
    const value = new Date('2026-07-13T14:59:58Z'); // 23:59:58 KST
    render(<TimeBadge value={value} timeZone={tz} onChange={onChange} />);

    const hours = screen.getByLabelText('hours');
    fireEvent.keyDown(hours, { key: 'ArrowUp' }); // 23 -> wrap 0
    const next = onChange.mock.calls.at(-1)![0] as Date;
    expect(formatInTimeZone(next, tz, 'HH:mm:ss')).toBe('00:59:58');
  });

  it('renders placeholders when value is empty', () => {
    render(<TimeBadge value={null} timeZone={tz} onChange={jest.fn()} />);
    expect(screen.getByLabelText('hours')).toHaveValue('');
  });
});
