import { Component, OnInit, ViewChild, ElementRef, Input, Output, AfterViewInit, OnDestroy, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import * as moment from 'moment-timezone';

import { IActiveThreadCounts, ResponseCode } from 'app/core/components/real-time/real-time-websocket.service';
import { getTotalResponseCount, getSuccessDataList } from './new-real-time-util';

@Component({
    selector: 'pp-new-real-time-chart',
    templateUrl: './new-real-time-chart.component.html',
    styleUrls: ['./new-real-time-chart.component.css']
})
export class NewRealTimeChartComponent implements OnInit, AfterViewInit, OnDestroy, OnChanges {
    @ViewChild('realTime') canvasRef: ElementRef;
    @Input() activeThreadCounts: { [key: string]: IActiveThreadCounts };
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Input() timeStamp: number;
    @Input() chartOption: { [key: string]: any };
    @Output() outClick = new EventEmitter<string>();

    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private firstTimeStamp: { [key: string]: number } = {};
    private gridLineStart: number = null;
    private chartStart: { [key: string]: number } = {};
    private animationFrameId: number;
    private timeStampList: { [key: string]: number[] } = {};
    private dataList: { [key: string]: number[][] } = {};
    private maxRatio = 3 / 4; // 차트의 높이에 대해 데이터의 최댓값을 위치시킬 비율
    private ratio: number; // maxRatio를 바탕으로 각 데이터에 적용되는 비율
    private duration = 4000; // 차트 Area에서 데이터가 흐르는 시간(ms)
    private startingXPos: { [key: string]: number } = {}; // 최초로 움직이기 시작하는 점의 x좌표
    private lastMousePosInCanvas: ICoordinate;
    private chartNumPerRow: number;
    private linkIconInfoList: { [key: string]: any }[] = []; // { leftX, rightX, topY, bottomY, agentKey } 를 담은 object의 배열
    private dataSumList: number[][] = [];
    private removedKeys: string[] = [];
    private addedKeys: string[] = [];
    private mergedKeys: string[] = [];

    showTooltip = false;
    tooltipDataObj = {
        title: '',
        values: [] as number[],
    };

    constructor(
        private el: ElementRef
    ) {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        const changesOnATC = changes['activeThreadCounts'];
        const changesOnTimeStamp = changes['timeStamp'];

        if (changesOnATC && changesOnTimeStamp) {
            const { previousValue: prevATC, currentValue: currATC, firstChange } = changesOnATC;
            const { currentValue: timeStamp } = changesOnTimeStamp;
            const prevATCKeys = firstChange ? [] : Object.keys(prevATC);
            const currATCKeys = Object.keys(currATC);

            this.mergedKeys = [...new Set([...prevATCKeys, ...currATCKeys])];
            this.addedKeys = [];
            this.removedKeys = [];

            this.mergedKeys.forEach((key: string) => {
                if (!prevATCKeys.includes(key)) {
                    // 새로 추가된 agent
                    const { status } = currATC[key];
                    const data = status ? status : [];

                    this.dataList[key] = [data];
                    this.timeStampList[key] = [timeStamp];
                    this.firstTimeStamp[key] = timeStamp - 1000;
                    if (!firstChange) {
                        this.addedKeys.push(key);
                    }
                } else if (!currATCKeys.includes(key)) {
                    // 삭제된 agent
                    // 해당 key에 대한 dataList, timeStampList, firstStamp, startingXPos, chartStart 제거
                    delete this.dataList[key];
                    delete this.timeStampList[key];
                    delete this.firstTimeStamp[key];
                    delete this.startingXPos[key];
                    delete this.chartStart[key];
                    this.removedKeys.push(key);
                } else {
                    // 변동없는 agent
                    const { status } = currATC[key];
                    const data = status ? status : [];

                    this.dataList[key].push(data);
                    this.timeStampList[key].push(timeStamp);
                }
            });

            this.dataSumList.push(getTotalResponseCount(getSuccessDataList(currATC)));
            if (this.canvas && (prevATCKeys.length !== currATCKeys.length)) {
                this.setCanvasSize();
            }
        }
    }

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
        const numOfChart = this.mergedKeys.length;

        this.canvas.width = this.el.nativeElement.offsetWidth;
        this.setChartNumPerRow();
        this.canvas.height = this.getTopEdgeYPos(numOfChart - 1) + titleHeight + containerHeight + canvasBottomPadding;
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

    private getOriginXPos(chartIndex: number): number {
        const { chartInnerPadding, yAxisLabelWidth, marginFromYAxis } = this.chartOption;

        return this.getLeftEdgeXPos(chartIndex) + chartInnerPadding + yAxisLabelWidth + marginFromYAxis;
    }

    private getOriginYPos(chartIndex: number): number {
        const { chartInnerPadding, titleHeight, chartHeight } = this.chartOption;

        return this.getTopEdgeYPos(chartIndex) + titleHeight + chartInnerPadding + chartHeight;
    }

    private draw(timeStamp: number): void {
        const { drawHGridLine, drawVGridLine, showXAxis, showYAxis, tooltipEnabled } = this.chartOption;

        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.mergedKeys.forEach((key: string, i: number) => {
            this.drawChartTitle(key, i);
            this.drawChartContainerRect(i);
            if (this.removedKeys.includes(key)) {
                this.drawRemovedText(i);
                return;
            }

            if (drawHGridLine) {
                this.drawHGridLine(i);
            }

            // if (drawVGridLine) {
            //     this.drawVGridLine(timeStamp, i);
            // }

            if (showXAxis) {
                this.drawXAxis(i);
            }

            if (showYAxis) {
                this.drawYAxis(i);
            }

            if (this.timeStampList[key].length !== 0) {
                this.drawChart(timeStamp, key, i);
                if (tooltipEnabled) {
                    this.setTooltip(key);
                }
            }

            this.drawErrorText(key, i);
        });

        this.animationFrameId = requestAnimationFrame((t) => this.draw(t));
    }

    private drawRemovedText(i: number): void {
        const { chartHeight, chartWidth } = this.chartOption;
        const originXPos = this.getOriginXPos(i);
        const originYPos = this.getOriginYPos(i);
        const x = originXPos + chartWidth / 2;
        const y = originYPos - chartHeight / 2;

        this.ctx.fillStyle = 'rgba(232, 229, 240, 0.9)';
        this.ctx.fillRect(originXPos, originYPos - chartHeight, chartWidth, chartHeight);

        this.ctx.font = `600 13px Nanum Gothic`;
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'middle';
        this.ctx.fillStyle = '#c04e3f';
        this.ctx.fillText('Removed', x, y);
    }

    private drawErrorText(key: string, i: number): void {
        const { chartInnerPadding, errorFontSize, titleHeight, chartHeight, chartWidth } = this.chartOption;

        this.ctx.font = `600 ${errorFontSize} Nanum Gothic`;
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'middle';

        const { code, message } = this.activeThreadCounts[key];
        const isResponseSuccess = code === ResponseCode.SUCCESS;

        if (!isResponseSuccess) {
            const originXPos = this.getOriginXPos(i);
            const originYPos = this.getOriginYPos(i);
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

            this.ctx.fillStyle = 'rgba(232, 229, 240, 0.9)';
            this.ctx.fillRect(originXPos, originYPos - chartHeight, chartWidth, chartHeight);

            const length = arrangedText.length;

            arrangedText.forEach((text: string, j: number) => {
                const y = this.getTopEdgeYPos(i) + titleHeight + chartInnerPadding + (j + 1) * chartHeight / (length + 1);

                this.ctx.fillStyle = '#c04e3f';
                this.ctx.fillText(text, x, y);
            });
        }
    }

    private drawChartTitle(key: string, i: number): void {
        const { containerWidth, titleHeight, titleFontSize, marginRightForLinkIcon, linkIconCode } = this.chartOption;
        const isRemovedKey = this.removedKeys.includes(key);
        const isAddedKey = this.addedKeys.includes(key);

        this.ctx.fillStyle = isAddedKey ? '#34b994' : isRemovedKey ? '#e95459' : '#74879a';
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
    }

    private getChartTitleText(text: string): string {
        const { containerWidth, marginRightForLinkIcon, linkIconCode, ellipsis } = this.chartOption;
        const linkIconWidth = linkIconCode ? this.ctx.measureText(linkIconCode).width : 0;
        const ellipsisWidth = this.ctx.measureText(ellipsis).width;
        const textWidth = this.ctx.measureText(text).width;
        const maxWidth = linkIconCode ? containerWidth / 2 - linkIconWidth - marginRightForLinkIcon - 5 : containerWidth / 2;
        const isOverflow = textWidth / 2 > maxWidth;

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

    private drawChartContainerRect(i: number): void {
        const { containerWidth, containerHeight, titleHeight } = this.chartOption;

        this.ctx.fillStyle = '#fff';
        this.ctx.fillRect(this.getLeftEdgeXPos(i), this.getTopEdgeYPos(i) + titleHeight, containerWidth, containerHeight);
    }

    // Horizontal Grid Line
    private drawHGridLine(i: number): void {
        const { chartHeight, chartWidth } = this.chartOption;
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

    // Vertical Grid Line
    // TODO: 데이터(서버의 timestamp) 고려해서 위치 수정하기
    private drawVGridLine(timeStamp: number, i: number): void {
        const { chartWidth, chartHeight } = this.chartOption;

        if (!this.gridLineStart) {
            this.gridLineStart = timeStamp;
        }

        const xPos = this.getOriginXPos(i) + chartWidth - Math.floor(chartWidth / this.duration * (timeStamp - this.gridLineStart)) % chartWidth;
        const originYPos = this.getOriginYPos(i);

        this.ctx.beginPath();
        this.ctx.moveTo(xPos, originYPos - chartHeight);
        this.ctx.lineTo(xPos, originYPos);
        this.ctx.strokeStyle = 'rgba(0, 0, 0, 0.2)';
        this.ctx.stroke();
    }

    private drawXAxis(i: number): void {
        const { chartWidth } = this.chartOption;
        const originXPos = this.getOriginXPos(i);
        const originYPos = this.getOriginYPos(i);

        this.ctx.beginPath();
        this.ctx.moveTo(originXPos, originYPos);
        this.ctx.lineTo(originXPos + chartWidth, originYPos);
        this.ctx.strokeStyle = 'rgba(0, 0, 0, 0.5)';
        this.ctx.stroke();
    }

    private drawYAxis(i: number): void {
        const { chartHeight } = this.chartOption;
        const originXPos = this.getOriginXPos(i);
        const originYPos = this.getOriginYPos(i);

        this.ctx.beginPath();
        this.ctx.moveTo(originXPos, originYPos);
        this.ctx.lineTo(originXPos, originYPos - chartHeight);
        this.ctx.strokeStyle = 'rgba(0, 0, 0, 0.5)';
        this.ctx.stroke();
    }

    private drawYAxisLabel(i: number, max: number): void {
        const { marginFromYAxis, chartHeight } = this.chartOption;
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

    private drawChart(timeStamp: number, key: string, i: number): void {
        if (!this.chartStart[key]) {
            this.chartStart[key] = timeStamp;
        }

        const { chartWidth, chartHeight, chartColors, showYAxisLabel, drawVGridLine } = this.chartOption;
        const dataList = this.dataList[key];

        this.startingXPos[key] = chartWidth - Math.floor(chartWidth / this.duration * (timeStamp - this.chartStart[key]));

        while (this.isChartOverflow(key)) {
            this.timeStampList[key].shift();
            dataList.shift();
            if (this.dataSumList.length > dataList.length) {
                this.dataSumList.shift();
            }
        }

        const x0 = this.getXPosInChart(key, 0);

        if (x0 < chartWidth) {
            const originXPos = this.getOriginXPos(i);
            const originYPos = this.getOriginYPos(i);
            const max = Math.max(...this.dataSumList.map((data: number[]) => data.reduce((acc: number, curr: number) => acc + curr, 0)));
            const dataTypeLength = chartColors.length;
            const dataLength = dataList.length;

            if (showYAxisLabel) {
                this.drawYAxisLabel(i, max);
            }

            if (drawVGridLine) {
                this.timeStampList[key].forEach((t: number, j: number) => {
                    const x = this.getXPosInChart(key, j);

                    if (!(x > 0 && x < chartWidth)) {
                        return;
                    }

                    this.ctx.beginPath();
                    this.ctx.moveTo(originXPos + x, originYPos - chartHeight);
                    this.ctx.lineTo(originXPos + x, originYPos);
                    this.ctx.strokeStyle = 'rgba(0, 0, 0, 0.2)';
                    this.ctx.stroke();
                });
            }

            this.ratio = max === 0 ? 1 : chartHeight * this.maxRatio / max;

            for (let j = dataTypeLength - 1; j >= 0; j--) {
                this.ctx.fillStyle = chartColors[j];
                const data = dataList.map((d: number[]) => {
                    return d.length === 0 ? null : d.slice(0, j + 1).reduce((acc: number, curr: number) => acc + curr, 0);
                }); // ex) [ null, 6, 2, null ]

                this.ctx.beginPath();

                if (data[0] !== null) {
                    if (x0 < 0 && data[1] != null) {
                        // 앞 경계면 처리
                        const x1 = this.getXPosInChart(key, 1);

                        this.ctx.moveTo(originXPos, originYPos); // 시작
                        this.ctx.lineTo(originXPos, originYPos - (data[0] * x1 - data[1] * x0) / (x1 - x0) * this.ratio);
                    } else if (x0 >= 0) {
                        this.ctx.moveTo(originXPos + x0, originYPos); // 시작
                        this.ctx.lineTo(originXPos + x0, originYPos - (data[0] * this.ratio));
                    }
                }

                for (let k = 1; k < dataLength; k++) {
                    const xkm1 = this.getXPosInChart(key, k - 1);
                    const xk = this.getXPosInChart(key, k);

                    if (data[k] === null || (data[k - 1] === null && xk > chartWidth)) {
                        continue;
                    }

                    if (data[k - 1] === null) {
                        this.ctx.beginPath();
                        this.ctx.moveTo(originXPos + xk, originYPos);
                    }

                    if (xkm1 <= chartWidth && xk > chartWidth) {
                        this.ctx.lineTo(originXPos + chartWidth, originYPos - (data[k - 1] + (chartWidth - xkm1) * (data[k] - data[k - 1]) / (xk - xkm1)) * this.ratio);
                        this.ctx.lineTo(originXPos + chartWidth, originYPos);
                        this.ctx.fill();
                        break;
                    } else {
                        this.ctx.lineTo(originXPos + xk, originYPos - (data[k] * this.ratio));
                        if (data[k + 1] == null) {
                            this.ctx.lineTo(originXPos + xk, originYPos);
                            this.ctx.fill();
                            if (data[k + 1] === undefined && xk <= chartWidth * 0.95) {
                                // When it's delayed
                                const x = originXPos + 5 / 6 * chartWidth;
                                const y = originYPos - 1 / 6 * chartHeight;

                                this.ctx.font = '9px Nanum Gothic';
                                this.ctx.fillStyle = '#c04e3f';
                                this.ctx.textAlign = 'center';
                                this.ctx.textBaseline = 'middle';
                                this.ctx.fillText('Delayed', x, y);
                            }
                        }
                    }
                }
            }
        }
    }

    private getXPosInChart(key: string, i: number): number {
        const { chartWidth } = this.chartOption;

        return this.startingXPos[key] + Math.floor(chartWidth / this.duration * (this.timeStampList[key][i] - this.firstTimeStamp[key]));
    }

    private isChartOverflow(key: string): boolean {
        return this.timeStampList[key].length >= 2 && this.getXPosInChart(key, 1) < 0;
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

    private drawTooltipPoint(key: string, i: number): void {
        const { chartColors } = this.chartOption;
        const originXPos = this.getOriginXPos(0);
        const originYPos = this.getOriginYPos(0);
        const data = this.dataList[key][i]; // [0, 1, 2, 3]
        const length = data.length;
        const x = originXPos + this.getXPosInChart(key, i);
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

    private setTooltip(key: string): void {
        if (this.isMouseInChartArea() && this.timeStampList[key].length >= 2) {
            const originXPos = this.getOriginXPos(0);
            const x = this.lastMousePosInCanvas.coordX;
            const k = this.timeStampList[key].findIndex((timeStamp: number, i: number) => {
                return originXPos + this.getXPosInChart(key, i) <= x && originXPos + this.getXPosInChart(key, i + 1) > x;
            });
            const isDataEmpty = (i: number) => this.dataList[key][i].length === 0;

            if (!(k === -1 || isDataEmpty(k))) {
                this.showTooltip = true;
                this.setTooltipData(key, k);
                this.drawTooltipPoint(key, k);
            }
        } else {
            this.showTooltip = false;
        }
    }

    private setTooltipData(key: string, i: number): void {
        this.tooltipDataObj = {
            title: moment(this.timeStampList[key][i]).tz(this.timezone).format(this.dateFormat),
            values: this.dataList[key][i]
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
