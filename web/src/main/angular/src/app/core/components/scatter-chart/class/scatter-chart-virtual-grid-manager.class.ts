import { ScatterChartSizeCoordinateManager } from './scatter-chart-size-coordinate-manager.class';

export class ScatterChartVirtualGridManager {
    // ! x 값이 현재의 차트영역을 벗어난 위치일 때(=리얼타임때 인듯) 에 대한 고민.
    static readonly gridUnit = 10; // {n}px * {n}px
    private virtualGrid: Array<{x0: number, x1: number, y0: number, y1: number}> = [];

    constructor(
        private coordinateManager: ScatterChartSizeCoordinateManager
    ) {
        this.initGrid();
    }

    private initGrid(): void {
        const chartWidth = this.coordinateManager.getWidthOfChartSpace();
        const chartHeight = this.coordinateManager.getHeightOfChartSpace() + this.coordinateManager.getBubbleSize();
        const gridUnit = ScatterChartVirtualGridManager.gridUnit;
        // Size of the scatter chart: 354 * 170

        for (let x1 = chartWidth; x1 > 0; x1 -= gridUnit) {
            for (let y1 = chartHeight; y1 > 0; y1 -= gridUnit) {
                const x0 = x1 - gridUnit > 0 ? x1 - gridUnit : 0;
                const y0 = y1 - gridUnit > 0 ? y1 - gridUnit : 0;

                this.virtualGrid.push({x0, y0, x1, y1});
            }
        }

        // console.log(this.virtualGrid);
    }

    getGrid(dot: number[]): {x0: number, x1: number, y0: number, y1: number} {
        const {x, y} = this.coordinateManager.getCoord(dot);

        return this.virtualGrid.find(({x0, y0, x1, y1}: {[key: string]: number}) => {
            return (x <= x1 && x > x0) && (y <= y1 && y > y0);
        });
    }
}
