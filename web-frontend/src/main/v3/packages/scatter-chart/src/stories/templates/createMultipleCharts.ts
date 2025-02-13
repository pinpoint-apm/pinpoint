import { newScatterChart } from './createDefault';
import { ScatterChart } from '../../ui';
import staticData from '../mock/realtimeData.json';

export const createMultipleCharts = () => {
  const CHART_COUNT = 30;
  const applicationTotal: any = [];
  const refinedData: any = {};
  const wrapper = document.createElement('div');
  const chartContainer = document.createElement('div');
  chartContainer.style.display = 'grid';
  chartContainer.style.gridTemplateColumns = '1fr 1fr 1fr 1fr 1fr 1fr ';
  const chartWrappers = [...Array(CHART_COUNT)].map(() => {
    const chartWrapper = document.createElement('div');
    chartContainer.append(chartWrapper);
    return chartWrapper;
  });
  wrapper.append(chartContainer);
  const btnStart = document.createElement('button');
  btnStart.innerText = 'start realtime';
  const btnStop = document.createElement('button');
  btnStop.innerText = 'stop realtime';
  const btnSetOption = document.createElement('button');
  btnSetOption.innerText = 'set axis option';

  let SCs: ScatterChart[];

  const newData = (d: any) => {
    if (applicationTotal.length > 5) {
      applicationTotal.shift();
    }
    const newServerKeys = Object.keys(d.result.activeThreadCounts);
    // ['alpha', 'beta', 'charlie', 'delta']
    //
    [...Array(CHART_COUNT)].forEach((_, i) => {
      const key = newServerKeys[i % 4];
      if (refinedData[key]) {
        if (refinedData[key].length > 4) {
          refinedData[key].shift();
        }
        refinedData[key].push(d.result.activeThreadCounts[key].status);
      } else {
        refinedData[key] = [d.result.activeThreadCounts[key].status];
      }

      d.result.activeThreadCounts[key].status.forEach((stat: any, j: number) => {
        SCs[i].render(
          [
            {
              x: d.result.timeStamp,
              y: stat,
              type: j === 0 ? 'success' : 'fail',
            },
          ],
          { append: true },
        );
      });
    });
  };

  setTimeout(() => {
    let dataIndex = 0;
    let interval: NodeJS.Timer;

    SCs = [...Array(CHART_COUNT)].map((_, i) => {
      const SC = newScatterChart(chartWrappers[i], {
        axis: {
          x: {
            min: 1683532197132 - 7000,
            max: 1683532197132 - 2000,
            tick: {
              count: 6,
              strokeColor: 'transparent',
              format: (_) => '',
            },
          },
          y: {
            min: 0,
            max: 10,
            tick: {
              count: 3,
              strokeColor: 'transparent',
              format: (value) => `${value.toLocaleString()}`,
            },
          },
        },
        legend: {
          hidden: true,
        },
        guide: {
          hidden: true,
        },
        data: [
          {
            type: 'success',
            color: 'green',
            shape: 'area',
            priority: 11,
          },
          {
            type: 'fail',
            color: 'red',
            shape: 'area',
            priority: 1,
          },
        ],
      });
      // const newData = data1.staticData.map((d) => ({ ...d, x: d.x + 47335 }));
      // SC.render(newData, { append: true });
      return SC;
    });

    wrapper.append(btnStart);
    wrapper.append(btnStop);
    wrapper.append(btnSetOption);

    btnStart.addEventListener('click', () => {
      SCs.forEach((SC) => {
        SC.startRealtime(5000);
      });

      if (!interval) {
        newData(staticData[dataIndex]);
        interval = setInterval(() => {
          dataIndex++;
          newData(staticData[dataIndex]);
          if (dataIndex === staticData.length - 1) {
            clearInterval(interval as any);
          }
        }, 1000);
      }
    });

    btnStop.addEventListener('click', () => {
      SCs.forEach((SC) => {
        SC.stopRealtime();
      });
    });

    btnSetOption.addEventListener('click', () => {
      SCs.forEach((SC) => {
        SC.setOption({
          axis: {
            y: {
              min: Math.floor(Math.random() * 10),
              max: Math.floor(Math.random() * 90) + 10,
            },
          },
        });
      });
    });
  }, 500);
  return wrapper;
};
