import { interval, of, animationFrameScheduler } from 'rxjs';
import { map, takeWhile, concat } from 'rxjs/operators';
import * as moment from 'moment-timezone';
import { IOptions } from './scatter-chart.class';
import { ScatterChartSizeCoordinateManager } from './scatter-chart-size-coordinate-manager.class';

export class ScatterChartAxisRenderer {
    private ticksX: number;
    private ticksY: number;

    private elementAxisCanvas: HTMLCanvasElement;
    private ctxAxisCanvas: CanvasRenderingContext2D;
    private elementAxisContainer: HTMLElement;
    private elementsAxisX: HTMLElement[] = [];
    private elementsAxisY: HTMLElement[] = [];
    private elementAxisXUnit: HTMLElement;
    private elementAxisYUnit: HTMLElement;
    constructor(
        private options: IOptions,
        private coordinateManager: ScatterChartSizeCoordinateManager,
        private elementContainer: HTMLElement
    ) {
        this.ticksX = this.options.ticks.x - 1;
        this.ticksY = this.options.ticks.y - 1;

        this.makeAxisCanvas();
        this.drawAxisLine();
        this.drawAxisValue();
        this.updateAxisValue();
    }
    private makeAxisCanvas(): void {
        this.elementAxisCanvas = document.createElement('canvas');
        this.elementAxisCanvas.setAttribute('width', this.coordinateManager.getWidth() + 'px');
        this.elementAxisCanvas.setAttribute('height', this.coordinateManager.getHeight() + 'px');
        this.elementAxisCanvas.setAttribute('style', 'top: 0px; z-index: 10; position: absolute');

        this.elementContainer.appendChild(this.elementAxisCanvas);
        this.ctxAxisCanvas = this.elementAxisCanvas.getContext('2d');
    }
    private drawAxisLine(): void {
        const width = this.coordinateManager.getWidth();
        const height = this.coordinateManager.getHeight();
        const oPadding = this.coordinateManager.getPadding();
        const bubbleHalfSize = this.coordinateManager.getBubbleHalfSize();
        const lineColor = this.options.lineColor;
        const gridAxisStyle = this.options.gridAxisStyle;
        const tickX = this.coordinateManager.getWidthOfChartSpace() / this.ticksX;
        const tickY = this.coordinateManager.getHeightOfChartSpace() / this.ticksY;
        const xTickLength = this.options.tickLength.x;
        const yTickLength = this.options.tickLength.y;

        this.ctxAxisCanvas.lineWidth = gridAxisStyle.lineWidth;
        this.ctxAxisCanvas.globalAlpha = 1;
        this.ctxAxisCanvas.lineCap = 'round';
        this.ctxAxisCanvas.strokeStyle = lineColor;

        this.ctxAxisCanvas.beginPath();
        this.moveTo(this.ctxAxisCanvas, oPadding.left, oPadding.top);
        this.lineTo(this.ctxAxisCanvas, oPadding.left, height - oPadding.bottom);
        this.lineTo(this.ctxAxisCanvas, width - oPadding.right, height - oPadding.bottom);
        this.ctxAxisCanvas.stroke();

        for (let i = 0 ; i <= this.ticksX ; i++) {
            const mov = oPadding.left + bubbleHalfSize + tickX * i;
            this.ctxAxisCanvas.beginPath();
            this.moveTo(this.ctxAxisCanvas, mov, height - oPadding.bottom);
            this.lineTo(this.ctxAxisCanvas, mov, height - oPadding.bottom + xTickLength);
            this.ctxAxisCanvas.stroke();
        }

        for (let i = 0 ; i <= this.ticksY ; i++) {
            const mov = height - (oPadding.bottom + bubbleHalfSize + tickY * i);
            this.ctxAxisCanvas.beginPath();
            this.moveTo(this.ctxAxisCanvas, oPadding.left, mov);
            this.lineTo(this.ctxAxisCanvas, oPadding.left - yTickLength, mov);
            this.ctxAxisCanvas.stroke();
        }
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
    private drawAxisValue() {
        const widthTickX = this.coordinateManager.getWidthOfChartSpace() / this.ticksX;
        const widthTickY = this.coordinateManager.getHeightOfChartSpace() / this.ticksY;
        const width = this.coordinateManager.getWidth();
        const height = this.coordinateManager.getHeight();
        const padding = this.coordinateManager.getPadding();
        const bubbleHalfSize = this.coordinateManager.getBubbleHalfSize();
        const axisColor = this.options.axisColor;

        this.elementAxisContainer = document.createElement('div');
        const elementDivStyle = `top: 0px; width:${width}px ; height:${height}px ;cursor: corsshair; z-index: 10; position: absolute; background-color: rgba(0,0,0,0)`;
        this.elementAxisContainer.setAttribute('style', elementDivStyle);
        this.elementContainer.appendChild(this.elementAxisContainer);

        // x axis
        for (let i = 0 ; i <= this.ticksX ; i++) {
            const tempAxisDiv = document.createElement('div');
            const style = `
                top: ${height - padding.bottom + 10}px;
                left: ${padding.left - (widthTickX / 2) + (i * widthTickX)}px;
                width: ${widthTickX}px;
                color: ${axisColor};
                position: absolute;
                text-align: center;
            `;
            tempAxisDiv.setAttribute('style', style + this.options.axisLabelStyle);
            tempAxisDiv.textContent = ' ';
            this.elementsAxisX.push(tempAxisDiv);
            this.elementAxisContainer.appendChild(tempAxisDiv);
        }
        // y axis
        for (let i = 0 ; i <= this.ticksY ; i++) {
            const tempAxisDiv = document.createElement('div');
            const style = `
                top: ${bubbleHalfSize + (i * widthTickY) + padding.top - 10}px;
                left: 0px;
                width: ${padding.left - 10}px;
                color: ${axisColor};
                position: absolute;
                text-align: center;
                vertical-align: middle;
            `;
            tempAxisDiv.setAttribute('style', style + this.options.axisLabelStyle);
            tempAxisDiv.textContent = ' ';
            this.elementsAxisY.push(tempAxisDiv);
            this.elementAxisContainer.appendChild(tempAxisDiv);
        }

        // x axis unit
        const xUnit = this.options.axisUnit.x;
        if (xUnit !== '') {
            this.elementAxisXUnit = document.createElement('div');
            const style = `
                top: ${height - padding.bottom + 10}px;
                right: 80px;
                color: ${axisColor};
                position: absolute;
                text-align: right;
            `;
            this.elementAxisXUnit.setAttribute('style', style + this.options.axisLabelStyle);
            this.elementAxisXUnit.textContent = xUnit;
            this.elementAxisContainer.appendChild(this.elementAxisXUnit);
        }
        // y axis unit
        const yUnit = this.options.axisUnit.y;
        if (yUnit !== '') {
            this.elementAxisYUnit = document.createElement('div');
            const style = `
                top: 0px;
                left: 10px;
                color: ${axisColor};
                width: ${padding.left - 15};
                position: absolute;
                text-align: right;
                vertical-align: middle;
            `;
            this.elementAxisYUnit.setAttribute('style', style + this.options.axisLabelStyle);
            this.elementAxisYUnit.textContent = yUnit;
            this.elementAxisContainer.appendChild(this.elementAxisYUnit);
        }
    }
    updateAxisValue(animation?: boolean, duration?: number): void {
        const xRange = this.coordinateManager.getX();
        const tickX = (this.coordinateManager.getGapX()) / this.ticksX;
        this.elementsAxisX.forEach((element: Element, index: number) => {
            const xMoment = moment(tickX * index + xRange.from).tz(this.options.timezone);
            element.innerHTML = xMoment.format(this.options.dateFormat[0]) + '<br>' + xMoment.format(this.options.dateFormat[1]);
        });

        const yRange = this.coordinateManager.getY();
        const tickY = (this.coordinateManager.getGapY()) / this.ticksY;
        this.elementsAxisY.forEach((element: Element, index: number) => {
            element.innerHTML = ((yRange.to + yRange.from) - ((tickY * index) + yRange.from)).toLocaleString();
        });
        if (animation === true) {
            this.animateBackground(this.elementsAxisX[this.elementsAxisX.length - 1], duration);
        }
    }
    private animateBackground(element: HTMLElement, duration: number): void {
        const start = animationFrameScheduler.now();
        interval(1, animationFrameScheduler).pipe(
            map(() => {
                return (animationFrameScheduler.now() - start) / duration;
            }),
            takeWhile((opacity: number) => {
                return opacity <= 1;
            }),
            concat(of(1))
        ).subscribe((opacity: number) => {
            element.style.backgroundColor = `rgba(254, 255, 210, ${1 - opacity}`;
        });

    }
    drawToCanvas(ctxDownload: CanvasRenderingContext2D, topPadding: number): CanvasRenderingContext2D {
        let xLastLabelLeftPosition = 0;
        ctxDownload.drawImage(this.elementAxisCanvas, 0, topPadding);
        // x axis
        ctxDownload.textAlign = 'center';
        this.elementsAxisX.forEach((element: HTMLElement) => {
            ctxDownload.font = element.style.font;
            ctxDownload.fillStyle = element.style.color;
            const axisX = element.innerHTML.replace(/<br>/gi, ' ').split(' ');
            axisX.forEach((txt: string, index: number) => {
                ctxDownload.fillText(txt, parseInt(element.style.left, 10) + element.getBoundingClientRect().width / 2, parseInt(element.style.top, 10) + (15 * (index + 1)) + topPadding);
            });
            xLastLabelLeftPosition = Math.max(xLastLabelLeftPosition, parseInt(element.style.left, 10));
        });
        // y axis
        ctxDownload.textAlign = 'right';
        this.elementsAxisY.forEach((element: HTMLElement) => {
            ctxDownload.font = element.style.font;
            ctxDownload.fillStyle = element.style.color;
            ctxDownload.fillText(element.textContent, element.getBoundingClientRect().width - 10, parseInt(element.style.top, 10) + 15 +  + topPadding);
        });
        // x label
        if (this.options.axisUnit.x !== '' && this.elementAxisXUnit) {
            ctxDownload.textAlign = 'right';
            ctxDownload.font = this.elementAxisXUnit.style.fontFamily;
            ctxDownload.fillStyle = this.elementAxisXUnit.style.color;
            ctxDownload.fillText(this.elementAxisXUnit.textContent, xLastLabelLeftPosition + this.elementAxisXUnit.getBoundingClientRect().width, parseInt(this.elementAxisXUnit.style.top, 10) + 30 + topPadding);
        }
        // y label
        if (this.options.axisUnit.y !== '' && this.elementAxisYUnit) {
            ctxDownload.textAlign = 'right';
            ctxDownload.font = this.elementAxisYUnit.style.fontFamily;
            ctxDownload.fillStyle = this.elementAxisYUnit.style.color;
            ctxDownload.fillText(this.elementAxisYUnit.textContent, parseInt(this.elementAxisYUnit.style.left, 10) + this.elementAxisYUnit.getBoundingClientRect().width, 10 + topPadding);
        }
        return ctxDownload;
    }
    reset(): void {
        this.updateAxisValue();
    }
}
