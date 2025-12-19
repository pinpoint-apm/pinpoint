import { GraphStyle, defaultTheme, ServerMapTheme } from '../../../constants/style/theme';

describe('GraphStyle', () => {
  it('NODE_WIDTH가 100이어야 함', () => {
    expect(GraphStyle.NODE_WIDTH).toBe(100);
  });

  it('NODE_HEIGHT가 100이어야 함', () => {
    expect(GraphStyle.NODE_HEIGHT).toBe(100);
  });

  it('NODE_RADIUS가 NODE_HEIGHT의 절반이어야 함', () => {
    expect(GraphStyle.NODE_RADIUS).toBe(50);
  });

  it('NODE_GAP가 30이어야 함', () => {
    expect(GraphStyle.NODE_GAP).toBe(30);
  });

  it('RANK_SEP가 200이어야 함', () => {
    expect(GraphStyle.RANK_SEP).toBe(200);
  });
});

describe('defaultTheme', () => {
  it('transactionStatus가 정의되어 있어야 함', () => {
    expect(defaultTheme.transactionStatus).toBeDefined();
    expect(defaultTheme.transactionStatus.default).toBeDefined();
    expect(defaultTheme.transactionStatus.good).toBeDefined();
    expect(defaultTheme.transactionStatus.slow).toBeDefined();
    expect(defaultTheme.transactionStatus.bad).toBeDefined();
  });

  it('transactionStatus 색상이 올바르게 설정되어 있어야 함', () => {
    expect(defaultTheme.transactionStatus.good.stroke).toBe('#32BA94');
    expect(defaultTheme.transactionStatus.slow.stroke).toBe('#E48022');
    expect(defaultTheme.transactionStatus.bad.stroke).toBe('#F0515B');
  });

  it('node 스타일이 정의되어 있어야 함', () => {
    expect(defaultTheme.node).toBeDefined();
    expect(defaultTheme.node.default).toBeDefined();
    expect(defaultTheme.node.highlight).toBeDefined();
    expect(defaultTheme.node.main).toBeDefined();
  });

  it('node 기본 스타일이 올바르게 설정되어 있어야 함', () => {
    expect(defaultTheme.node.default['background-color']).toBe('#FFF');
    expect(defaultTheme.node.default['border-width']).toBe('3');
    expect(defaultTheme.node.default['border-color']).toBe('#ddd');
  });

  it('edge 스타일이 정의되어 있어야 함', () => {
    expect(defaultTheme.edge).toBeDefined();
    expect(defaultTheme.edge.default).toBeDefined();
    expect(defaultTheme.edge.highlight).toBeDefined();
    expect(defaultTheme.edge.loop).toBeDefined();
  });

  it('edge 기본 스타일이 올바르게 설정되어 있어야 함', () => {
    expect(defaultTheme.edge.default.width).toBe(1.5);
    expect(defaultTheme.edge.default['line-color']).toBe('#C0C3C8');
    expect(defaultTheme.edge.default['target-arrow-color']).toBe('#C0C3C8');
  });
});

