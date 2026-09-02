import { colors, screenPixels, screens, toPixels } from './theme';

describe('toPixels', () => {
  it('rem 을 픽셀로 바꾼다', () => {
    expect(toPixels('40rem')).toBe(640);
    expect(toPixels('96rem')).toBe(1536);
  });

  it('px 은 숫자만 떼어낸다', () => {
    expect(toPixels('640px')).toBe(640);
  });

  it('읽을 수 없으면 0 이다', () => {
    expect(toPixels('auto')).toBe(0);
  });
});

describe('screens', () => {
  it('Tailwind 원문 값을 그대로 둔다 — 미디어 쿼리에 그대로 싣는다', () => {
    expect(screens.sm).toBe('40rem');
  });

  it('픽셀 숫자도 함께 제공한다', () => {
    expect(screenPixels.sm).toBe(640);
    expect(screenPixels.md).toBe(768);
    expect(screenPixels.lg).toBe(1024);
    expect(screenPixels.xl).toBe(1280);
  });
});

describe('colors', () => {
  // echarts(zrender)는 `oklch()` 를 파싱하지 못해 색이 조용히 사라진다. JS 로 나가는 값은
  // 전부 hex 여야 한다.
  it('기본 팔레트를 hex 로 내놓는다', () => {
    expect(colors.blue[600]).toBe('#155dfc');
    expect(colors.red[500]).toBe('#fb2c36');
    expect(colors.black).toBe('#000');
  });

  it('pinpoint 색도 hex 로 내놓는다', () => {
    expect(colors['status-success']).toBe('#00d492');
    expect(colors.fast).toBe('#5ee9b5');
    expect(colors.slow).toBe('#ff6900');
  });

  it('oklch 표기가 남아 있지 않다', () => {
    const remaining = JSON.stringify(colors).match(/oklch\(/g);
    expect(remaining).toBeNull();
  });

  it('CSS 변수로 된 색은 그대로 둔다 — 유틸리티 클래스가 쓴다', () => {
    expect(colors['status-good']).toBe('hsl(var(--ui-primary))');
  });
});
