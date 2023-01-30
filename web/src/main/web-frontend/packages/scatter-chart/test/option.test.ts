// import { render, screen } from '@testing-library/react'
// import { Button } from './Button';
import 'jest-canvas-mock';
import { ScatterChart } from "../src";

describe('Option', () => {
  let SC: ScatterChart;

  beforeEach(() => {
    const wrapper = document.createElement('div');
    SC = new ScatterChart(wrapper, {
      axis: {
        x: {
          min: 1669103462000,
          max: 1669103509335,
          tick: {
            count: 5,
            format: (value) => {
              const date = new Date(value);
              return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
            }, 
          }
        },
        y: {
          min: 0,
          max: 10000,
          tick: {
            count: 5,
            format: (value) => value.toLocaleString(),
          }
        }
      },
      data: [
        {
          type: 'success',
          color: 'green',
          priority: 11,
        },
        {
          type: 'fail',
          color: 'red',
          priority: 1,
        },
      ],
    });
  })

  it('set axis option', () => {
    SC.setAxisOption({
      x: {
        padding: 20,
        tick: {
          font: '20px serif',
          color: 'red',
          strokeColor: 'blue',
          width: 10,
        }
      },
      y: {
        padding: 50,
        tick: {
          font: '15px serif',
          color: 'green',
          strokeColor: 'purple',
          width: 10,
        }
      }
    })
    const events = SC.viewport.context.__getDrawCalls();
    expect(events).toMatchSnapshot();
    // Given
    // When
    // Then
  })
})