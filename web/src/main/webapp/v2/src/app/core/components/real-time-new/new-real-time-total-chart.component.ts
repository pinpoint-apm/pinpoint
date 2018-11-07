import { Component, OnInit, Input, AfterViewInit, ViewChild, ElementRef, OnDestroy, Renderer2 } from '@angular/core';
import * as moment from 'moment-timezone';

@Component({
    selector: 'pp-new-real-time-total-chart',
    templateUrl: './new-real-time-total-chart.component.html',
    styleUrls: ['./new-real-time-total-chart.component.css']
})
export class NewRealTimeTotalChartComponent implements OnInit, AfterViewInit, OnDestroy {
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Input() hasError: boolean;
    @Input() errorMessage: string;
    @Input() applicationName: string;
    @Input()
    set data(data: number[]) {
        const isEmpty = data.length === 0;

        if (!isEmpty) {
            this._dataList = this._dataList.map((_data: number[], i: number) => {
                return [ ..._data, data[i] ];
            });
        }

        this._data = data;
        this.totalCount = isEmpty ? null : data.reduce((acc: number, curr: number) => {
            return acc + curr;
        }, 0);
    }

    get data(): number[] {
        return this._data;
    }

    @Input()
    set timeStamp(timeStamp: number) {
        this._timeStampList.push(timeStamp);
        if (this._timeStampList.length === 1) {
            this.firstTimeStamp = this._timeStampList[0] - 2000;
        }
    }
    @ViewChild('canvas') canvasRef: ElementRef;

    firstTimeStamp: number;
    _timeStampList: number[] = [];
    _data: number[] = [];
    _dataList: number[][] = [ [], [], [], [] ] ;

    canvas: HTMLCanvasElement;
    ctx: CanvasRenderingContext2D;
    totalCount: number;

    chartConstant = {
        canvasLeftPadding: 0,
        canvasTopPadding: 0,
        canvasRightPadding: 0,
        canvasBottomPadding: 0,
        chartInnerPadding: 15,
        yAxisWidth: 8,
        marginFromYAxis: 5,
        marginFromLegend: 10,
        containerWidth: 277,
        containerHeight: 132,
        chartWidth: 159,
        chartHeight: 102,
        titleHeight: 46,
        chartColors: ['#33b692', '#51afdf', '#fea63e', '#e76f4b'],
        chartLabels: ['1s', '3s', '5s', 'Slow'],
        chartSpeedControl: 20,
        ellipsis: '...',
    };
    chartStart: number = null;
    animationFrameId: number;
    showTooltip = false;
    tooltipDataObj = {
        title: '',
        values: [] as number[],
    };
    lastMousePosInCanvas: ICoordinate;
    maxRatio = 3 / 5; // 차트의 높이에 대해 데이터의 최댓값을 위치시킬 비율
    ratio: number; // maxRatio를 바탕으로 각 데이터에 적용되는 비율
    startingXPos: number; // 최초로 움직이기 시작하는 점의 x좌표

    constructor(
        private el: ElementRef,
    ) {}
    ngOnInit() {}
    ngAfterViewInit() {
        this.canvas = this.canvasRef.nativeElement;
        this.ctx = this.canvas.getContext('2d');

        this.canvas.width = this.el.nativeElement.offsetWidth;
        this.canvas.height = this.chartConstant.titleHeight + this.chartConstant.containerHeight;

        this.animationFrameId = requestAnimationFrame((t) => this.draw(t));
    }

    ngOnDestroy() {
        cancelAnimationFrame(this.animationFrameId);
    }

    draw(timestamp: number): void {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.drawChartTitle();
        this.drawChartContainerRect();
        this.drawHGridLine();
        this.drawXAxis();

        if (this.hasError) {
            this.drawErrorText();
        } else {
            if (this._timeStampList.length !== 0) {
                this.drawChart(timestamp);
                this.setTooltip();
            }
        }

        this.animationFrameId = requestAnimationFrame((t) => this.draw(t));
    }

    getLegendStyle(legend: HTMLElement): { [key: string]: string } {
        const { containerWidth, chartInnerPadding, titleHeight } = this.chartConstant;
        const legendWidth = legend.offsetWidth;

        return {
            left: `${this.getLeftEdgeXPos() + containerWidth - chartInnerPadding - legendWidth}px`,
            top: `${this.getTopEdgeYPos() + titleHeight + chartInnerPadding - 12}px`
        };
    }

    getLeftEdgeXPos(): number {
        // 차트 컨테이너 왼쪽 모서리 x좌표를 리턴
        // TODO: AgentChart쪽이랑 같은식으로 돌려도될듯 i = 0으로.
        return this.chartConstant.canvasLeftPadding;
    }

    getTopEdgeYPos(): number {
        // 차트 컨테이너 위쪽 모서리 y좌표를 리턴
        // TODO: AgentChart쪽이랑 같은식으로 돌려도될듯 i = 0으로.
        return this.chartConstant.canvasTopPadding;
    }

    drawErrorText(): void {
        this.ctx.font = '600 15px Nanum Gothic';
        this.ctx.fillStyle = '#c04e3f';
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'middle';

        const { chartInnerPadding, yAxisWidth, marginFromYAxis, titleHeight, chartHeight, chartWidth } = this.chartConstant;
        const x = this.getLeftEdgeXPos() + chartInnerPadding + yAxisWidth + marginFromYAxis + chartWidth / 2;
        const isOverflow = (str: string) => this.ctx.measureText(str).width > chartWidth - 10;

        const words = this.errorMessage.split(' ');
        const arrangedText: string[] = []; // 여기에 message를 루프돌면서 overflow하지않는 단위로 잘라서 넣어줄 예정. 그리고, 이걸로 마지막에 fillText()
        let startIndex = 0;
        let lastIndex = words.length - 1;

        while (this.errorMessage !== arrangedText.join(' ')) {
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
            const y = this.getTopEdgeYPos() + titleHeight + chartInnerPadding + (j + 1) * chartHeight / (length + 1);

            this.ctx.fillText(text, x, y);
        });
    }

    drawChartTitle(): void {
        const { containerWidth, titleHeight } = this.chartConstant;

        this.ctx.fillStyle = '#74879a';
        this.ctx.fillRect(this.getLeftEdgeXPos(), this.getTopEdgeYPos(), containerWidth, titleHeight);

        this.ctx.font = '15px Nanum Gothic';
        this.ctx.fillStyle = '#fff';
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'middle';
        this.ctx.fillText(this.getChartTitleText(this.applicationName), this.getLeftEdgeXPos() + containerWidth / 2, this.getTopEdgeYPos() + titleHeight / 2);
    }

    getChartTitleText(text: string): string {
        const { containerWidth, ellipsis } = this.chartConstant;
        const ellipsisWidth = this.ctx.measureText(ellipsis).width;
        const textWidth = this.ctx.measureText(text).width;
        const maxWidth = containerWidth / 2;
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

    drawChartContainerRect(): void {
        const { containerWidth, containerHeight, titleHeight } = this.chartConstant;

        this.ctx.fillStyle = '#e8e5f0';
        this.ctx.fillRect(this.getLeftEdgeXPos(), this.getTopEdgeYPos() + titleHeight, containerWidth, containerHeight);
    }

    // Horizontal Grid Line
    drawHGridLine(): void {
        const { chartInnerPadding, yAxisWidth, marginFromYAxis, titleHeight, chartHeight, chartWidth } = this.chartConstant;
        const xPos = this.getLeftEdgeXPos() + chartInnerPadding + yAxisWidth + marginFromYAxis; // Grid Line 시작 x좌표
        const yPos = this.getTopEdgeYPos();

        // Horizontal grid line 1 (top)
        this.ctx.beginPath();
        this.ctx.moveTo(xPos, yPos + titleHeight + chartInnerPadding);
        this.ctx.lineTo(xPos + chartWidth, yPos + titleHeight + chartInnerPadding);
        this.ctx.strokeStyle = 'rgba(0, 0, 0, 0.1)';
        this.ctx.stroke();

        // Horizontal grid line 2 (middle)
        this.ctx.beginPath();
        this.ctx.moveTo(xPos, yPos + titleHeight + chartInnerPadding + chartHeight / 2);
        this.ctx.lineTo(xPos + chartWidth, yPos + titleHeight + chartInnerPadding + chartHeight / 2);
        this.ctx.strokeStyle = 'rgba(0, 0, 0, 0.1)';
        this.ctx.stroke();
    }

    drawYAxisLabel(max: number): void {
        const { chartInnerPadding, yAxisWidth, titleHeight, chartHeight } = this.chartConstant;
        const xPos = this.getLeftEdgeXPos() + chartInnerPadding + yAxisWidth;
        const yPos = this.getTopEdgeYPos();
        const yAxisFlipValue = yPos + titleHeight + chartInnerPadding + chartHeight;
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
        this.ctx.fillText(`0`, xPos, yAxisFlipValue);
        if (maxLabel) {
            this.ctx.fillText(`${midLabel}`, xPos, yPos + titleHeight + chartInnerPadding + chartHeight / 2);
            this.ctx.fillText(`${maxLabel}`, xPos, yPos + titleHeight + chartInnerPadding);
        }
    }

    drawXAxis(): void {
        const { chartInnerPadding, yAxisWidth, marginFromYAxis, titleHeight, chartHeight, chartWidth } = this.chartConstant;
        const xPos = this.getLeftEdgeXPos() + chartInnerPadding + yAxisWidth + marginFromYAxis;
        const yPos = this.getTopEdgeYPos();
        const yAxisFlipValue = yPos + titleHeight + chartInnerPadding + chartHeight;

        this.ctx.beginPath();
        this.ctx.moveTo(xPos, yAxisFlipValue);
        this.ctx.lineTo(xPos + chartWidth, yAxisFlipValue);
        this.ctx.strokeStyle = 'rgba(0, 0, 0, 0.1)';
        this.ctx.stroke();
    }

    drawChart(timestamp: number): void {
        if (!this.chartStart) {
            this.chartStart = timestamp;
        }

        const { chartInnerPadding, chartWidth, yAxisWidth, titleHeight, chartHeight, marginFromYAxis, chartColors, chartSpeedControl } = this.chartConstant;
        this.startingXPos = chartWidth - Math.floor((timestamp - this.chartStart) / chartSpeedControl);
        const isOverflow = this._timeStampList.length >= 2 && this.getXPosInChart(1) < 0;

        if (isOverflow) {
            this._timeStampList.shift();
        }

        const xPos0 = this.getXPosInChart(0);

        if (xPos0 < chartWidth) {
            const dataList = this._dataList;

            if (isOverflow) {
                dataList.forEach((dataArr: number[]) => {
                    dataArr.shift();
                });
            }

            const originXPos = this.getLeftEdgeXPos() + chartInnerPadding + yAxisWidth + marginFromYAxis;
            const yAxisFlipValue = this.getTopEdgeYPos() + titleHeight + chartInnerPadding + chartHeight;
            const max = Math.max(...dataList.map((data: number[]) => Math.max(...data)));
            const length = dataList.length;

            this.drawYAxisLabel(max);
            this.ratio = max === 0 ? 1 : chartHeight * this.maxRatio / max;

            for (let j = 0; j < length; j++) {
                const data = dataList[j];

                this.ctx.beginPath();

                if (xPos0 < 0) {
                    // 앞 경계면 처리
                    const xPos1 = this.getXPosInChart(1);

                    this.ctx.moveTo(originXPos, yAxisFlipValue); // 시작
                    this.ctx.lineTo(originXPos, yAxisFlipValue - (data[0] * xPos1 - data[1] * xPos0) / (xPos1 - xPos0) * this.ratio);
                } else {
                    this.ctx.moveTo(originXPos + this.getXPosInChart(0), yAxisFlipValue); // 시작
                    this.ctx.lineTo(originXPos + this.getXPosInChart(0), yAxisFlipValue - (data[0] * this.ratio));
                }

                const timeStampLength = this._timeStampList.length;

                for (let k = 1; k < timeStampLength; k++) {
                    const poskm1 = this.getXPosInChart(k - 1);
                    const posk = this.getXPosInChart(k);

                    if (poskm1 <= chartWidth && posk > chartWidth) {
                        // 뒷 경계면 처리
                        this.ctx.lineTo(originXPos + chartWidth, yAxisFlipValue - (data[k - 1] + (chartWidth - poskm1) * (data[k] - data[k - 1]) / (posk - poskm1)) * this.ratio);
                        break;
                    } else {
                        this.ctx.lineTo(originXPos + this.getXPosInChart(k), yAxisFlipValue - (data[k] * this.ratio));
                    }
                }

                this.ctx.lineTo(originXPos + chartWidth, yAxisFlipValue); // 마지막

                this.ctx.fillStyle = chartColors[j];
                this.ctx.fill();
            }
        }
    }

    getXPosInChart(i: number): number {
        const { chartSpeedControl } = this.chartConstant;

        return this.startingXPos + Math.floor((this._timeStampList[i] - this.firstTimeStamp) / chartSpeedControl);
    }

    calculateTooltipLeft(tooltip: HTMLElement): string {
        const { coordX } = this.lastMousePosInCanvas;
        const { chartWidth, chartInnerPadding, yAxisWidth, marginFromYAxis } = this.chartConstant;
        const originXPos = this.getLeftEdgeXPos() + chartInnerPadding + yAxisWidth + marginFromYAxis;
        const tooltipWidth = tooltip.offsetWidth;
        const ratio = (coordX - originXPos) / chartWidth;

        return `${coordX - (tooltipWidth * ratio)}px`;
    }

    calculateTooltipCaretLeft(tooltipCaret: HTMLElement): string {
        const { coordX } = this.lastMousePosInCanvas;

        return `${coordX - (tooltipCaret.offsetWidth / 2)}px`;
    }

    drawTooltipPoint(i: number): void {
        const { chartInnerPadding, titleHeight, chartHeight, chartColors, yAxisWidth, marginFromYAxis } = this.chartConstant;
        const yAxisFlipValue = this.getTopEdgeYPos() + titleHeight + chartInnerPadding + chartHeight;
        const yPosList = this._dataList.map((data: number[]) => yAxisFlipValue - (data[i] * this.ratio));
        const originXPos = this.getLeftEdgeXPos() + chartInnerPadding + yAxisWidth + marginFromYAxis;
        const x = originXPos + this.getXPosInChart(i);
        const r = 3;

        if (x > originXPos) {
            yPosList.forEach((y: number, index: number) => {
                this.ctx.beginPath();
                this.ctx.arc(x, y, r, 0, 2 * Math.PI);
                this.ctx.fillStyle = chartColors[index];
                this.ctx.fill();
            });
        }
    }

    setTooltip(): void {
        if (this.isMouseInChartArea() && this._timeStampList.length >= 2) {
            const { chartInnerPadding, yAxisWidth, marginFromYAxis } = this.chartConstant;
            const originXPos = this.getLeftEdgeXPos() + chartInnerPadding + yAxisWidth + marginFromYAxis;
            const x = this.lastMousePosInCanvas.coordX;
            const k = this._timeStampList.findIndex((timeStamp: number, i: number) => {
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

    setTooltipData(i: number): void {
        this.tooltipDataObj = {
            title: moment(this._timeStampList[i]).tz(this.timezone).format(this.dateFormat),
            values: this._dataList.map((data: number[]) => data[i])
        };
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

    isMouseInChartArea(): boolean {
        if (!this.lastMousePosInCanvas) {
            return false;
        }

        const { coordX, coordY } = this.lastMousePosInCanvas;
        const { chartInnerPadding, yAxisWidth, marginFromYAxis, titleHeight, chartHeight, chartWidth } = this.chartConstant;
        const minX = this.getLeftEdgeXPos() + chartInnerPadding + yAxisWidth + marginFromYAxis + 10;
        const maxX = this.getLeftEdgeXPos() + chartInnerPadding + yAxisWidth + marginFromYAxis + chartWidth - 10;
        const minY = this.getTopEdgeYPos() + titleHeight + chartInnerPadding;
        const maxY = minY + chartHeight;

        return minX < coordX && coordX < maxX && minY < coordY && coordY < maxY;
    }
}
