import { IOptions } from './scatter-chart.class';

export class ScatterChartSizeCoordinateManager {
    private initFromX: number; // for realtime
    private widthOfChartSpace: number;
    private heightOfChartSpace: number;
    private timePerPixel: number;
    private pixelPerTime: number;
    constructor(private options: IOptions) {
        this.initVar();
    }
    private initVar(): void {
        const bubbleRaduis = this.options.bubbleRadius;

        this.initFromX = this.options.x.from;
        this.widthOfChartSpace = (this.options.width - (this.options.padding.left + this.options.padding.right)) - bubbleRaduis * 2;
        this.heightOfChartSpace = (this.options.height - (this.options.padding.top + this.options.padding.bottom)) - bubbleRaduis * 2;

        this.calcuUnitValue();
    }
    private calcuUnitValue(): void {
        this.timePerPixel = this.getGapX() / this.widthOfChartSpace;
        this.pixelPerTime = this.widthOfChartSpace / this.getGapX();
    }
    getInitFromX(): number {
        return this.initFromX;
    }
    getLeftPadding(): number {
        return this.options.padding.left;
    }
    getTopPadding(): number {
        return this.options.padding.top;
    }
    getCanvasWidth(): number {
        return this.widthOfChartSpace;
    }
    getWidth(): number {
        return this.options.width;
    }
    getHeight(): number {
        return this.options.height;
    }
    getX(): {from: number, to: number} {
        return {
            from: this.options.x.from,
            to: this.options.x.to
        };
    }
    resetInitX(fromX: number): void {
        this.initFromX = fromX;
    }
    setX(from: number, to: number, bReset?: boolean): void {
        this.options.x.from = from;
        this.options.x.to = to;
        this.calcuUnitValue();
    }
    getY(): {from: number, to: number} {
        return {
            from: this.options.y.from,
            to: this.options.y.to
        };
    }
    setY(from: number, to: number): void {
        this.options.y.from = from;
        this.options.y.to = to;
    }
    getPadding(): {top: number, left: number, right: number, bottom: number} {
        return {
            top: this.options.padding.top,
            left: this.options.padding.left,
            right: this.options.padding.right,
            bottom: this.options.padding.bottom
        };
    }
    getBubbleHalfSize(): number {
        return this.options.bubbleRadius;
    }
    getBubbleSize(): number {
        return this.options.bubbleRadius * 2;
    }
    getWidthOfChartSpace(): number {
        return this.widthOfChartSpace;
    }
    getHeightOfChartSpace(): number {
        return this.heightOfChartSpace;
    }
    getPixelPerTime(): number {
        return this.pixelPerTime;
    }
    getTimePerPixel(): number {
        return this.timePerPixel;
    }
    parseXDataToXChart(x: number, plusPadding: boolean): number {
       return Math.round( ( ( x - this.options.x.from ) / this.getGapX() ) * this.widthOfChartSpace ) + this.getBubbleHalfSize() + ( plusPadding ? this.options.padding.left : 0 );
    }
    parseYDataToYChart(y: number): number {
        return Math.round(this.heightOfChartSpace - (((y - this.options.y.from) / this.getGapY()) * this.heightOfChartSpace)) + this.getBubbleHalfSize();
    }
    parseZDataToZChart(z: number): number {
        return Math.round(((z - this.options.z.from) / this.getGapZ()) * this.getBubbleHalfSize());
    }
    getXOfPixel(): number {
        return Math.round(this.getGapX() / this.widthOfChartSpace);
    }
    getYOfPixel(): number {
        return Math.round(this.getGapY() / this.heightOfChartSpace);
    }
    parseMouseXToXData(x: number): number {
        return Math.round((x / this.widthOfChartSpace) * this.getGapX()) + this.options.x.from;
    }
    parseMouseYToYData(y: number): number {
        return Math.round(this.options.y.from + ((y / this.heightOfChartSpace) * this.getGapY()));
    }
    getGapX(): number {
        return this.options.x.to - this.options.x.from;
    }
    getGapY(): number {
        return this.options.y.to - this.options.y.from;
    }
    getGapZ(): number {
        return this.options.z.to - this.options.z.from;
    }
}
