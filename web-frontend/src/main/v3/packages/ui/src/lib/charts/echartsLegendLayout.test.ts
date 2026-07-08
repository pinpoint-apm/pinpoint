import { buildBottomLegend, LEGEND_ICON_WIDTH, LEGEND_ITEM_GAP } from './echartsLegendLayout';

describe('buildBottomLegend', () => {
  it('builds a bottom legend with shared icon/spacing and the given names', () => {
    const legend = buildBottomLegend(['a', 'b']);
    expect(legend.data).toEqual(['a', 'b']);
    expect(legend.bottom).toBe(0);
    expect(legend.icon).toBe('square');
    expect(legend.itemWidth).toBe(LEGEND_ICON_WIDTH);
    expect(legend.itemHeight).toBe(10);
    expect(legend.itemGap).toBe(LEGEND_ITEM_GAP);
  });

  it('merges extra options (e.g. show)', () => {
    const legend = buildBottomLegend([], { show: false });
    expect(legend).toMatchObject({ show: false, data: [] });
  });
});
