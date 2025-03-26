import { DefaultValue } from './HeatmapSetting';

function generateMockData(width: number, height: number) {
  const baseTime = new Date().getTime();
  const matrixData = [];

  for (let row = height - 1; row >= 0; row--) {
    // row는 height부터 1까지 감소
    const timestamp = new Date(baseTime - (height - row) * 10 * 60 * 1000)
      .toISOString()
      .replace('T', ' ')
      .slice(0, 19);
    const cellData = [];

    for (let column = 0; column < width; column++) {
      const elapsedTime = ((column + 1) * DefaultValue.yMax) / 10; // column 증가에 따라 elapsedTime 증가
      const successCount = Math.floor(Math.random() * DefaultValue.yMax);
      const failCount = Math.floor(Math.random() * 100);

      cellData.push({ column, elapsedTime, successCount, failCount });
    }

    matrixData.push({ row, timestamp, cellData });
  }

  return {
    size: { width, height },
    matrixData,
  };
}

export const mockData = generateMockData(10, 10);
