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
        this.totalCount = isEmpty ? null : data.reduce((acc: number, curr: number) => {
            return acc + curr;
        }, 0);
        this._data = data;
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
    @ViewChild('legend') legendRef: ElementRef;

    firstTimeStamp: number;
    _timeStampList: number[] = [];
    _data: number[] = [];
    _dataList: number[][] = [ [], [], [], []] ;

    canvas: HTMLCanvasElement;
    ctx: CanvasRenderingContext2D;
    totalCount: number;

    chartConstant = {
        canvasLeftPadding: 0,
        canvasTopPadding: 0,
        canvasRightPadding: 0,
        canvasBottomPadding: 0,
        chartInnerPadding: 15,
        axisWidth: 8,
        marginFromAxis: 5,
        marginFromLegend: 10,
        containerWidth: 277,
        containerHeight: 132,
        chartWidth: 0,
        chartHeight: 0,
        titleHeight: 46,
        chartColors: ['#33b692', '#51afdf', '#fea63e', '#e76f4b'],
        chartLabels: ['1s', '3s', '5s', 'Slow'],
        gridLineSpeedControl: 25,
        chartSpeedControl: 25,
        ellipsis: '...',
        ellipsisWidth: 0,
        legendWidth: 0,
    };
    chartStart: number = null;
    animationFrameId: number;
    showTooltip = false;
    tooltipDataObj = {
        title: '',
        values: [] as number[],
    };
    lastMousePosInCanvas: ICoordinate;
    yRatio = 3 / 5; // 차트의 높이에 대한 데이터의 max 비율

    constructor(
        private el: ElementRef,
        private renderer: Renderer2
    ) {}
    ngOnInit() {}
    ngAfterViewInit() {
        this.canvas = this.canvasRef.nativeElement;
        this.ctx = this.canvas.getContext('2d');

        this.initConstant();
        this.setLegendPosition();
        this.canvas.width = this.el.nativeElement.offsetWidth;
        this.canvas.height = this.chartConstant.titleHeight + this.chartConstant.containerHeight;

        this.animationFrameId = requestAnimationFrame((t) => this.draw(t));
    }

    ngOnDestroy() {
        cancelAnimationFrame(this.animationFrameId);
    }

    initConstant(): void {
        const { containerWidth, containerHeight, chartInnerPadding, axisWidth, ellipsis, marginFromAxis, marginFromLegend } = this.chartConstant;
        this.chartConstant.ellipsisWidth = this.ctx.measureText(ellipsis).width;
        this.chartConstant.legendWidth = this.legendRef.nativeElement.offsetWidth;
        this.chartConstant.chartWidth = containerWidth - 2 * chartInnerPadding - axisWidth - marginFromAxis - marginFromLegend - this.chartConstant.legendWidth;
        this.chartConstant.chartHeight = containerHeight - 2 * chartInnerPadding;
    }

    draw(timestamp: number): void {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.drawChartTitle();
        this.drawChartContainerRect();
        this.drawGridLine();
        this.drawXAxis();
        if (this.hasError) {
            this.drawErrorText();
        } else {
            // TODO: _dataList.length !== 0일때로 해야하나?
            if (this._timeStampList.length !== 0) {
                this.drawChart(timestamp);
            }

        }
        this.animationFrameId = requestAnimationFrame((t) => this.draw(t));
    }

    setLegendPosition(): void {
        const { containerWidth, chartInnerPadding, titleHeight, legendWidth } = this.chartConstant;

        this.renderer.setStyle(this.legendRef.nativeElement, 'left', `${this.getXPos() + containerWidth - chartInnerPadding - legendWidth}px`);
        this.renderer.setStyle(this.legendRef.nativeElement, 'top', `${this.getYPos() + titleHeight + chartInnerPadding - 12}px`);
    }

    getXPos(): number {
        // 차트 컨테이너 왼쪽 위 꼭짓점 x좌표를 리턴
        return this.chartConstant.canvasLeftPadding;
    }

    getYPos(): number {
        // 차트 컨테이너 왼쪽 위 꼭짓점 y좌표를 리턴
        return this.chartConstant.canvasTopPadding;
    }

    drawErrorText(): void {
        this.ctx.font = '600 15px Nanum Gothic';
        this.ctx.fillStyle = '#c04e3f';
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'middle';

        const { chartInnerPadding, axisWidth, marginFromAxis, titleHeight, chartHeight, chartWidth } = this.chartConstant;
        const x = this.getXPos() + chartInnerPadding + axisWidth + marginFromAxis + chartWidth / 2;
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
            const y = this.getYPos() + titleHeight + chartInnerPadding + (j + 1) * chartHeight / (length + 1);

            this.ctx.fillText(text, x, y);
        });
    }

    drawChartTitle(): void {
        const { containerWidth, titleHeight } = this.chartConstant;

        this.ctx.fillStyle = '#74879a';
        this.ctx.fillRect(this.getXPos(), this.getYPos(), containerWidth, titleHeight);

        this.ctx.font = '15px Nanum Gothic';
        this.ctx.fillStyle = '#fff';
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'middle';
        this.ctx.fillText(this.getChartTitleText(this.applicationName), this.getXPos() + containerWidth / 2, this.getYPos() + titleHeight / 2);
    }

    getChartTitleText(text: string): string {
        const { containerWidth, ellipsis, ellipsisWidth } = this.chartConstant;
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
        this.ctx.fillRect(this.getXPos(), this.getYPos() + titleHeight, containerWidth, containerHeight);
    }

    // Horizontal Grid Line
    drawGridLine(): void {
        const { chartInnerPadding, axisWidth, marginFromAxis, titleHeight, chartHeight, chartWidth } = this.chartConstant;
        const xPos = this.getXPos() + chartInnerPadding + axisWidth + marginFromAxis; // Grid Line 시작 x좌표
        const yPos = this.getYPos();

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
        const { chartInnerPadding, axisWidth, titleHeight, chartHeight } = this.chartConstant;
        const xPos = this.getXPos() + chartInnerPadding + axisWidth;
        const yPos = this.getYPos();
        const yAxisFlipValue = yPos + titleHeight + chartInnerPadding + chartHeight;
        let maxLabel = max === 0 ? null : max / this.yRatio;
        let midLabel = max === 0 ? null : maxLabel / 2;

        if (maxLabel && !Number.isInteger(midLabel)) {
            midLabel = Math.round(midLabel);
            maxLabel = 2 * midLabel;
            this.yRatio = max / maxLabel;
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
        const { chartInnerPadding, axisWidth, marginFromAxis, titleHeight, chartHeight, chartWidth } = this.chartConstant;
        const xPos = this.getXPos() + chartInnerPadding + axisWidth + marginFromAxis;
        const yPos = this.getYPos();
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

        const { chartInnerPadding, chartWidth, axisWidth, titleHeight, chartHeight, marginFromAxis, chartColors, chartSpeedControl } = this.chartConstant;
        const startingXPos = chartWidth - Math.floor((timestamp - this.chartStart) / chartSpeedControl); // 최초의 시작하는 점의 x좌표
        const isOverflow = this._timeStampList.length >= 2 && startingXPos + Math.floor((this._timeStampList[1] - this.firstTimeStamp) / chartSpeedControl) < 0;

        if (isOverflow) {
            this._timeStampList.shift();
        }

        const xPos0 = startingXPos + Math.floor((this._timeStampList[0] - this.firstTimeStamp) / chartSpeedControl);

        if (xPos0 < chartWidth) {
            const dataList = this._dataList;

            if (isOverflow) {
                dataList.forEach((dataArr: number[]) => {
                    dataArr.shift();
                });
            }

            const originXPos = this.getXPos() + chartInnerPadding + axisWidth + marginFromAxis;
            const xPos = originXPos + startingXPos; // 차트에서의 기준점(t0) x좌표
            const yPos = this.getYPos(); // 왼쪽 위 꼭짓점 y좌표
            const yAxisFlipValue = yPos + titleHeight + chartInnerPadding + chartHeight;

            const length = dataList.length;
            const max = Math.max(...dataList.map((data: number[]) => Math.max(...data)));
            this.drawYAxisLabel(max);
            const contentRatio = max === 0 ? 1 : chartHeight * this.yRatio / max;

            for (let j = 0; j < length; j++) {
                const data = dataList[j];

                this.ctx.beginPath();

                if (xPos0 < 0) {
                    // 앞 경계면 처리
                    const xPos1 = startingXPos + Math.floor((this._timeStampList[1] - this.firstTimeStamp) / chartSpeedControl);

                    this.ctx.moveTo(originXPos, yAxisFlipValue); // 시작
                    this.ctx.lineTo(originXPos, yAxisFlipValue - (data[0] * xPos1 - data[1] * xPos0) / (xPos1 - xPos0) * contentRatio);
                } else {
                    this.ctx.moveTo(xPos + Math.floor((this._timeStampList[0] - this.firstTimeStamp) / chartSpeedControl), yAxisFlipValue); // 시작
                    this.ctx.lineTo(xPos + Math.floor((this._timeStampList[0] - this.firstTimeStamp) / chartSpeedControl), yAxisFlipValue - (data[0] * contentRatio));
                }

                const timeStampLength = this._timeStampList.length;

                for (let k = 1; k < timeStampLength; k++) {
                    const poskm1 = startingXPos + Math.floor((this._timeStampList[k - 1] - this.firstTimeStamp) / chartSpeedControl);
                    const posk = startingXPos + Math.floor((this._timeStampList[k] - this.firstTimeStamp) / chartSpeedControl);

                    if (poskm1 <= chartWidth && posk > chartWidth) {
                        // 뒷 경계면 처리
                        this.ctx.lineTo(originXPos + chartWidth, yAxisFlipValue - (data[k - 1] + (chartWidth - poskm1) * (data[k] - data[k - 1]) / (posk - poskm1)) * contentRatio);
                        break;
                    } else {
                        this.ctx.lineTo(xPos + Math.floor((this._timeStampList[k] - this.firstTimeStamp) / chartSpeedControl), yAxisFlipValue - (data[k] * contentRatio));
                    }
                }

                this.ctx.lineTo(originXPos + chartWidth, yAxisFlipValue); // 마지막

                // this.ctx.strokeStyle = this.chartConstant.chartColors[j];
                this.ctx.fillStyle = chartColors[j];
                // this.ctx.stroke();
                this.ctx.fill();
            }

            if (this.lastMousePosInCanvas && this.isMouseInChartArea() && this._timeStampList.length >= 2) {
                const x = this.lastMousePosInCanvas.coordX;
                const k = this._timeStampList.findIndex((timeStamp: number, i: number) => {
                    const func = (index: number): number => xPos + Math.floor((this._timeStampList[index] - this.firstTimeStamp) / chartSpeedControl);

                    return func(i) <= x && func(i + 1) > x;
                });

                if (k !== -1) {
                    this.showTooltip = true;
                    this.setTooltipData(k);
                }
            } else {
                this.showTooltip = false;
            }
        }
    }

    calculateTooltipLeft(tooltip: HTMLElement): string {
        const { coordX } = this.lastMousePosInCanvas;
        const { chartWidth, chartInnerPadding, axisWidth, marginFromAxis } = this.chartConstant;
        const originXPos = this.getXPos() + chartInnerPadding + axisWidth + marginFromAxis;
        const tooltipWidth = tooltip.offsetWidth;
        const ratio = (coordX - originXPos) / chartWidth;

        return `${coordX - (tooltipWidth * ratio)}px`;
    }

    calculateTooltipCaretLeft(tooltipCaret: HTMLElement): string {
        const { coordX } = this.lastMousePosInCanvas;

        return `${coordX - (tooltipCaret.offsetWidth / 2)}px`;
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
        const { coordX, coordY } = this.lastMousePosInCanvas;
        const { chartInnerPadding, axisWidth, marginFromAxis, titleHeight, chartHeight, chartWidth } = this.chartConstant;
        const minX = this.getXPos() + chartInnerPadding + axisWidth + marginFromAxis + 10;
        const maxX = this.getXPos() + chartInnerPadding + axisWidth + marginFromAxis + chartWidth - 10;
        const minY = this.getYPos() + titleHeight + chartInnerPadding;
        const maxY = minY + chartHeight;

        return minX < coordX && coordX < maxX && minY < coordY && coordY < maxY;
    }
}
