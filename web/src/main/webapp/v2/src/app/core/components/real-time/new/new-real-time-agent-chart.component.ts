import { Component, OnInit, OnDestroy, Input, Output, EventEmitter, AfterViewInit, ViewChild, ElementRef, Renderer2 } from '@angular/core';

// import { IRealTimeChartData } from './real-time-chart.component';
import { IActiveThreadCounts } from 'app/core/components/real-time/real-time-websocket.service';

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
        this._dataList = Object.keys(activeThreadCounts).reduce((acc: { [key: string]: number[][] }, curr: string) => {
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

    showAxis = false;
    chartConstant = {
        canvasLeftPadding: 0,
        canvasTopPadding: 0,
        canvasRightPadding: 0,
        canvasBottomPadding: 0,
        chartWidth: 152,
        chartHeight: 52,
        titleHeight: 32,
        gapBtnChart: 10,
        chartColors: ['#33b692', '#51afdf', '#fea63e', '#e76f4b'],
        yRatio: 3 / 5,
        gridLineSpeedControl: 25,
        chartSpeedControl: 25,
        linkIconCode: '\uf35d',
        linkIconWidth: 0,
        marginRightForLinkIcon: 10,
        ellipsis: '...',
        ellipsisWidth: 0,
    };
    canvas: HTMLCanvasElement;
    ctx: CanvasRenderingContext2D;
    chartNumPerRow: number;
    linkIconInfoList: { [key: string]: any }[] = []; // { leftX, rightX, topY, bottomY, agentKey } 를 담은 object의 배열
    gridLineStart: number = null;
    chartStart: number = null;
    animationFrameId: number;

    constructor(
        private el: ElementRef,
        private renderer: Renderer2
    ) {}
    ngOnInit() {}
    ngAfterViewInit() {
        this.canvas = this.canvasRef.nativeElement;
        this.ctx = this.canvas.getContext('2d');

        this.resize();
        this.initConstant();
        this.addEventListener();

        this.animationFrameId = requestAnimationFrame((t) => this.draw(t));
    }

    ngOnDestroy() {
        cancelAnimationFrame(this.animationFrameId);
    }

    initConstant(): void {
        this.chartConstant.linkIconWidth = this.ctx.measureText(this.chartConstant.linkIconCode).width;
        this.chartConstant.ellipsisWidth = this.ctx.measureText(this.chartConstant.ellipsis).width;
    }

    draw(timestamp: number): void {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.drawChartTitle();
        this.drawChartContainerRect();
        this.drawGridLine(timestamp);
        if (this._timeStampList.length !== 0) {
            this.drawChart(timestamp);
        }

        this.animationFrameId = requestAnimationFrame((t) => this.draw(t));
    }

    getXPos(i: number): number {
        // 차트 컨테이너 왼쪽 위 꼭짓점 x좌표를 리턴
        return this.chartConstant.canvasLeftPadding + (this.chartConstant.chartWidth + this.chartConstant.gapBtnChart) * (i % this.chartNumPerRow);
    }

    getYPos(i: number): number {
        // 차트 컨테이너 왼쪽 위 꼭짓점 y좌표를 리턴
        return this.chartConstant.canvasTopPadding + (this.chartConstant.chartHeight + this.chartConstant.titleHeight + this.chartConstant.gapBtnChart) * Math.floor(i / this.chartNumPerRow);
    }

    drawChartTitle(): void {
        Object.keys(this._activeThreadCounts).forEach((agentName: string, i: number) => {
            this.ctx.fillStyle = '#74879a';
            this.ctx.fillRect(this.getXPos(i), this.getYPos(i), this.chartConstant.chartWidth, this.chartConstant.titleHeight);

            this.ctx.font = '11px Nanum Gothic';
            this.ctx.fillStyle = '#fff';
            this.ctx.textAlign = 'center';
            this.ctx.textBaseline = 'middle';
            this.ctx.fillText(this.getChartTitleText(agentName), this.getXPos(i) + this.chartConstant.chartWidth / 2, this.getYPos(i) + this.chartConstant.titleHeight / 2);

            this.ctx.font = '600 9px "Font Awesome 5 Free"';
            this.ctx.textAlign = 'right';
            this.ctx.fillText(this.chartConstant.linkIconCode, this.getXPos(i) + this.chartConstant.chartWidth - this.chartConstant.marginRightForLinkIcon, this.getYPos(i) + this.chartConstant.titleHeight / 2);

            this.linkIconInfoList.push({
                leftX: this.getXPos(i) + this.chartConstant.chartWidth - this.chartConstant.marginRightForLinkIcon - this.chartConstant.linkIconWidth,
                rightX: this.getXPos(i) + this.chartConstant.chartWidth - this.chartConstant.marginRightForLinkIcon,
                topY: this.getYPos(i) + this.chartConstant.titleHeight / 2 - this.chartConstant.linkIconWidth / 2,
                bottomY: this.getYPos(i) + this.chartConstant.titleHeight / 2 + this.chartConstant.linkIconWidth / 2,
                key: agentName
            });
        });
    }

    getChartTitleText(text: string): string {
        const textWidth = this.ctx.measureText(text).width;
        const maxWidth = this.chartConstant.chartWidth / 2 - this.chartConstant.linkIconWidth - this.chartConstant.marginRightForLinkIcon;
        const isOverflow = textWidth / 2  > maxWidth;

        if (isOverflow) {
            let length = text.length;
            let newText;
            let newTextWidth;

            do {
                newText = text.substring(0, length - 1);
                newTextWidth = this.ctx.measureText(newText).width;
                length--;
            } while (newTextWidth / 2 + this.chartConstant.ellipsisWidth > maxWidth);

            return newText + this.chartConstant.ellipsis;
        } else {
            return text;
        }
    }

    drawChartContainerRect(): void {
        for (let i = 0; i < this.numOfAgent; i++) {
            this.ctx.fillStyle = '#e8e5f0';
            this.ctx.fillRect(this.getXPos(i), this.getYPos(i) + this.chartConstant.titleHeight, this.chartConstant.chartWidth, this.chartConstant.chartHeight);
        }
    }

    // Vertical Grid Line
    drawGridLine(timestamp: number): void {
        for (let i = 0; i < this.numOfAgent; i++) {
            if (!this.gridLineStart) {
                this.gridLineStart = timestamp;
            }

            const xPos = this.getXPos(i) + this.chartConstant.chartWidth - Math.floor((timestamp - this.gridLineStart) / this.chartConstant.gridLineSpeedControl) % this.chartConstant.chartWidth;

            this.ctx.beginPath();
            this.ctx.moveTo(xPos, this.getYPos(i) + this.chartConstant.titleHeight);
            this.ctx.lineTo(xPos, this.getYPos(i) + this.chartConstant.titleHeight + this.chartConstant.chartHeight);
            this.ctx.strokeStyle = 'rgba(0, 0, 0, 0.1)';
            this.ctx.stroke();
        }
    }

    drawChart(timestamp: number): void {
        if (!this.chartStart) {
            this.chartStart = timestamp;
        }

        const startingXPos = this.chartConstant.chartWidth - Math.floor((timestamp - this.chartStart) / this.chartConstant.chartSpeedControl); // 최초의 시작하는 점의 x좌표
        const isOverflow = this._timeStampList.length >= 2 && startingXPos + Math.floor((this._timeStampList[1] - this.firstTimeStamp) / this.chartConstant.chartSpeedControl) < 0;

        if (isOverflow) {
            this._timeStampList.shift();
        }

        const xPos0 = startingXPos + Math.floor((this._timeStampList[0] - this.firstTimeStamp) / this.chartConstant.chartSpeedControl);

        if (xPos0 < this.chartConstant.chartWidth) {
            Object.keys(this._dataList).forEach((agentName: string, i: number) => {
                const xPos = this.getXPos(i) + startingXPos; // 각 차트에서의 기준점(t0) x좌표
                const yPos = this.getYPos(i); // 왼쪽위 꼭짓점 y좌표
                const yAxisFlipValue = yPos + this.chartConstant.titleHeight + this.chartConstant.chartHeight; // for upside down

                const dataList = this._dataList[agentName];
                const max = Math.max(...dataList.map((data: number[]) => Math.max(...data)));
                const contentRatio = this.chartConstant.chartHeight * this.chartConstant.yRatio / max;
                const length = dataList.length;

                if (isOverflow) {
                    dataList.forEach((dataArr: number[]) => {
                        dataArr.shift();
                    });
                }

                for (let j = 0; j < length; j++) {
                    const data = dataList[j];

                    this.ctx.beginPath();

                    if (xPos0 < 0) {
                        // 앞 경계면 처리
                        const xPos1 = startingXPos + Math.floor((this._timeStampList[1] - this.firstTimeStamp) / this.chartConstant.chartSpeedControl);

                        this.ctx.moveTo(this.getXPos(i), yAxisFlipValue); // 시작
                        this.ctx.lineTo(this.getXPos(i), yAxisFlipValue - (data[0] * xPos1 - data[1] * xPos0) / (xPos1 - xPos0) * contentRatio);
                    } else {
                        this.ctx.moveTo(xPos + Math.floor((this._timeStampList[0] - this.firstTimeStamp) / this.chartConstant.chartSpeedControl), yAxisFlipValue); // 시작
                        this.ctx.lineTo(xPos + Math.floor((this._timeStampList[0] - this.firstTimeStamp) / this.chartConstant.chartSpeedControl), yAxisFlipValue - (data[0] * contentRatio));
                    }

                    const timeStampLength = this._timeStampList.length;

                    for (let k = 1; k < timeStampLength; k++) {
                        const poskm1 = startingXPos + Math.floor((this._timeStampList[k - 1] - this.firstTimeStamp) / this.chartConstant.chartSpeedControl);
                        const posk = startingXPos + Math.floor((this._timeStampList[k] - this.firstTimeStamp) / this.chartConstant.chartSpeedControl);

                        if (poskm1 <= this.chartConstant.chartWidth && posk > this.chartConstant.chartWidth) {
                            // 뒷 경계면 처리
                            this.ctx.lineTo(this.getXPos(i) + this.chartConstant.chartWidth, yAxisFlipValue - (data[k - 1] + (this.chartConstant.chartWidth - poskm1) * (data[k] - data[k - 1]) / (posk - poskm1)) * contentRatio);
                            break;
                        } else {
                            this.ctx.lineTo(xPos + Math.floor((this._timeStampList[k] - this.firstTimeStamp) / this.chartConstant.chartSpeedControl), yAxisFlipValue - (data[k] * contentRatio));
                        }
                    }

                    this.ctx.lineTo(this.getXPos(i) + this.chartConstant.chartWidth, yAxisFlipValue); // 마지막

                    // this.ctx.strokeStyle = this.chartConstant.chartColors[j];
                    this.ctx.fillStyle = this.chartConstant.chartColors[j];
                    // this.ctx.stroke();
                    this.ctx.fill();
                }
            });
        }
    }

    resize(): void {
        this.canvas.width = this.el.nativeElement.offsetWidth;
        this.chartNumPerRow = Math.floor((this.canvas.width - this.chartConstant.canvasLeftPadding - this.chartConstant.canvasRightPadding) / (this.chartConstant.chartWidth + this.chartConstant.gapBtnChart));
        this.canvas.height = this.getYPos(this.numOfAgent - 1) + this.chartConstant.titleHeight + this.chartConstant.chartHeight + this.chartConstant.canvasBottomPadding;
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
