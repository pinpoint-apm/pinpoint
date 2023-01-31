import 'jest-canvas-mock';
import { fireEvent } from '@testing-library/dom'
import data1 from './mock/data1.json';
import data2 from './mock/data2.json';
import { initOption, ScatterChartTestHelper } from './helper';

describe('Test for Scatter', () => {
  let SC: ScatterChartTestHelper;
  let wrapper: HTMLDivElement = document.createElement('div');

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

    it('should occur callback function registerd by `on` method when clicking chart area', () => {
      // given
      const onClick = jest.fn();
      SC = new ScatterChartTestHelper(wrapper, initOption);
      SC.on('click', onClick)

      // when
      fireEvent.click(SC.getGuide().canvas)

      // then
      expect(onClick).toHaveBeenCalled();
    });

    it('should not occur callback function when remove callback event by `off` method', () => {
      // given
      const onClick = jest.fn();
      SC = new ScatterChartTestHelper(wrapper, initOption);
      SC.on('click', onClick);
      SC.off('click');

      // when
      fireEvent.click(SC.getGuide().canvas)

      // then
      expect(onClick).not.toHaveBeenCalled();
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
  });
})
