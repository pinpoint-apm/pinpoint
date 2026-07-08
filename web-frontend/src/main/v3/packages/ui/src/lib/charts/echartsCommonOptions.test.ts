import { buildEmptyMessageGraphic } from './echartsCommonOptions';

describe('buildEmptyMessageGraphic', () => {
  it('shows the message when there is no data', () => {
    const [graphic] = buildEmptyMessageGraphic(false, 'No Data');
    expect(graphic.style.text).toBe('No Data');
    expect(graphic.style.fontSize).toBe(18);
    expect(graphic.top).toBe('middle');
  });

  it('keeps a single element but blanks the text when data exists', () => {
    const graphic = buildEmptyMessageGraphic(true, 'No Data');
    expect(graphic).toHaveLength(1);
    expect(graphic[0].style.text).toBe('');
  });

  it('honors fontSize and top overrides', () => {
    const [graphic] = buildEmptyMessageGraphic(false, 'x', { fontSize: 14, top: '30%' });
    expect(graphic.style.fontSize).toBe(14);
    expect(graphic.top).toBe('30%');
  });
});
