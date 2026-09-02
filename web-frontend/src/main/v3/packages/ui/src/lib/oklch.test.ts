import { oklchToHex } from './oklch';

describe('oklchToHex', () => {
  // Tailwind 4 기본 팔레트의 실제 값과 공식 문서에 적힌 hex 를 짝지어 고정한다.
  // 변환이 틀리면 차트 색이 조용히 달라지므로(echarts 는 oklch 를 못 읽는다) 값으로 못 박는다.
  it.each([
    ['blue-600', 'oklch(54.6% 0.245 262.881)', '#155dfc'],
    ['red-500', 'oklch(63.7% 0.237 25.331)', '#fb2c36'],
    ['emerald-400', 'oklch(76.5% 0.177 163.223)', '#00d492'],
    ['gray-300', 'oklch(87.2% 0.01 258.338)', '#d1d5dc'],
    ['orange-500', 'oklch(70.5% 0.213 47.604)', '#ff6900'],
  ])('%s 를 hex 로 바꾼다', (_name, oklch, hex) => {
    expect(oklchToHex(oklch)).toBe(hex);
  });

  it('밝기를 0~1 로 준 표기도 받는다', () => {
    expect(oklchToHex('oklch(0.546 0.245 262.881)')).toBe('#155dfc');
  });

  it('알파는 무시한다 — hex 로 표현하지 않는다', () => {
    expect(oklchToHex('oklch(54.6% 0.245 262.881 / 0.5)')).toBe('#155dfc');
  });

  it('무채색(채도 0)도 바꾼다', () => {
    expect(oklchToHex('oklch(0% 0 0)')).toBe('#000000');
    expect(oklchToHex('oklch(100% 0 0)')).toBe('#ffffff');
  });

  // Tailwind 의 회색 계열은 색상을 `none` 으로 적는다(CSS Color 4 의 결측 성분).
  it('색상이 none 인 표기도 바꾼다', () => {
    expect(oklchToHex('oklch(98.5% 0 none)')).toBe('#fafafa');
    expect(oklchToHex('oklch(97% 0 none)')).toBe('#f5f5f5');
  });

  it('읽을 수 없는 값은 그대로 돌려준다', () => {
    expect(oklchToHex('oklch(bad)')).toBe('oklch(bad)');
    expect(oklchToHex('#ff0000')).toBe('#ff0000');
  });
});
