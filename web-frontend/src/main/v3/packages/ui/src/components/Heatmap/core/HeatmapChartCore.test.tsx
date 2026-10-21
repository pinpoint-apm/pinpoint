import { fireEvent, render, screen } from '@testing-library/react';
import { GetHeatmapAppData } from '@pinpoint-fe/ui/src/constants';
import { DefaultValue } from './heatmapSettingDefault';
import type { HeatmapChartProps } from './HeatmapChart';

// echarts ships ESM only, which jest cannot load, and jsdom has no canvas. The chart stands in as a
// button that reports a drag, so the test sees what HeatmapChartCore hands to it.
jest.mock('./HeatmapChart', () => ({
  __esModule: true,
  default: ({
    setting,
    onDragEnd,
    ref,
  }: HeatmapChartProps & { ref?: React.Ref<HTMLDivElement> }) => (
    <div ref={ref}>
      <button
        data-testid="heatmap-chart"
        data-y-max={setting.yMax}
        onClick={() => onDragEnd?.({ x1: 1, y1: 2, x2: 3, y2: 4 }, ['success'])}
      />
    </div>
  ),
}));

import { HeatmapChartCore } from './HeatmapChartCore';

const data = {} as GetHeatmapAppData.Response;

// Rendered with no router, no React Query provider and no stored setting, the way an MCP Apps view
// draws it.
describe('HeatmapChartCore', () => {
  test('draws the chart with the given setting and passes drags to the given callback', () => {
    const onDragEnd = jest.fn();
    render(
      <HeatmapChartCore
        data={data}
        setting={{ ...DefaultValue, yMax: 3000 }}
        onSettingChange={jest.fn()}
        onDragEnd={onDragEnd}
        captureFileName="heatmap.png"
      />,
    );

    const chart = screen.getByTestId('heatmap-chart');
    expect(chart.getAttribute('data-y-max')).toBe('3000');

    fireEvent.click(chart);
    expect(onDragEnd).toHaveBeenCalledWith({ x1: 1, y1: 2, x2: 3, y2: 4 }, ['success']);
  });

  test('shows the expand button only when onExpand is given', () => {
    const { rerender } = render(
      <HeatmapChartCore
        data={data}
        setting={DefaultValue}
        onSettingChange={jest.fn()}
        captureFileName="heatmap.png"
      />,
    );
    expect(screen.getByLabelText('Heatmap settings')).toBeTruthy();
    expect(screen.getByLabelText('Download heatmap image')).toBeTruthy();
    expect(screen.queryByLabelText('Expand heatmap')).toBeNull();

    const onExpand = jest.fn();
    rerender(
      <HeatmapChartCore
        data={data}
        setting={DefaultValue}
        onSettingChange={jest.fn()}
        onExpand={onExpand}
        captureFileName="heatmap.png"
      />,
    );
    fireEvent.click(screen.getByLabelText('Expand heatmap'));
    expect(onExpand).toHaveBeenCalled();
  });

  test('hides each toolbar button that toolbarOption hides', () => {
    const { rerender } = render(
      <HeatmapChartCore
        data={data}
        setting={DefaultValue}
        onSettingChange={jest.fn()}
        onExpand={jest.fn()}
        captureFileName="heatmap.png"
        toolbarOption={{ captureImage: { hide: true }, expand: { hide: true } }}
      />,
    );
    expect(screen.getByLabelText('Heatmap settings')).toBeTruthy();
    expect(screen.queryByLabelText('Download heatmap image')).toBeNull();
    expect(screen.queryByLabelText('Expand heatmap')).toBeNull();

    rerender(
      <HeatmapChartCore
        data={data}
        setting={DefaultValue}
        onSettingChange={jest.fn()}
        onExpand={jest.fn()}
        captureFileName="heatmap.png"
        toolbarOption={{ hide: true }}
      />,
    );
    expect(screen.queryByLabelText('Heatmap settings')).toBeNull();
    expect(screen.queryByLabelText('Download heatmap image')).toBeNull();
    expect(screen.queryByLabelText('Expand heatmap')).toBeNull();
  });

  test('shows the skeleton instead of the chart while loading', () => {
    render(
      <HeatmapChartCore
        isLoading
        data={data}
        setting={DefaultValue}
        onSettingChange={jest.fn()}
        captureFileName="heatmap.png"
      />,
    );
    expect(screen.queryByTestId('heatmap-chart')).toBeNull();
  });
});
