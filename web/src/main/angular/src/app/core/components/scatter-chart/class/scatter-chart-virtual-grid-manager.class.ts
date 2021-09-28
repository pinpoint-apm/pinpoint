import { ScatterChartSizeCoordinateManager } from './scatter-chart-size-coordinate-manager.class';

export class ScatterChartVirtualGridManager {
    static readonly gridUnit = 10; // {n}px * {n}px

    constructor(
        private coordinateManager: ScatterChartSizeCoordinateManager
    ) {}

    getGrid(dot: number[]): {x0: number, x1: number, y0: number, y1: number} {
        const {x, y} = this.coordinateManager.getCoord(dot);

        const xGrid = this.getXGrid(x);
        const yGrid = this.getYGrid(y);

        return {...xGrid, ...yGrid};
    }

    private getGridRange(value: number, min: number, max: number): {min: number, max: number} {
        const gridUnit = ScatterChartVirtualGridManager.gridUnit;

        let gridRange: {min: number, max: number};

        for (let start = min; start < max; start += gridUnit) {
            const end = start + gridUnit > max ? max : start + gridUnit;

            if (value >= start && value < end) {
                gridRange = {min: start, max: end};
                break;
            } else {
                continue;
            }
        }

        return gridRange;
    }

    private getXGrid(x: number): {x0: number, x1: number} {
        const chartWidth = this.coordinateManager.getWidthOfChartSpace();
        const canvasOrder = Math.floor(x / chartWidth); // canvas order for the point included
        const minX = chartWidth * canvasOrder;
        const maxX = minX + chartWidth;

        const {min: x0, max: x1} = this.getGridRange(x, minX, maxX);

        return {x0, x1};
    }

    private getYGrid(y: number): {y0: number, y1: number} {
        const chartHeight = this.coordinateManager.getHeightOfChartSpace() + this.coordinateManager.getBubbleSize();
        const minY = 0;
        const maxY = chartHeight;

        const {min: y0, max: y1} = this.getGridRange(y, minY, maxY);

        return {y0, y1};
    }
}
