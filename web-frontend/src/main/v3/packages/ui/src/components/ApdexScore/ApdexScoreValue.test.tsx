import { render, screen } from '@testing-library/react';
import { ApdexScoreValue } from './ApdexScoreValue';

// 표본이 있는 점수. 등급 자체를 확인하는 경우들은 "데이터 없음" 분기로 새지 않아야 한다.
const sampled = (apdexScore: number) => ({
  apdexScore,
  apdexFormula: { satisfiedCount: 0, toleratingCount: 0, totalSamples: 100 },
});

describe('ApdexScoreValue', () => {
  // 색은 등급을 말해 주는 유일한 표시라, 경계값에서 한 칸 밀리면 사용자는 멀쩡한 application을
  // 문제 있는 것으로 읽는다. ChartsBoard의 Apdex와 같은 등급 기준인지 경계마다 확인한다.
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
    render(<ApdexScoreValue apdex={sampled(score)} />);

    expect(screen.getByText(/^\d\.\d{2}$/).classList.contains(className)).toBe(true);
  });

  // 백엔드는 표본이 없으면 점수를 0으로 내려보낸다(ApdexScore#calculateApdexScore). 그것을 최하위
  // 등급으로 칠하면 트래픽이 없었을 뿐인 application이 장애로 보이고, 같은 노드의 ChartsBoard
  // Apdex·map 노드 링과도 색이 어긋난다.
  test.each([
    ['no samples', { apdexScore: 0, apdexFormula: { totalSamples: 0 } }],
    ['no response yet', undefined],
  ])('does not rank a score of %s as a failure', (_label, apdex) => {
    render(<ApdexScoreValue apdex={apdex} />);

    const value = screen.getByText('0.00');
    expect(value.classList.contains('text-status-success')).toBe(true);
    expect(value.classList.contains('text-status-fail')).toBe(false);
  });

  // 반대쪽. 표본이 있는데 나온 0은 진짜 0(전부 느리거나 실패)이라 최하위 등급이어야 한다.
  test('still ranks a real zero as unacceptable', () => {
    render(<ApdexScoreValue apdex={sampled(0)} />);

    expect(screen.getByText('0.00').classList.contains('text-status-fail')).toBe(true);
  });

  // map 노드 라벨·ChartsBoard와 같이 버림으로 맞춘다. 반올림하면 0.937이 0.94로 보여
  // 색(Good)과 숫자(Excellent 경계)가 어긋난다.
  test('truncates the score to two decimals instead of rounding it', () => {
    render(<ApdexScoreValue apdex={sampled(0.937)} />);

    const value = screen.getByText(/^\d\.\d{2}$/);
    expect(value.textContent).toBe('0.93');
    expect(value.classList.contains('text-status-good')).toBe(true);
  });

  test('keeps the className given by the caller', () => {
    render(<ApdexScoreValue apdex={sampled(0.5)} className="shrink-0" />);

    expect(screen.getByText('0.50').classList.contains('shrink-0')).toBe(true);
  });
});
