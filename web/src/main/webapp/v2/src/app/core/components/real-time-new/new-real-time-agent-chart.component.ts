import { Component, OnInit, OnDestroy, Input, Output, EventEmitter, AfterViewInit, ViewChild, ElementRef, Renderer2 } from '@angular/core';

import { IActiveThreadCounts, ResponseCode } from 'app/core/components/real-time-new/new-real-time-websocket.service';

@Component({
    selector: 'pp-new-real-time-agent-chart',
    templateUrl: './new-real-time-agent-chart.component.html',
    styleUrls: ['./new-real-time-agent-chart.component.css']
})
export class NewRealTimeAgentChartComponent implements OnInit, AfterViewInit, OnDestroy {
    @Input()
    set activeThreadCounts(activeThreadCounts: { [key: string]: IActiveThreadCounts }) {
        this.numOfAgent = Object.keys(activeThreadCounts).length;
        this._activeThreadCounts = activeThreadCounts;
        // this._dataList = Object.keys(activeThreadCounts).reduce((acc: { [key: string]: number[][] }, curr: string) => {
        //     // TODO: agentName가 key로 왜 필요할까?
        //     const status = activeThreadCounts[curr].status;
        //     const dataList = status ?
        //     (this._dataList[curr] ? this._dataList[curr].map((data: number[], i: number) => [ ...data, status[i] ]) : status.map((v: number) => [v])) :
        //     [[], [], [], []];

        //     return { ...acc, [curr]: dataList };
        // }, {});
        this._dataList = Object.keys(activeThreadCounts).reduce((acc: { [key: string]: number[][] }, curr: string) => {
            // TODO: agentName가 key로 왜 필요할까? A) 꼭 필요한건아닌데, 일단은 key-value로 묶어논거
            const status = activeThreadCounts[curr].status;
            const dataList = status ?
            (this._dataList[curr] ? this._dataList[curr].map((data: number[], i: number) => [ ...data, status[i] ]) : status.map((v: number) => [v])) :
            [[], [], [], []];

            return { ...acc, [curr]: dataList };
        }, {});
    }
    @Input()
    set timeStamp(timeStamp: number) {
        this._timeStampList.push(timeStamp);
        if (this._timeStampList.length === 1) {
            this.firstTimeStamp = this._timeStampList[0] - 2000;
        }
    }
    @Output() outOpenThreadDump: EventEmitter<string> = new EventEmitter();
    @ViewChild('canvas') canvasRef: ElementRef;

    numOfAgent: number;
    firstTimeStamp: number;
    _activeThreadCounts: { [key: string]: IActiveThreadCounts };
    _timeStampList: number[] = [];
    _dataList: { [key: string]: number[][] } = {};

    chartConstant = {
        canvasLeftPadding: 0,
        canvasTopPadding: 0,
        canvasRightPadding: 0,
        canvasBottomPadding: 0,
        containerWidth: 152,
        containerHeight: 52,
        chartWidth: 152,
        chartHeight: 52,
        titleHeight: 32,
        gapBtnChart: 10,
        chartColors: ['#33b692', '#51afdf', '#fea63e', '#e76f4b'],
        gridLineSpeedControl: 25,
        chartSpeedControl: 25,
        linkIconCode: '\uf35d',
        marginRightForLinkIcon: 10,
        ellipsis: '...',
    };
    canvas: HTMLCanvasElement;
    ctx: CanvasRenderingContext2D;
    chartNumPerRow: number;
    linkIconInfoList: { [key: string]: any }[] = []; // { leftX, rightX, topY, bottomY, agentKey } 를 담은 object의 배열
    gridLineStart: number = null;
    chartStart: number = null;
    animationFrameId: number;
    maxRatio = 3 / 5; // 차트의 높이에 대해 데이터의 최댓값을 위치시킬 비율
    ratio: number; // maxRatio를 바탕으로 각 데이터에 적용되는 비율
    startingXPos: number; // 최초로 움직이기 시작하는 점의 x좌표

    constructor(
        private el: ElementRef,
        private renderer: Renderer2
    ) {}
    ngOnInit() {}
    ngAfterViewInit() {
        this.canvas = this.canvasRef.nativeElement;
        this.ctx = this.canvas.getContext('2d');

        this.resize();
        this.addEventListener();

        this.animationFrameId = requestAnimationFrame((t) => this.draw(t));
    }

    ngOnDestroy() {
        cancelAnimationFrame(this.animationFrameId);
    }

    draw(timestamp: number): void {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.drawChartTitle();
        this.drawChartContainerRect();
        this.drawVGridLine(timestamp);

        if (this._timeStampList.length !== 0) {
            this.drawChart(timestamp);
        }

        this.drawErrorText();
        this.animationFrameId = requestAnimationFrame((t) => this.draw(t));
    }

    getLeftEdgeXPos(i: number): number {
        // 차트 컨테이너 왼쪽 위 꼭짓점 x좌표를 리턴
        return this.chartConstant.canvasLeftPadding + (this.chartConstant.chartWidth + this.chartConstant.gapBtnChart) * (i % this.chartNumPerRow);
    }

    getTopEdgeYPos(i: number): number {
        // 차트 컨테이너 왼쪽 위 꼭짓점 y좌표를 리턴
        return this.chartConstant.canvasTopPadding + (this.chartConstant.chartHeight + this.chartConstant.titleHeight + this.chartConstant.gapBtnChart) * Math.floor(i / this.chartNumPerRow);
    }

    drawErrorText(): void {
        this.ctx.font = '600 13px Nanum Gothic';
        this.ctx.fillStyle = '#c04e3f';
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'middle';

        const { titleHeight, chartWidth, chartHeight } = this.chartConstant;

        Object.keys(this._activeThreadCounts).forEach((agentName: string, i: number) => {
            const { code, message } = this._activeThreadCounts[agentName];
            const isResponseSuccess = code === ResponseCode.SUCCESS;

            if (!isResponseSuccess) {
                const x = this.getLeftEdgeXPos(i) + chartWidth / 2;
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
                    const y = this.getTopEdgeYPos(i) + titleHeight + (j + 1) * chartHeight / (length + 1);

                    this.ctx.fillText(text, x, y);
                });
            }
        });
    }

    drawChartTitle(): void {
        const { containerWidth, titleHeight, marginRightForLinkIcon, linkIconCode } = this.chartConstant;
        const linkIconWidth = this.ctx.measureText(linkIconCode).width;

        Object.keys(this._activeThreadCounts).forEach((agentName: string, i: number) => {
            this.ctx.fillStyle = '#74879a';
            this.ctx.fillRect(this.getLeftEdgeXPos(i), this.getTopEdgeYPos(i), containerWidth, titleHeight);

            this.ctx.font = '11px Nanum Gothic';
            this.ctx.fillStyle = '#fff';
            this.ctx.textAlign = 'center';
            this.ctx.textBaseline = 'middle';
            this.ctx.fillText(this.getChartTitleText(agentName), this.getLeftEdgeXPos(i) + containerWidth / 2, this.getTopEdgeYPos(i) + titleHeight / 2);

            this.ctx.font = '600 9px "Font Awesome 5 Free"';
            this.ctx.textAlign = 'right';
            this.ctx.fillText(linkIconCode, this.getLeftEdgeXPos(i) + containerWidth - marginRightForLinkIcon, this.getTopEdgeYPos(i) + titleHeight / 2);

            this.linkIconInfoList.push({
                leftX: this.getLeftEdgeXPos(i) + containerWidth - marginRightForLinkIcon - linkIconWidth,
                rightX: this.getLeftEdgeXPos(i) + containerWidth - marginRightForLinkIcon,
                topY: this.getTopEdgeYPos(i) + titleHeight / 2 - linkIconWidth / 2,
                bottomY: this.getTopEdgeYPos(i) + titleHeight / 2 + linkIconWidth / 2,
                key: agentName
            });
        });
    }

    getChartTitleText(text: string): string {
        const { containerWidth, marginRightForLinkIcon, linkIconCode, ellipsis } = this.chartConstant;
        const linkIconWidth = this.ctx.measureText(linkIconCode).width;
        const ellipsisWidth = this.ctx.measureText(ellipsis).width;
        const textWidth = this.ctx.measureText(text).width;
        const maxWidth = containerWidth / 2 - linkIconWidth - marginRightForLinkIcon - 5;
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

    drawChartContainerRect(): void {
        const { containerWidth, containerHeight, titleHeight } = this.chartConstant;

        for (let i = 0; i < this.numOfAgent; i++) {
            this.ctx.fillStyle = '#e8e5f0';
            this.ctx.fillRect(this.getLeftEdgeXPos(i), this.getTopEdgeYPos(i) + titleHeight, containerWidth, containerHeight);
        }
    }

    // Vertical Grid Line
    drawVGridLine(timestamp: number): void {
        const { chartWidth, gridLineSpeedControl, titleHeight, chartHeight } = this.chartConstant;

        for (let i = 0; i < this.numOfAgent; i++) {
            if (!this.gridLineStart) {
                this.gridLineStart = timestamp;
            }

            const xPos = this.getLeftEdgeXPos(i) + chartWidth - Math.floor((timestamp - this.gridLineStart) / gridLineSpeedControl) % chartWidth;

            this.ctx.beginPath();
            this.ctx.moveTo(xPos, this.getTopEdgeYPos(i) + titleHeight);
            this.ctx.lineTo(xPos, this.getTopEdgeYPos(i) + titleHeight + chartHeight);
            this.ctx.strokeStyle = 'rgba(0, 0, 0, 0.1)';
            this.ctx.stroke();
        }
    }

    drawChart(timestamp: number): void {
        if (!this.chartStart) {
            this.chartStart = timestamp;
        }

        const { chartWidth, titleHeight, chartHeight, chartColors, chartSpeedControl } = this.chartConstant;
        this.startingXPos = chartWidth - Math.floor((timestamp - this.chartStart) / chartSpeedControl);
        const isOverflow = this._timeStampList.length >= 2 && this.getXPosInChart(1) < 0;

        if (isOverflow) {
            this._timeStampList.shift();
        }

        const xPos0 = this.getXPosInChart(0);

        if (xPos0 < chartWidth) {
            Object.keys(this._dataList).forEach((agentName: string, i: number) => {
                const dataList = this._dataList[agentName];

                if (isOverflow) {
                    dataList.forEach((dataArr: number[]) => {
                        dataArr.shift();
                    });
                }

                const originXPos = this.getLeftEdgeXPos(i);
                const yAxisFlipValue = this.getTopEdgeYPos(i) + titleHeight + chartHeight; // for upside down
                const max = Math.max(...dataList.map((data: number[]) => Math.max(...data)));
                const length = dataList.length;

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
                            this.ctx.lineTo(this.getLeftEdgeXPos(i) + chartWidth, yAxisFlipValue - (data[k - 1] + (chartWidth - poskm1) * (data[k] - data[k - 1]) / (posk - poskm1)) * this.ratio);
                            break;
                        } else {
                            this.ctx.lineTo(originXPos + this.getXPosInChart(k), yAxisFlipValue - (data[k] * this.ratio));
                        }
                    }

                    this.ctx.lineTo(originXPos + chartWidth, yAxisFlipValue); // 마지막

                    this.ctx.fillStyle = chartColors[j];
                    this.ctx.fill();
                }
            });
        }
    }

    getXPosInChart(i: number): number {
        const { chartSpeedControl } = this.chartConstant;

        return this.startingXPos + Math.floor((this._timeStampList[i] - this.firstTimeStamp) / chartSpeedControl);
    }

    resize(): void {
        const { canvasLeftPadding, canvasRightPadding, chartWidth, gapBtnChart, titleHeight, chartHeight, canvasBottomPadding } = this.chartConstant;

        this.canvas.width = this.el.nativeElement.offsetWidth;
        this.chartNumPerRow = Math.floor((this.canvas.width - canvasLeftPadding - canvasRightPadding) / (chartWidth + gapBtnChart));
        this.canvas.height = this.getTopEdgeYPos(this.numOfAgent - 1) + titleHeight + chartHeight + canvasBottomPadding;
    }

    addEventListener(): void {
        window.addEventListener('resize', (() => this.resize()));
    }

    onMouseMove(e: MouseEvent): void {
        this.renderer.setStyle(this.canvas, 'cursor', this.getLinkIconInfo(e) ? 'pointer' : 'auto');
    }

    onClick(e: MouseEvent): void {
        const linkIconInfo = this.getLinkIconInfo(e);

        if (linkIconInfo) {
            this.outOpenThreadDump.emit(linkIconInfo.key);
        }
    }

    getLinkIconInfo(e: MouseEvent): { [key: string]: any } {
        const { left, top } = this.canvas.getBoundingClientRect();
        const { clientX, clientY } = e;
        const xPosOnCanvas = clientX - left;
        const yPosOnCanvas = clientY - top;

        return this.linkIconInfoList.find((linkIcon: { [key: string]: any }) => {
            const { leftX, rightX, topY, bottomY } = linkIcon;

            return xPosOnCanvas >= leftX && xPosOnCanvas <= rightX && yPosOnCanvas >= topY && yPosOnCanvas <= bottomY;
        });
    }
}
