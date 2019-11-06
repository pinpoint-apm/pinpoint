import { IOptions } from './scatter-chart.class';
import { ScatterChartSizeCoordinateManager } from './scatter-chart-size-coordinate-manager.class';

export class ScatterChartGridRenderer {
    private ticksX: number;
    private ticksY: number;
    private elementGridCanvas: HTMLCanvasElement;
    private ctxGridCanvas: CanvasRenderingContext2D;
    constructor(private options: IOptions, private coordinateManager: ScatterChartSizeCoordinateManager, private elementContainer: HTMLElement) {
        this.ticksX = this.options.ticks.x - 1;
        this.ticksY = this.options.ticks.y - 1;

        this.makeGridCanvas();
        this.drawGridLine();
    }
    private makeGridCanvas(): void {
        this.elementGridCanvas = document.createElement('canvas');
        this.elementGridCanvas.setAttribute('width', this.coordinateManager.getWidth() + 'px');
        this.elementGridCanvas.setAttribute('height', this.coordinateManager.getHeight() + 'px');
        this.elementGridCanvas.setAttribute('style', 'top: 0px; z-index: 0; position: absolute');

        this.elementContainer.appendChild(this.elementGridCanvas);
        this.ctxGridCanvas = this.elementGridCanvas.getContext('2d');
    }
    private drawGridLine(): void {
        this.setStyle(this.ctxGridCanvas, this.options.gridAxisStyle);
        this.drawXGridLine();
        this.drawYGridLine();
    }
    private drawXGridLine(): void {
        const height = this.coordinateManager.getHeight();
        const padding = this.coordinateManager.getPadding();
        const bubbleHalfSize = this.coordinateManager.getBubbleHalfSize();
        const tickX = this.coordinateManager.getWidthOfChartSpace() / this.ticksX;

        const xStart = padding.left + bubbleHalfSize;
        const yStart = padding.top;
        const yEnd = height - padding.bottom;
        for (let i = 0 ; i <= this.ticksX ; i++) {
            const x = xStart + (tickX * i);
            this.drawLine(this.ctxGridCanvas, x, yStart, x, yEnd);
        }
    }
    private drawYGridLine(): void {
        const width = this.coordinateManager.getWidth();
        const height = this.coordinateManager.getHeight();
        const padding = this.coordinateManager.getPadding();
        const bubbleHalfSize = this.coordinateManager.getBubbleHalfSize();
        const tickY = this.coordinateManager.getHeightOfChartSpace() / this.ticksY;

        const xStart = padding.left;
        const xEnd = width - padding.right;
        const yZero = padding.bottom + bubbleHalfSize;
        for (let i = 0 ; i <= this.ticksY ; i++) {
            const y = height - (yZero + tickY * i);
            this.drawLine(this.ctxGridCanvas, xStart, y, xEnd, y);
        }
    }
    private drawLine(context: CanvasRenderingContext2D, xStart: number, yStart: number, xEnd: number, yEnd: number): void {
        context.beginPath();
        this.moveTo(context, xStart, yStart);
        this.lineTo(context, xEnd, yEnd);
        context.stroke();
    }
    private setStyle(ctx: any, styles: any): void {
        Object.keys(styles).forEach((key: string) => {
            if (key === 'lineDash') {
                ctx.setLineDash(styles[key]);
            } else {
                ctx[key] = styles[key];
            }
        });
    }
    private moveTo(ctx: any, x: number, y: number): void {
        if (x % 1 === 0) {
            x += 0.5;
        }
        if (y % 1 === 0) {
            y += 0.5;
        }
        ctx.moveTo(x, y);
    }
    private lineTo(ctx: any, x: number, y: number): void {
        if (x % 1 === 0) {
            x += 0.5;
        }
        if (y % 1 === 0) {
            y += 0.5;
        }
        ctx.lineTo(x, y);
    }
    drawToCanvas(ctxDownload: CanvasRenderingContext2D, topPadding: number): CanvasRenderingContext2D {
        ctxDownload.drawImage(this.elementGridCanvas, 0, topPadding);
        return ctxDownload;
    }
}
