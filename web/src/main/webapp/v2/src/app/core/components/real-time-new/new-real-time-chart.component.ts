import { Component, OnInit, ViewChild, ElementRef, Input, Output, AfterViewInit, OnDestroy, EventEmitter } from '@angular/core';
import * as moment from 'moment-timezone';

import { IActiveThreadCounts, ResponseCode } from 'app/core/components/real-time/real-time-websocket.service';

@Component({
    selector: 'pp-new-real-time-chart',
    templateUrl: './new-real-time-chart.component.html',
    styleUrls: ['./new-real-time-chart.component.css']
})
export class NewRealTimeChartComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('realTime') canvasRef: ElementRef;
    @Input()
    set activeThreadCounts(activeThreadCounts: { [key: string]: IActiveThreadCounts }) {
        this.numOfChart = Object.keys(activeThreadCounts).length;
        this._activeThreadCounts = activeThreadCounts;

        Object.keys(this._activeThreadCounts).forEach((key: string, i: number) => {
            const status = this._activeThreadCounts[key].status;

            if (status) {
                this.dataList[i] ? this.dataList[i].push(status) : this.dataList[i] = [status];
            }
        });
    }
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Input()
    set timeStamp(timeStamp: number) {
        this.timeStampList.push(timeStamp);
        if (this.timeStampList.length === 1) {
            this.firstTimeStamp = this.timeStampList[0] - 2000;
        }
    }
    @Input() chartOption: { [key: string]: any };
    @Output() outClick = new EventEmitter<string>();

    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private firstTimeStamp: number;
    private gridLineStart: number = null;
    private chartStart: number = null;
    private animationFrameId: number;
    private timeStampList: number[] = [];
    private _activeThreadCounts: { [key: string]: IActiveThreadCounts };
    private dataList: number[][][] = [];
    private numOfChart: number;
    private maxRatio = 3 / 5; // 차트의 높이에 대해 데이터의 최댓값을 위치시킬 비율
    private ratio: number; // maxRatio를 바탕으로 각 데이터에 적용되는 비율
    private startingXPos: number; // 최초로 움직이기 시작하는 점의 x좌표
    private lastMousePosInCanvas: ICoordinate;
    private chartNumPerRow: number;
    private linkIconInfoList: { [key: string]: any }[] = []; // { leftX, rightX, topY, bottomY, agentKey } 를 담은 object의 배열
    showTooltip = false;
    tooltipDataObj = {
        title: '',
        values: [] as number[],
    };

    constructor(
        private el: ElementRef
    ) {}
    ngOnInit() {}
    ngAfterViewInit() {
        this.canvas = this.canvasRef.nativeElement;
        this.ctx = this.canvas.getContext('2d');

        this.setCanvasSize();

        this.animationFrameId = requestAnimationFrame((t) => this.draw(t));
        this.addEventListener();
    }

    ngOnDestroy() {
        cancelAnimationFrame(this.animationFrameId);
    }

    private setCanvasSize(): void {
        const { titleHeight, containerHeight, canvasBottomPadding } = this.chartOption;

        this.canvas.width = this.el.nativeElement.offsetWidth;
        this.setChartNumPerRow();
        this.canvas.height = this.getTopEdgeYPos(this.numOfChart - 1) + titleHeight + containerHeight + canvasBottomPadding;
    }

    private setChartNumPerRow(): void {
        const { canvasLeftPadding, canvasRightPadding, containerWidth, gapBtnChart } = this.chartOption;

        this.chartNumPerRow = Math.floor((this.canvas.width - canvasLeftPadding - canvasRightPadding) / (containerWidth + gapBtnChart));
    }

    private addEventListener(): void {
        window.addEventListener('resize', (() => this.onResize()));
    }

    private onResize(): void {
        this.setCanvasSize();
    }

    private getLeftEdgeXPos(i: number): number {
        // 차트 컨테이너 왼쪽 모서리 x좌표를 리턴
        const { canvasLeftPadding, containerWidth, gapBtnChart } = this.chartOption;

        return canvasLeftPadding + (containerWidth + gapBtnChart) * (i % this.chartNumPerRow);
    }

    private getTopEdgeYPos(i: number): number {
        // 차트 컨테이너 위쪽 모서리 y좌표를 리턴
        const { canvasTopPadding, containerHeight, titleHeight, gapBtnChart } = this.chartOption;

        return canvasTopPadding + (containerHeight + titleHeight + gapBtnChart) * Math.floor(i / this.chartNumPerRow);
    }

    private draw(timestamp: number): void {
        const { drawHGridLine, drawVGridLine, showXAxis, tooltipEnabled } = this.chartOption;

        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.drawChartTitle();
        this.drawChartContainerRect();
        if (drawHGridLine) {
            this.drawHGridLine();
        }

        if (drawVGridLine) {
            this.drawVGridLine(timestamp);
        }

        if (showXAxis) {
            this.drawXAxis();
        }

        if (this.timeStampList.length !== 0) {
            this.drawChart(timestamp);
            if (tooltipEnabled) {
                this.setTooltip();
            }
        }

        this.drawErrorText();
        this.animationFrameId = requestAnimationFrame((t) => this.draw(t));
    }

    private drawErrorText(): void {
        const { chartInnerPadding, errorFontSize, titleHeight, chartHeight, chartWidth } = this.chartOption;

        Object.keys(this._activeThreadCounts).forEach((key: string, i: number) => {
            const { code, message } = this._activeThreadCounts[key];
            const isResponseSuccess = code === ResponseCode.SUCCESS;

            if (!isResponseSuccess) {
                const originXPos = this.getOriginXPos(i);
                const x = originXPos + chartWidth / 2;
                const isOverflow = (str: string) => this.ctx.measureText(str).width > chartWidth - 10;

                const words = message.split(' ');
                const arrangedText: string[] = []; // 여기에 message를 루프돌면서 overflow하지않는 단위로 잘라서 넣어줄 예정. 그리고, 이걸로 마지막에 fillText()
                let startIndex = 0;
                let lastIndex = words.length - 1;

                while (message !== arrangedText.join(' ')) {
                    const substr = words.slice(startIndex, lastIndex + 1).join(' ');

                    if (isOverflow(substr)) {
                        lastIndex--;
                    } else {
                        arrangedText.push(substr);
                        startIndex = lastIndex + 1;
                        lastIndex = words.length - 1;
                    }
                }

                const length = arrangedText.length;

                arrangedText.forEach((text: string, j: number) => {
                    const y = this.getTopEdgeYPos(i) + titleHeight + chartInnerPadding + (j + 1) * chartHeight / (length + 1);

                    this.ctx.font = `600 ${errorFontSize} Nanum Gothic`;
                    this.ctx.fillStyle = '#c04e3f';
                    this.ctx.textAlign = 'center';
                    this.ctx.textBaseline = 'middle';
                    this.ctx.fillText(text, x, y);
                });
            }
        });
    }

    private drawChartTitle(): void {
        const { containerWidth, titleHeight, titleFontSize, marginRightForLinkIcon, linkIconCode } = this.chartOption;

        Object.keys(this._activeThreadCounts).forEach((key: string, i: number) => {
            this.ctx.fillStyle = '#74879a';
            this.ctx.fillRect(this.getLeftEdgeXPos(i), this.getTopEdgeYPos(i), containerWidth, titleHeight);

            this.ctx.font = `${titleFontSize} Nanum Gothic`;
            this.ctx.fillStyle = '#fff';
            this.ctx.textAlign = 'center';
            this.ctx.textBaseline = 'middle';
            this.ctx.fillText(this.getChartTitleText(key), this.getLeftEdgeXPos(i) + containerWidth / 2, this.getTopEdgeYPos(i) + titleHeight / 2);

            if (linkIconCode) {
                this.ctx.font = '600 9px "Font Awesome 5 Free"';
                this.ctx.textAlign = 'right';
                this.ctx.fillText(linkIconCode, this.getLeftEdgeXPos(i) + containerWidth - marginRightForLinkIcon, this.getTopEdgeYPos(i) + titleHeight / 2);

                const linkIconWidth = this.ctx.measureText(linkIconCode).width;

                this.linkIconInfoList.push({
                    leftX: this.getLeftEdgeXPos(i) + containerWidth - marginRightForLinkIcon - linkIconWidth,
                    rightX: this.getLeftEdgeXPos(i) + containerWidth - marginRightForLinkIcon,
                    topY: this.getTopEdgeYPos(i) + titleHeight / 2 - linkIconWidth / 2,
                    bottomY: this.getTopEdgeYPos(i) + titleHeight / 2 + linkIconWidth / 2,
                    key
                });
            }
        });
    }

    private getChartTitleText(text: string): string {
        const { containerWidth, marginRightForLinkIcon, linkIconCode, ellipsis } = this.chartOption;
        const linkIconWidth = linkIconCode ? this.ctx.measureText(linkIconCode).width : 0;
        const ellipsisWidth = this.ctx.measureText(ellipsis).width;
        const textWidth = this.ctx.measureText(text).width;
        const maxWidth = linkIconCode ? containerWidth / 2 - linkIconWidth - marginRightForLinkIcon - 5 : containerWidth / 2;
        const isOverflow = textWidth / 2  > maxWidth;

        if (isOverflow) {
            let length = text.length;
            let newText;
            let newTextWidth;

            do {
                newText = text.substring(0, length - 1);
                newTextWidth = this.ctx.measureText(newText).width;
                length--;
            } while (newTextWidth / 2 + ellipsisWidth > maxWidth);

            return newText + ellipsis;
        } else {
            return text;
        }
    }

    private drawChartContainerRect(): void {
        const { containerWidth, containerHeight, titleHeight } = this.chartOption;

        for (let i = 0; i < this.numOfChart; i++) {
            this.ctx.fillStyle = '#fff';
            this.ctx.fillRect(this.getLeftEdgeXPos(i), this.getTopEdgeYPos(i) + titleHeight, containerWidth, containerHeight);
        }
    }

    // Horizontal Grid Line
    private drawHGridLine(): void {
        const { chartHeight, chartWidth } = this.chartOption;

        for (let i = 0; i < this.numOfChart; i++) {
            const originXPos = this.getOriginXPos(i);
            const originYPos = this.getOriginYPos(i);

            // Horizontal grid line 1 (top)
            this.ctx.beginPath();
            this.ctx.moveTo(originXPos, originYPos - chartHeight);
            this.ctx.lineTo(originXPos + chartWidth, originYPos - chartHeight);
            this.ctx.strokeStyle = 'rgba(0, 0, 0, 0.2)';
            this.ctx.stroke();

            // Horizontal grid line 2 (middle)
            this.ctx.beginPath();
            this.ctx.moveTo(originXPos, originYPos - chartHeight / 2);
            this.ctx.lineTo(originXPos + chartWidth, originYPos - chartHeight / 2);
            this.ctx.strokeStyle = 'rgba(0, 0, 0, 0.2)';
            this.ctx.stroke();
        }
    }

    // Vertical Grid Line
    private drawVGridLine(timestamp: number): void {
        const { chartWidth, gridLineSpeedControl, chartHeight } = this.chartOption;

        for (let i = 0; i < this.numOfChart; i++) {
            if (!this.gridLineStart) {
                this.gridLineStart = timestamp;
            }

            const xPos = this.getOriginXPos(i) + chartWidth - Math.floor((timestamp - this.gridLineStart) / gridLineSpeedControl) % chartWidth;
            const originYPos = this.getOriginYPos(i);

            this.ctx.beginPath();
            this.ctx.moveTo(xPos, originYPos - chartHeight);
            this.ctx.lineTo(xPos, originYPos);
            this.ctx.strokeStyle = 'rgba(0, 0, 0, 0.2)';
            this.ctx.stroke();
        }
    }

    private drawYAxisLabel(max: number): void {
        const { marginFromYAxis, chartHeight } = this.chartOption;

        for (let i = 0; i < this.numOfChart; i++) {
            const xPos = this.getOriginXPos(i) - marginFromYAxis;
            const originYPos = this.getOriginYPos(i);
            let maxLabel = max === 0 ? null : max / this.maxRatio;
            let midLabel = max === 0 ? null : maxLabel / 2;

            if (maxLabel && !Number.isInteger(midLabel)) {
                midLabel = Math.round(midLabel);
                maxLabel = 2 * midLabel;
                this.maxRatio = max / maxLabel;
            }

            this.ctx.font = '9px Nanum Gothic';
            this.ctx.textBaseline = 'middle';
            this.ctx.textAlign = 'right';
            this.ctx.fillStyle = '#333';
            this.ctx.fillText(`0`, xPos, originYPos);
            if (maxLabel) {
                this.ctx.fillText(`${midLabel}`, xPos, originYPos - chartHeight / 2);
                this.ctx.fillText(`${maxLabel}`, xPos, originYPos - chartHeight);
            }
        }
    }

    private drawXAxis(): void {
        const { chartWidth } = this.chartOption;

        for (let i = 0; i < this.numOfChart; i++) {
            const originXPos = this.getOriginXPos(i);
            const originYPos = this.getOriginYPos(i);

            this.ctx.beginPath();
            this.ctx.moveTo(originXPos, originYPos);
            this.ctx.lineTo(originXPos + chartWidth, originYPos);
            this.ctx.strokeStyle = 'rgba(0, 0, 0, 0.2)';
            this.ctx.stroke();
        }
    }

    private getOriginXPos(chartIndex: number): number {
        const { chartInnerPadding, yAxisLabelWidth, marginFromYAxis } = this.chartOption;

        return this.getLeftEdgeXPos(chartIndex) + chartInnerPadding + yAxisLabelWidth + marginFromYAxis;
    }

    private getOriginYPos(chartIndex: number): number {
        const { chartInnerPadding, titleHeight, chartHeight } = this.chartOption;

        return this.getTopEdgeYPos(chartIndex) + titleHeight + chartInnerPadding + chartHeight;
    }

    private drawChart(timestamp: number): void {
        if (!this.chartStart) {
            this.chartStart = timestamp;
        }

        const { chartWidth, chartHeight, chartColors, chartSpeedControl, showYAxisLabel } = this.chartOption;
        this.startingXPos = chartWidth - Math.floor((timestamp - this.chartStart) / chartSpeedControl);
        const isOverflow = this.timeStampList.length >= 2 && this.getXPosInChart(1) < 0;

        if (isOverflow) {
            this.timeStampList.shift();
        }

        const x0 = this.getXPosInChart(0);

        if (x0 < chartWidth) {
            this.dataList.forEach((dataList: number[][], i: number) => {
                if (isOverflow) {
                    dataList.shift();
                }

                const originXPos = this.getOriginXPos(i);
                const originYPos = this.getOriginYPos(i);
                const max = Math.max(...dataList.map((data: number[]) => data.reduce((acc: number, curr: number) => acc + curr), 0));
                const dataTypeLength = dataList[0].length;
                const dataLength = dataList.length;

                if (showYAxisLabel) {
                    this.drawYAxisLabel(max);
                }

                this.ratio = max === 0 ? 1 : chartHeight * this.maxRatio / max;

                for (let j = dataTypeLength - 1; j >= 0; j--) {
                    const data = dataList.map((d: number[]) => d.slice(0, j + 1).reduce((acc: number, curr: number) => acc + curr, 0));

                    this.ctx.beginPath();

                    if (x0 < 0) {
                        // 앞 경계면 처리
                        const x1 = this.getXPosInChart(1);

                        this.ctx.moveTo(originXPos, originYPos); // 시작
                        this.ctx.lineTo(originXPos, originYPos - (data[0] * x1 - data[1] * x0) / (x1 - x0) * this.ratio);
                    } else {
                        this.ctx.moveTo(originXPos + this.getXPosInChart(0), originYPos); // 시작
                        this.ctx.lineTo(originXPos + this.getXPosInChart(0), originYPos - (data[0] * this.ratio));
                    }

                    for (let k = 1; k < dataLength; k++) {
                        const xkm1 = this.getXPosInChart(k - 1);
                        const xk = this.getXPosInChart(k);

                        if (xkm1 <= chartWidth && xk > chartWidth) {
                            // 뒷 경계면 처리
                            this.ctx.lineTo(originXPos + chartWidth, originYPos - (data[k - 1] + (chartWidth - xkm1) * (data[k] - data[k - 1]) / (xk - xkm1)) * this.ratio);
                            break;
                        } else {
                            this.ctx.lineTo(originXPos + this.getXPosInChart(k), originYPos - (data[k] * this.ratio));
                        }
                    }

                    this.ctx.lineTo(originXPos + chartWidth, originYPos); // 마지막

                    this.ctx.fillStyle = chartColors[j];
                    this.ctx.fill();
                }
            });
        }
    }

    private getXPosInChart(i: number): number {
        const { chartSpeedControl } = this.chartOption;

        return this.startingXPos + Math.floor((this.timeStampList[i] - this.firstTimeStamp) / chartSpeedControl);
    }

    calculateTooltipLeft(tooltip: HTMLElement): string {
        const { coordX } = this.lastMousePosInCanvas;
        const { chartWidth } = this.chartOption;
        const originXPos = this.getOriginXPos(0);
        const tooltipWidth = tooltip.offsetWidth;
        const ratio = (coordX - originXPos) / chartWidth;

        return `${coordX - (tooltipWidth * ratio)}px`;
    }

    calculateTooltipCaretLeft(tooltipCaret: HTMLElement): string {
        const { coordX } = this.lastMousePosInCanvas;

        return `${coordX - (tooltipCaret.offsetWidth / 2)}px`;
    }

    private drawTooltipPoint(i: number): void {
        const { chartColors } = this.chartOption;
        const originXPos = this.getOriginXPos(0);
        const originYPos = this.getOriginYPos(0);
        const data = this.dataList[0][i]; // [0, 1, 2, 3]
        const length = data.length;
        const x = originXPos + this.getXPosInChart(i);
        const r = 3;

        if (x > originXPos) {
            for (let j = 0; j < length; j++) {
                if (data[j] === 0) {
                    continue;
                }

                const d = data.slice(0, j + 1).reduce((acc: number, curr: number) => acc + curr, 0);
                const y = originYPos - (d * this.ratio);

                this.ctx.beginPath();
                this.ctx.arc(x, y, r, 0, 2 * Math.PI);
                this.ctx.fillStyle = chartColors[j];
                this.ctx.strokeStyle = 'rgb(0, 0, 0, 0.3)';
                this.ctx.stroke();
                this.ctx.fill();
            }
        }
    }

    private setTooltip(): void {
        if (this.isMouseInChartArea() && this.timeStampList.length >= 2) {
            const originXPos = this.getOriginXPos(0);
            const x = this.lastMousePosInCanvas.coordX;
            const k = this.timeStampList.findIndex((timeStamp: number, i: number) => {
                return originXPos + this.getXPosInChart(i) <= x && originXPos + this.getXPosInChart(i + 1) > x;
            });

            if (k !== -1) {
                this.showTooltip = true;
                this.setTooltipData(k);
                this.drawTooltipPoint(k);
            }
        } else {
            this.showTooltip = false;
        }
    }

    private setTooltipData(i: number): void {
        this.tooltipDataObj = {
            title: moment(this.timeStampList[i]).tz(this.timezone).format(this.dateFormat),
            values: this.dataList[0][i]
        };
    }

    private isMouseInChartArea(): boolean {
        if (!this.lastMousePosInCanvas) {
            return false;
        }

        const { coordX, coordY } = this.lastMousePosInCanvas;
        const { chartHeight, chartWidth } = this.chartOption;
        const originXPos = this.getOriginXPos(0);
        const originYPos = this.getOriginYPos(0);
        const minX = originXPos + 10;
        const maxX = originXPos + chartWidth - 10;
        const minY = originYPos - chartHeight;
        const maxY = originYPos;

        return minX < coordX && coordX < maxX && minY < coordY && coordY < maxY;
    }

    onMouseMove(e: MouseEvent): void {
        const { left, top } = this.canvas.getBoundingClientRect();
        const { clientX, clientY } = e;
        const xPosOnCanvas = clientX - left;
        const yPosOnCanvas = clientY - top;

        this.lastMousePosInCanvas = {
            coordX: xPosOnCanvas,
            coordY: yPosOnCanvas
        };
    }

    onMouseOut(): void {
        this.lastMousePosInCanvas = null;
        this.showTooltip = false;
    }

    onClick(): void {
        const linkIconInfo = this.getLinkIconInfo();

        if (linkIconInfo) {
            this.outClick.emit(linkIconInfo.key);
        }
    }

    private getLinkIconInfo(): { [key: string]: any } {
        const { linkIconCode } = this.chartOption;

        if (!(this.lastMousePosInCanvas && linkIconCode)) {
            return null;
        }

        const { coordX, coordY } = this.lastMousePosInCanvas;

        return this.linkIconInfoList.find((linkIcon: { [key: string]: any }) => {
            const { leftX, rightX, topY, bottomY } = linkIcon;

            return coordX >= leftX && coordX <= rightX && coordY >= topY && coordY <= bottomY;
        });
    }

    getCursorStyle(): string {
        return this.getLinkIconInfo() ? 'pointer' : 'auto';
    }
}
