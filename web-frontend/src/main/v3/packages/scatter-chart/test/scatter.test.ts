import 'jest-canvas-mock';
import { fireEvent, waitFor } from '@testing-library/dom';
import data1 from './mock/data1.json';
import data2 from './mock/data2.json';
import { initOption, ScatterChartTestHelper, simulateDrag } from './helper';
import { Legend } from '../src/ui/Legend';
import { Viewport } from '../src/ui/Viewport';

describe('Test for Scatter', () => {
  let SC: ScatterChartTestHelper;
  const wrapper = document.createElement('div');

  describe('Test for Scatter Method', () => {
    it('should check data rendered by `render` method', () => {
      // given
      SC = new ScatterChartTestHelper(wrapper, initOption);

      // when
      SC.render(data1.data);

      // then
      const events = SC.viewport.context.__getEvents();
      expect(events).toMatchSnapshot();
    });

    it('should check data rendered by `render` method with append flag is true', () => {
      // given
      SC = new ScatterChartTestHelper(wrapper, initOption);

      // when
      SC.render(data1.data, { append: true });
      SC.render(data2.data, { append: true });

      // then
      const events = SC.viewport.context.__getEvents();
      expect(events).toMatchSnapshot();
    });

    it('should render all data then greater than y.min when drawOutOfRange flag is true', () => {
      // given
      SC = new ScatterChartTestHelper(wrapper, initOption);
      const dataCount = data1.data.filter((d) => d.y > SC.getYAxis().min && d.type === 'success').length;

      // when
      SC.render(data1.data, { drawOutOfRange: true });
      const successCount = wrapper.querySelector('.success .__scatter_chart__legend_count')?.innerHTML;

      // then
      waitFor(() => {
        expect(successCount).toBe(`${dataCount}`);
      });
    });

    it('should occur `click` callback function registerd by `on` method when clicking chart area', () => {
      // given
      const onClick = jest.fn();
      SC = new ScatterChartTestHelper(wrapper, initOption);
      SC.on('click', onClick);

      // when
      fireEvent.click(SC.getGuide()!.canvas);

      // then
      expect(onClick).toHaveBeenCalled();
    });

    it('should occur `dragEnd` callback function registerd by `on` method when drag chart area', () => {
      // given
      const onDragEnd = jest.fn();
      SC = new ScatterChartTestHelper(wrapper, initOption);
      SC.on('dragEnd', onDragEnd);

      // when
      simulateDrag(SC.getGuide()!.canvas, 0, 0, 100, 100);

      // then
      expect(onDragEnd).toHaveBeenCalled();
    });

    it('should occur `resize` callback function registerd by `on` method when resize method called', () => {
      // given
      const onResize = jest.fn();
      SC = new ScatterChartTestHelper(wrapper, initOption);
      SC.on('resize', onResize);

      // when
      SC.resize(100, 100);

      // then
      expect(onResize).toHaveBeenCalled();
    });

    it('should occur `clickLegend` callback function registerd by `on` method when clicking legend area', () => {
      // given
      const onClick = jest.fn();
      SC = new ScatterChartTestHelper(wrapper, initOption);
      SC.on('clickLegend', onClick);

      // when
      const inputElement = SC.getLegend()!.container.getElementsByTagName('input')[0];
      fireEvent.click(inputElement);

      // then
      expect(onClick).toHaveBeenCalled();
      expect(onClick.mock.calls[0][1]).toMatchObject({ checked: ['fail'] });
    });

    it('should not occur callback function when remove callback event by `off` method', () => {
      // given
      const onClick = jest.fn();
      const onClickLegend = jest.fn();
      const onDragEnd = jest.fn();
      const onResize = jest.fn();
      SC = new ScatterChartTestHelper(wrapper, initOption);
      SC.on('click', onClick);
      SC.on('dragEnd', onDragEnd);
      SC.on('clickLegend', onClickLegend);
      SC.on('resize', onResize);
      SC.off('click');
      SC.off('dragEnd');
      SC.off('clickLegend');
      SC.off('resize');

      // when
      SC.resize(100, 100);
      fireEvent.click(SC.getGuide()!.canvas);

      // then
      expect(onClick).not.toHaveBeenCalled();
      expect(onClickLegend).not.toHaveBeenCalled();
      expect(onDragEnd).not.toHaveBeenCalled();
      expect(onResize).not.toHaveBeenCalled();
    });

    it('should be resized by `resize` method', () => {
      // given
      SC = new ScatterChartTestHelper(wrapper, initOption);

      // when
      SC.resize(100, 100);

      // then
      expect(SC.viewport.styleWidth).toBe(100);
      expect(SC.viewport.styleHeight).toBe(100);
    });

    it('data should be cleared by `clear` method', () => {
      // given
      SC = new ScatterChartTestHelper(wrapper, initOption);

      // when
      SC.clear();

      // then
      expect(SC.getData().length).toBe(0);
    });

    it('all legends should be 0 by `clear` method', () => {
      // given
      SC = new ScatterChartTestHelper(wrapper, initOption);
      SC.render(data1.data);

      // when
      SC.clear();

      // then
      const successCount = wrapper.querySelector('.success .__scatter_chart__legend_count')?.innerHTML;
      const failCount = wrapper.querySelector('.fail .__scatter_chart__legend_count')?.innerHTML;
      waitFor(() => {
        expect(successCount).toBe('0');
        expect(failCount).toBe('0');
      });
    });

    it('should remove rendered elements(view, legend) from the screen when destroy method is called.', () => {
      // given
      SC = new ScatterChartTestHelper(wrapper, initOption);
      SC.render(data1.data);

      // when
      SC.destroy();

      // then
      const viewContainer = wrapper.querySelector(Viewport.VIEW_CONTAINER_CLASS);
      const legendContainer = wrapper.querySelector(Legend.LEGEND_CONTAINER_CLASS);

      waitFor(() => {
        expect(viewContainer?.childNodes.length).toBe('0');
        expect(legendContainer?.childNodes.length).toBe('0');
      });
    });
  });
});
