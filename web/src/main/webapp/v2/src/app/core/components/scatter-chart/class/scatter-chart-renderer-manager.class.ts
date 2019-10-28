import { IOptions } from './scatter-chart.class';
import { DataIndex, ScatterChartDataBlock } from './scatter-chart-data-block.class';
import { ScatterChartSizeCoordinateManager } from './scatter-chart-size-coordinate-manager.class';
import { ScatterChartTransactionTypeManager } from './scatter-chart-transaction-type-manager.class';

export class ScatterChartRendererManager {
    private canvasMap: {[key: string]: HTMLCanvasElement[]};
    private ctxMap: {[key: string]: CanvasRenderingContext2D[]};

    private elementScroller: HTMLElement;
    private scrollOrder: number[];
    private requestAnimationFrameRef: any;
    constructor(
        private options: IOptions,
        private coordinateManager: ScatterChartSizeCoordinateManager,
        private elementContainer: any,
        private typeManager: ScatterChartTransactionTypeManager,
    ) {
        this.initVariable();
        this.initCanvasWrapper();
    }
    private initVariable(): void {
        this.canvasMap = {};
        this.ctxMap = {};
    }
    private initCanvasWrapper() {
        let zIndex = 100;
        const bubbleSize = this.coordinateManager.getBubbleSize();
        const bubbleHalfSize = this.coordinateManager.getBubbleHalfSize();
        const widthOfChartSpace = this.coordinateManager.getWidthOfChartSpace();
        const heightOfChartSpace = this.coordinateManager.getHeightOfChartSpace();

        this.elementScroller = document.createElement('div');
        this.elementScroller.setAttribute('style', 'top: 0px; left: 0px; position: absolute; background-color: grey; height:' + (heightOfChartSpace + bubbleSize) + 'px' );
        this.elementScroller.setAttribute('class', 'canvas-scroller');

        const elementCanvasWrapper = document.createElement('div');
        const elementCanvasWrapperStyles = `
            top: ${this.coordinateManager.getTopPadding()}px;
            left: ${(this.coordinateManager.getLeftPadding() + bubbleHalfSize)}px;
            width: ${widthOfChartSpace}px;
            height: ${heightOfChartSpace + bubbleSize}px;
            z-index: ${zIndex++};
            overflow: hidden;
            position: absolute;
        `;
        elementCanvasWrapper.setAttribute('class', 'canvas-wrapper');
        elementCanvasWrapper.setAttribute('style', elementCanvasWrapperStyles);
        elementCanvasWrapper.appendChild(this.elementScroller);
        this.elementContainer.appendChild(elementCanvasWrapper);
    }
    makeDataCanvas(dataBlock: ScatterChartDataBlock, agentList: string[]): void {
        let zIndex = 110;
        const bubbleSize = this.coordinateManager.getBubbleSize();
        const prefix = this.options.prefix;
        const heightOfChartSpace = this.coordinateManager.getHeightOfChartSpace();
        const canvasWidth = this.coordinateManager.getCanvasWidth();

        agentList.forEach((agentName: string, index: number) => {
            this.typeManager.getTypeNameList().forEach((typeName: string) => {
                const key = `${agentName}-${prefix}-${typeName}`;
                if ((key in this.canvasMap) === false) {
                    const canvas1 = this.createCanvas({
                        width: canvasWidth + 'px',
                        height: (heightOfChartSpace + bubbleSize) + 'px',
                        'data-agent': agentName,
                        'data-type': typeName,
                        'data-key': key
                    }, `
                        top: 0px;
                        left: 0px;
                        z-index: ${zIndex++};
                        position: absolute;
                    `);
                    const canvas2 = this.createCanvas({
                        width: canvasWidth + 'px',
                        height: (heightOfChartSpace + bubbleSize) + 'px',
                        'data-agent': agentName,
                        'data-type': typeName,
                        'data-key': key
                    }, `
                        top: 0px;
                        left: ${canvasWidth}px;
                        z-index: ${zIndex++};
                        position: absolute;
                    `);
                    this.elementScroller.appendChild(canvas1);
                    this.elementScroller.appendChild(canvas2);
                    this.canvasMap[key] = [canvas1, canvas2];
                    this.ctxMap[key] = [canvas1.getContext('2d'), canvas2.getContext('2d')];
                }
            });
        });
        this.scrollOrder = [0, 1];
    }
    private createCanvas(attrs: object, styles: string): HTMLCanvasElement {
        const elementCanvas = document.createElement('canvas');
        Object.keys(attrs).forEach((key: string) => {
            elementCanvas.setAttribute(key, attrs[key]);
        });
        elementCanvas.setAttribute('style', styles);
        return elementCanvas;
    }
    setTypeView(agentName: string, typeName: string, typeChecked: boolean): void {
        const viewType = typeChecked ? 'block' : 'none';
        Object.keys(this.canvasMap).forEach((key: string) => {
            this.canvasMap[key].forEach((canvas: HTMLCanvasElement) => {
                if (key.endsWith(typeName)) {
                    canvas.style.display = viewType;
                }
            });
        });
    }
    toggle(bIsAll: boolean, agentName: string, type: string): void {
        Object.keys(this.canvasMap).forEach((key: string) => {
            this.canvasMap[key].forEach((canvas: HTMLCanvasElement) => {
                if ((bIsAll || key.startsWith(agentName)) && key.endsWith(type)) {
                    if ( canvas.style.display === 'none' ) {
                        canvas.style.display = 'block';
                    } else {
                        canvas.style.display = 'none';
                    }
                }
            });
        });
    }
    showSelectedAgent(agentName: string): void {
        if (agentName === '') {
            Object.keys(this.canvasMap).forEach((key: string) => {
                this.canvasMap[key].forEach((canvas: HTMLCanvasElement) => {
                    if (this.typeManager.isCheckedByName(key.split('-').pop())) {
                        canvas.style.display = 'block';
                    } else {
                        canvas.style.display = 'none';
                    }
                });
            });
        } else {
            Object.keys(this.canvasMap).forEach((key: string) => {
                if (key.startsWith(agentName)) {
                    this.canvasMap[key].forEach((canvas: HTMLCanvasElement) => {
                        if (this.typeManager.isCheckedByName(key.split('-').pop())) {
                            canvas.style.display = 'block';
                        }
                    });
                } else {
                    this.canvasMap[key].forEach((canvas: HTMLCanvasElement) => {
                        canvas.style.display = 'none';
                    });
                }
            });
        }
    }
    drawTransaction(key: string, color: string, data: number[]): void {
        const bubbleRadius = this.options.bubbleRadius;
        const rangeY = this.coordinateManager.getY();

        let x = (data[DataIndex.X] - this.coordinateManager.getInitFromX()) * this.coordinateManager.getPixelPerTime();
        const y = this.coordinateManager.parseYDataToYChart(Math.min(rangeY.to, Math.max(rangeY.from, data[DataIndex.Y])));
        const r = this.coordinateManager.parseZDataToZChart(bubbleRadius);

        let ctxIndex = this.scrollOrder[0];
        const canvasWidth = this.coordinateManager.getCanvasWidth();
        const zeroLeft = Math.round(parseInt(this.canvasMap[key][ctxIndex].style.left, 10));
        const currentMaxX = zeroLeft + canvasWidth;
        if (x > currentMaxX) {
            ctxIndex = this.scrollOrder[1];
            x -= currentMaxX;
        } else {
            x -= zeroLeft;
        }

        this.ctxMap[key][ctxIndex].beginPath();
        this.ctxMap[key][ctxIndex].fillStyle = color;
        this.ctxMap[key][ctxIndex].strokeStyle = color;
        this.ctxMap[key][ctxIndex].arc(x, y, r, 0, Math.PI * 2, true);
        this.ctxMap[key][ctxIndex].globalAlpha = 0.3 + (0.1 * data[DataIndex.GROUP_COUNT]);
        this.ctxMap[key][ctxIndex].fill();
    }
    moveChart(moveXValue: number, duration: number): void {
        const canvasWidth = this.coordinateManager.getCanvasWidth();
        const height = this.coordinateManager.getHeight();
        const baseLeft = parseInt(this.elementScroller.style.left, 10);
        const nextLeft = baseLeft - moveXValue;

        const self = this;
        let startTime = -1;
        function moveElement(timestamp: number, inMoveXValue: number, inDuration: number): void {
            const runTime = timestamp - startTime;
            let progress = runTime / inDuration;
            progress = Math.min(progress, 1);

            self.elementScroller.style.left = (baseLeft + -(inMoveXValue * progress)).toFixed(2) + 'px';
            if (runTime < inDuration) {
                self.requestAnimationFrameRef = window.requestAnimationFrame((time: number) => {
                    moveElement(time, inMoveXValue, inDuration);
                });
            } else {
                const orderFirst = self.scrollOrder[0];
                const orderSecond = self.scrollOrder[1];
                let bOverBoundary = false;
                Object.keys(self.canvasMap).forEach((key: string): void => {
                    const canvasArr = self.canvasMap[key];
                    if (Math.abs(nextLeft) > (parseInt(canvasArr[orderFirst].style.left, 10) + canvasWidth)) {
                        bOverBoundary = true;
                        canvasArr[orderFirst].style.left = (parseInt(canvasArr[orderSecond].style.left, 10) + canvasWidth) + 'px';
                        self.ctxMap[key][orderFirst].clearRect(0, 0, canvasWidth, height);
                    }
                });
                if (bOverBoundary) {
                    self.scrollOrder[0] = orderSecond;
                    self.scrollOrder[1] = orderFirst;
                }
            }
        }
        this.requestAnimationFrameRef = window.requestAnimationFrame((timestamp: number) => {
            startTime = timestamp;
            moveElement(timestamp, moveXValue, 300);
        });
    }
    reset() {
        window.cancelAnimationFrame(this.requestAnimationFrameRef);
        this.scrollOrder = [0, 1];
        this.elementScroller.style.left = '0px';
        this.elementScroller.innerHTML = '';
        this.initVariable();
    }
    clear() {
        const width = this.coordinateManager.getCanvasWidth();
        const height = this.coordinateManager.getHeight();

        Object.keys(this.ctxMap).forEach((key: string) => {
            this.ctxMap[key].forEach((ctx: CanvasRenderingContext2D) => {
                ctx.clearRect(0, 0, width, height);
            });
        });
    }
    drawToCanvas(ctxDownload: CanvasRenderingContext2D, topPadding: number): CanvasRenderingContext2D {
        const padding = this.coordinateManager.getPadding();
        const bubbleHalfSize = this.coordinateManager.getBubbleHalfSize();
        // scatter
        Object.keys(this.canvasMap).forEach((key: string) => {
            this.canvasMap[key].forEach((canvas: HTMLCanvasElement) => {
                if (canvas.style.display !== 'none') {
                    ctxDownload.drawImage(canvas, padding.left + bubbleHalfSize, padding.top + topPadding);
                }
            });
        });
        ctxDownload.textBaseline = 'top';
        return ctxDownload;
    }
}
