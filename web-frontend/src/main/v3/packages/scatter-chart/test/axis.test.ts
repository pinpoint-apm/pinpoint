import 'jest-canvas-mock';
import { initOption, ScatterChartTestHelper } from './helper';

describe('Test for Axis', () => {
  let SC: ScatterChartTestHelper;
  const wrapper: HTMLDivElement = document.createElement('div');

  afterEach(() => {
    // check all canvas context rendering events
    const viewportEvents = SC.viewport.context.__getEvents();
    const contexts = [SC.viewport.context, SC.getXAxis().context, SC.getYAxis().context, SC.getGridAxis().context];
    contexts.forEach((ctx) => {
      const events = ctx.__getEvents();
      expect(events).toMatchSnapshot();
    });
    expect(viewportEvents).toMatchSnapshot();
  });

  it('should check initOption is setted', () => {
    // given
    // when
    SC = new ScatterChartTestHelper(wrapper, initOption);

    // then
    // axis x
    expect(SC.getOption().axis.x).toEqual(
      expect.objectContaining({
        ...initOption.axis.x,
        tick: expect.objectContaining(initOption.axis.x.tick),
      }),
    );
    // axis y
    expect(SC.getOption().axis.y).toEqual(
      expect.objectContaining({
        ...initOption.axis.y,
        tick: expect.objectContaining(initOption.axis.y.tick),
      }),
    );
  });

  it('should check axis options are setted by setOption ', () => {
    // given
    const x = {
      padding: 20,
      tick: {
        font: '20px serif',
        color: 'red',
        strokeColor: 'blue',
        width: 10,
      },
    };
    const y = {
      padding: 50,
      tick: {
        font: '15px serif',
        color: 'green',
        strokeColor: 'purple',
        width: 10,
      },
    };
    SC = new ScatterChartTestHelper(wrapper, initOption);

    // when
    SC.setOption({
      axis: {
        x,
        y,
      },
    });

    // then
    // axis x
    expect(SC.getOption().axis.x).toEqual(
      expect.objectContaining({
        ...x,
        tick: expect.objectContaining(x.tick),
      }),
    );
    // axis y
    expect(SC.getOption().axis.y).toEqual(
      expect.objectContaining({
        ...y,
        tick: expect.objectContaining(y.tick),
      }),
    );
  });
});
