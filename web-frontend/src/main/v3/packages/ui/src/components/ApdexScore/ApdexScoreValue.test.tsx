import { render, screen } from '@testing-library/react';
import { ApdexScoreValue } from './ApdexScoreValue';

describe('ApdexScoreValue', () => {
  // 색은 등급을 말해 주는 유일한 표시라, 경계값에서 한 칸 밀리면 사용자는 멀쩡한 application을
  // 문제 있는 것으로 읽는다. ChartsBoard의 Apdex와 같은 등급 기준(getRank)인지 경계마다 확인한다.
  test.each([
    [1, 'text-status-success'],
    [0.94, 'text-status-success'],
    [0.93, 'text-status-good'],
    [0.85, 'text-status-good'],
    [0.84, 'text-[#f7d84a]'],
    [0.7, 'text-[#f7d84a]'],
    [0.69, 'text-status-warn'],
    [0.5, 'text-status-warn'],
    [0.49, 'text-status-fail'],
    [0, 'text-status-fail'],
  ])('colors %s with the rank class of the ChartsBoard Apdex', (score, className) => {
    render(<ApdexScoreValue score={score} />);

    expect(screen.getByText(/^\d\.\d{2}$/).classList.contains(className)).toBe(true);
  });

  // map 노드 라벨·ChartsBoard와 같이 버림으로 맞춘다. 반올림하면 0.937이 0.94로 보여
  // 색(Good)과 숫자(Excellent 경계)가 어긋난다.
  test('truncates the score to two decimals instead of rounding it', () => {
    render(<ApdexScoreValue score={0.937} />);

    const value = screen.getByText(/^\d\.\d{2}$/);
    expect(value.textContent).toBe('0.93');
    expect(value.classList.contains('text-status-good')).toBe(true);
  });

  test('keeps the className given by the caller', () => {
    render(<ApdexScoreValue score={0.5} className="shrink-0" />);

    expect(screen.getByText('0.50').classList.contains('shrink-0')).toBe(true);
  });
});
