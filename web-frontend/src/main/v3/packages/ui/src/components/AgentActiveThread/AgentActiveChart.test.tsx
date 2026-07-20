import { render } from '@testing-library/react';
import { AgentActiveData } from './AgentActiveTable';

// echarts 자체와 인스턴스 라이프사이클 훅은 모킹해, AgentActiveChart 가 chart.setOption 에 넘기는
// 옵션(순수 변환 로직)과 클릭 핸들러만 검증한다. (실제 렌더/canvas 는 이 테스트의 관심사가 아니다)
const mockSetOption = jest.fn();
const mockZrOn = jest.fn();
const mockZrOff = jest.fn();
const mockZr = { on: mockZrOn, off: mockZrOff };
const mockContainPixel = jest.fn(() => true);
const mockConvertFromPixel = jest.fn(() => 0);
const mockChartRef = { current: null as HTMLDivElement | null };
const mockChartInstanceRef = {
  current: {
    setOption: mockSetOption,
    getZr: () => mockZr,
    containPixel: mockContainPixel,
    convertFromPixel: mockConvertFromPixel,
  },
};

jest.mock('echarts/core', () => ({ use: jest.fn() }));
jest.mock('echarts/charts', () => ({ BarChart: {} }));
jest.mock('echarts/components', () => ({
  GridComponent: {},
  TooltipComponent: {},
  LegendComponent: {},
}));
jest.mock('echarts/renderers', () => ({ CanvasRenderer: {} }));
jest.mock('../../lib/charts/useEChartsInstance', () => ({
  useEChartsInstance: () => ({
    chartRef: mockChartRef,
    chartInstanceRef: mockChartInstanceRef,
    renderRef: { current: null },
  }),
}));

// eslint-disable-next-line import/first
import { AgentActiveChart, AgentActiveChartProps } from './AgentActiveChart';

const makeRow = (server: string, values: [number, number, number, number]): AgentActiveData => ({
  server,
  '1s': values[0],
  '3s': values[1],
  '5s': values[2],
  slow: values[3],
  agentName: `${server}-name`,
  message: '',
});

const renderChart = (props: Partial<AgentActiveChartProps> = {}) => {
  render(<AgentActiveChart {...props} />);
  const [option, mergeArg] = mockSetOption.mock.calls[0];
  return { option, mergeArg };
};

describe('AgentActiveChart', () => {
  beforeEach(() => {
    mockSetOption.mockClear();
    mockZrOn.mockClear();
    mockZrOff.mockClear();
    mockContainPixel.mockClear();
    mockContainPixel.mockReturnValue(true);
    mockConvertFromPixel.mockClear();
    mockConvertFromPixel.mockReturnValue(0);
  });

  it('enables growth animation and merges updates so bars transition smoothly', () => {
    const { option, mergeArg } = renderChart({ data: [makeRow('a', [1, 2, 3, 4])] });
    expect(option.animation).toBe(true);
    // 기본 병합(replaceMerge 미사용)이라 실시간 값 변화가 애니메이션으로 이어진다.
    expect(mergeArg).toBeUndefined();
  });

  it('stacks four bar series from slow (bottom) to 1s (top) with fixed colors', () => {
    const { option } = renderChart({ data: [makeRow('a', [1, 2, 3, 4])] });
    const dataSeries = option.series.filter((s: { stack?: string }) => s.stack === 'agentActiveThread');
    expect(dataSeries.map((s: { name: string }) => s.name)).toEqual(['slow', '5s', '3s', '1s']);
    expect(dataSeries.every((s: { type: string }) => s.type === 'bar')).toBe(true);
    expect(dataSeries[0].itemStyle.color).toBe('#e67f22'); // slow
    expect(dataSeries[3].itemStyle.color).toBe('#34b994'); // 1s
  });

  it('maps each series value from the matching data key', () => {
    const { option } = renderChart({ data: [makeRow('a', [10, 20, 30, 40])] });
    // series order: slow, 5s, 3s, 1s
    expect(option.series[0].data[0].value).toBe(40); // slow
    expect(option.series[1].data[0].value).toBe(30); // 5s
    expect(option.series[2].data[0].value).toBe(20); // 3s
    expect(option.series[3].data[0].value).toBe(10); // 1s
  });

  it('dims non-selected servers to 0.3 and keeps the selected one at 1', () => {
    const { option } = renderChart({
      data: [makeRow('a', [1, 1, 1, 1]), makeRow('b', [1, 1, 1, 1])],
      clickedActiveThread: 'b',
    });
    expect(option.series[0].data[0].itemStyle.opacity).toBe(0.3); // server a (not selected)
    expect(option.series[0].data[1].itemStyle.opacity).toBe(1); // server b (selected)
  });

  it('keeps every server at full opacity when nothing is selected', () => {
    const { option } = renderChart({ data: [makeRow('a', [1, 1, 1, 1]), makeRow('b', [1, 1, 1, 1])] });
    expect(option.series[0].data[0].itemStyle.opacity).toBe(1);
    expect(option.series[0].data[1].itemStyle.opacity).toBe(1);
  });

  it('hides the bars while loading (empty series data)', () => {
    const { option } = renderChart({ data: [makeRow('a', [1, 2, 3, 4])], loading: true });
    expect(option.series.every((s: { data: unknown[] }) => s.data.length === 0)).toBe(true);
  });

  it('applies the segment decal to the bars but not the legend swatch', () => {
    const { option } = renderChart({ data: [makeRow('a', [1, 2, 3, 4])] });
    // 막대(데이터 아이템)에는 흰 가로줄 decal 이 있어 칸칸(볼륨미터)으로 보인다.
    expect(option.series[0].data[0].itemStyle.decal).toBeDefined();
    expect(option.series[0].data[0].itemStyle.decal.color).toContain('255');
    // 시리즈 레벨(범례 아이콘이 참조)에는 decal 이 없어야 스와치가 갈라지지 않는다.
    expect(option.series[0].itemStyle.decal).toBeUndefined();
  });

  it('applies the configured yMax to the value axis', () => {
    const { option } = renderChart({
      data: [makeRow('a', [1, 2, 3, 4])],
      setting: { yMax: 10, isSplit: false, inactivityThreshold: 5 },
    });
    expect(option.yAxis.max).toBe(10);
  });

  it('renders the legend in reverse (1s, 3s, 5s, slow) with rounded swatches', () => {
    const { option } = renderChart({ data: [makeRow('a', [1, 2, 3, 4])] });
    expect(option.legend.data).toEqual(['1s', '3s', '5s', 'slow']);
    // 1:1 비율 rounded-square path (정사각형 둥근 스와치)
    expect(option.legend.icon).toContain('path://');
    expect(option.legend.itemWidth).toBe(option.legend.itemHeight);
  });

  it('hides the x-axis server labels', () => {
    const { option } = renderChart({ data: [makeRow('srv', [1, 2, 3, 4])] });
    expect(option.xAxis.axisLabel.show).toBe(false);
    expect(option.xAxis.data).toEqual(['srv']);
  });

  it('marks the selected column with an overlapping full-height highlight bar (bar width, not slot)', () => {
    const { option } = renderChart({
      data: [makeRow('srv-a', [1, 2, 3, 4]), makeRow('srv-b', [1, 2, 3, 4])],
      clickedActiveThread: 'srv-b',
      setting: { yMax: 100, isSplit: false, inactivityThreshold: 5 },
    });
    const highlight = option.series.find((s: { name: string }) => s.name === '__selected_column__');
    expect(highlight).toBeDefined();
    // 막대와 같은 폭으로 겹쳐(barGap -100%) 옆 칸을 침범하지 않는다.
    expect(highlight.barGap).toBe('-100%');
    expect(highlight.barWidth).toBe(option.series[0].barWidth);
    // 선택된 서버만 전체 높이(yMax), 나머지는 null.
    expect(highlight.data).toEqual([null, 100]);
  });

  it('clears the highlight bar when nothing is selected', () => {
    const { option } = renderChart({ data: [makeRow('srv-a', [1, 2, 3, 4])] });
    const highlight = option.series.find((s: { name: string }) => s.name === '__selected_column__');
    expect(highlight.data).toEqual([]);
  });

  describe('tooltip formatter', () => {
    it('shows the server as header and rows in reverse order, mapping negatives to "-"', () => {
      const { option } = renderChart({ data: [makeRow('srv1', [1, 2, 3, 4])] });
      // params come in series order (slow, 5s, 3s, 1s); -1 means uncollected
      const html = option.tooltip.formatter([
        { axisValue: 'srv1', seriesName: 'slow', value: -1, color: '#e67f22' },
        { axisValue: 'srv1', seriesName: '5s', value: 3, color: '#ffba00' },
        { axisValue: 'srv1', seriesName: '3s', value: 2, color: '#51afdf' },
        { axisValue: 'srv1', seriesName: '1s', value: 1, color: '#34b994' },
      ]);
      expect(html).toContain('srv1');
      // reversed display order: 1s appears before slow
      expect(html.indexOf('1s')).toBeLessThan(html.indexOf('slow'));
      // negative value rendered as '-'
      expect(html).toContain('>-<');
    });
  });

  describe('click toggle (column background, not just the bar)', () => {
    it('toggles the server at the clicked column via a zrender click on the grid', () => {
      const setClicked = jest.fn();
      mockConvertFromPixel.mockReturnValue(1); // clicked column index → server 'b'
      render(
        <AgentActiveChart
          data={[makeRow('a', [1, 2, 3, 4]), makeRow('b', [1, 2, 3, 4])]}
          setClickedActiveThread={setClicked}
        />,
      );
      const [eventName, handler] = mockZrOn.mock.calls[0];
      expect(eventName).toBe('click');

      handler({ offsetX: 20, offsetY: 20 });
      const updater = setClicked.mock.calls[0][0];
      expect(updater('')).toBe('b'); // select when none selected
      expect(updater('b')).toBe(''); // deselect when already selected
    });

    it('ignores clicks outside the grid (e.g. legend/blank area)', () => {
      const setClicked = jest.fn();
      mockContainPixel.mockReturnValue(false);
      render(<AgentActiveChart data={[makeRow('a', [1, 2, 3, 4])]} setClickedActiveThread={setClicked} />);
      const handler = mockZrOn.mock.calls[0][1];
      handler({ offsetX: 0, offsetY: 0 });
      expect(setClicked).not.toHaveBeenCalled();
    });
  });

  it('disables legend toggling so it stays on one line', () => {
    const { option } = renderChart({ data: [makeRow('a', [1, 2, 3, 4])] });
    expect(option.legend.selectedMode).toBe(false);
  });
});
