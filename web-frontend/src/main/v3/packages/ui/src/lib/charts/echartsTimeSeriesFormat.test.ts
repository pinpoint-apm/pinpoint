import { formatAxisTooltip } from './echartsTimeSeriesFormat';

describe('formatAxisTooltip', () => {
  it('returns an empty string for empty/invalid params', () => {
    expect(formatAxisTooltip([], (v) => String(v))).toBe('');
    expect(formatAxisTooltip(null, (v) => String(v))).toBe('');
  });

  it('renders a row per series and applies formatValue', () => {
    const html = formatAxisTooltip(
      [
        { axisValue: 0, seriesName: 'cpu', color: '#f00', value: 5 },
        { axisValue: 0, seriesName: 'mem', color: '#0f0', value: 10 },
      ],
      (v) => `${v}%`,
    );
    expect(html).toContain('cpu');
    expect(html).toContain('5%');
    expect(html).toContain('mem');
    expect(html).toContain('10%');
  });

  it('hides null-valued series by default (uncollected → gap)', () => {
    const html = formatAxisTooltip(
      [{ axisValue: 0, seriesName: 'cpu', color: '#f00', value: null }],
      (v) => String(v),
    );
    expect(html).not.toContain('cpu');
  });

  it('shows null-valued series as zero when nullBehavior is "zero"', () => {
    const html = formatAxisTooltip(
      [{ axisValue: 0, seriesName: 'req', color: '#f00', value: [123, null] }],
      (v) => String(v),
      { nullBehavior: 'zero' },
    );
    expect(html).toContain('req');
    expect(html).toContain('>0<'); // formatValue(0)
  });

  it('uses the injected formatDate for the header', () => {
    const html = formatAxisTooltip(
      [{ axisValue: 1700000000000, seriesName: 'a', color: '#000', value: 1 }],
      (v) => String(v),
      { formatDate: () => 'CUSTOM_DATE' },
    );
    expect(html).toContain('CUSTOM_DATE');
  });

  it('escapes HTML in series names', () => {
    const html = formatAxisTooltip(
      [{ axisValue: 0, seriesName: '<img src=x onerror=alert(1)>', color: '#000', value: 1 }],
      (v) => String(v),
    );
    expect(html).not.toContain('<img src=x');
  });
});
