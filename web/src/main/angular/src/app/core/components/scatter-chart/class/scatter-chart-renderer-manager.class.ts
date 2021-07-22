import { isEmpty } from 'app/core/utils/util';
import { IOptions } from './scatter-chart.class';
import { DataIndex, ScatterChartDataBlock } from './scatter-chart-data-block.class';
import { ScatterChartSizeCoordinateManager } from './scatter-chart-size-coordinate-manager.class';
import { ScatterChartTransactionTypeManager } from './scatter-chart-transaction-type-manager.class';

export class ScatterChartRendererManager {
    private canvasMap: {[key: string]: HTMLCanvasElement[]};
    private ctxMap: {[key: string]: CanvasRenderingContext2D[]};

    private elementScroller: HTMLElement;
    private scrollOrder = [0, 1];
    private requestAnimationFrameRef: any;

    private spareCanvasMap = new Map<string, CanvasRenderingContext2D[]>();
    private t0 = -1;

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
                if (key.startsWith(agentName) && key.endsWith(typeName)) {
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

    private getCoord(data: number[]): any {
        const rangeY = this.coordinateManager.getY();

        return {
            x: (data[DataIndex.X] - this.coordinateManager.getInitFromX()) * this.coordinateManager.getPixelPerTime(),
            y: this.coordinateManager.parseYDataToYChart(Math.min(rangeY.to, Math.max(rangeY.from, data[DataIndex.Y])))
        };
    }

    private drawPoint(context: CanvasRenderingContext2D, {x, y}: any, {color, alpha}: any): void {
        const bubbleRadius = this.options.bubbleRadius;
        const r = this.coordinateManager.parseZDataToZChart(bubbleRadius);

        context.beginPath();
        context.fillStyle = color;
        context.strokeStyle = color;
        context.arc(x, y, r, 0, Math.PI * 2, true);
        context.globalAlpha = alpha >= 1 ? 1 : alpha;
        context.fill();
    }

    private generateCanvas(order: number): CanvasRenderingContext2D {
        const canvas = document.createElement('canvas');

        canvas.width = this.coordinateManager.getCanvasWidth();
        canvas.height = this.coordinateManager.getHeightOfChartSpace() + this.coordinateManager.getBubbleSize();
        canvas.setAttribute('data-order', `${order}`);

        return canvas.getContext('2d');
    }

    drawTransaction(key: string, color: string, data: number[]): void {
        const {x, y} = this.getCoord(data);
        let renderedX;

        let ctxIndex = this.scrollOrder[0];
        const canvasWidth = this.coordinateManager.getCanvasWidth();
        const zeroLeft = Math.round(parseInt(this.canvasMap[key][ctxIndex].style.left, 10));
        const currentMaxX = zeroLeft + canvasWidth;
        const alpha = 0.3 + (0.1 * data[DataIndex.GROUP_COUNT]);

        if (x > currentMaxX) {
            if ((x - currentMaxX) <= canvasWidth) {
                // when it's drawable in the following canvas
                ctxIndex = this.scrollOrder[1];
                renderedX = x - currentMaxX;
            } else {
                // if not, draw it in a spare canvas and keep it there till the first canvas gets out of the area and moves to the tail.
                const canvasCount = Math.floor(x / canvasWidth);
                let context: CanvasRenderingContext2D;

                renderedX = x - (canvasCount * canvasWidth);
                if (!this.spareCanvasMap.has(key)) {
                    context = this.generateCanvas(canvasCount + 1);
                    this.spareCanvasMap.set(key, [context]);
                } else {
                    const c = this.spareCanvasMap.get(key).find((ctx: CanvasRenderingContext2D) => ctx.canvas.getAttribute('data-order') === `${canvasCount + 1}`);

                    if (!!c) {
                        context = c;
                    } else {
                        context = this.generateCanvas(canvasCount + 1);
                        this.spareCanvasMap.set(key, [...this.spareCanvasMap.get(key), context]);
                    }
                }

                this.drawPoint(context, {x: renderedX, y}, {color, alpha});
                return;
            }
        } else {
            renderedX = x - zeroLeft;
        }

        this.drawPoint(this.ctxMap[key][ctxIndex], {x: renderedX, y}, {color, alpha});
    }
    moveChart(timestamp: number): void {
        if (this.t0 === -1) {
            this.t0 = timestamp;
        }

        const deltaT = timestamp - this.t0;
        const deltaX =  this.coordinateManager.getPixelPerTime() * deltaT;
        const canvasWidth = this.coordinateManager.getCanvasWidth();
        const height = this.coordinateManager.getHeight();
        // const baseLeft = parseInt(this.elementScroller.style.left, 10);
        const baseLeft = 0;
        const nextLeft = baseLeft - deltaX;

        const orderFirst = this.scrollOrder[0];
        const orderSecond = this.scrollOrder[1];
        let bOverBoundary = false;

        // this.elementScroller.style.left = nextLeft.toFixed(2) + 'px';
        // this.elementScroller.style.transform = `translateX(${nextLeft}px)`;
        this.elementScroller.style.transform = `translate3d(${nextLeft}px, 0, 0)`;
        Object.keys(this.canvasMap).forEach((key: string): void => {
            const canvasArr = this.canvasMap[key];

            if (Math.abs(nextLeft) > (parseInt(canvasArr[orderFirst].style.left, 10) + canvasWidth)) {
                bOverBoundary = true;
                canvasArr[orderFirst].style.left = (parseInt(canvasArr[orderSecond].style.left, 10) + canvasWidth) + 'px';
                this.ctxMap[key][orderFirst].clearRect(0, 0, canvasWidth, height);
                const queuedCtxList = this.spareCanvasMap.get(key);

                if (this.spareCanvasMap.has(key) && !isEmpty(queuedCtxList)) {
                    // draw the points in the spare canvas to the existent one as image.
                    this.ctxMap[key][orderFirst].globalAlpha = 1; // needs to reset the globalAlpha of the destination canvas.
                    this.ctxMap[key][orderFirst].drawImage(this.spareCanvasMap.get(key)[0].canvas, 0, 0);
                    this.spareCanvasMap.get(key)[0].canvas.remove();
                    this.spareCanvasMap.get(key).shift();
                }
            }
        });

        if (bOverBoundary) {
            this.scrollOrder[0] = orderSecond;
            this.scrollOrder[1] = orderFirst;
        }

        this.requestAnimationFrameRef = requestAnimationFrame((t) => this.moveChart(t));
    }
    reset(mode: string) {
        this.t0 = -1;
        this.scrollOrder = [0, 1];
        this.elementScroller.style.transform = `translate3d(0, 0, 0)`;
        this.elementScroller.innerHTML = '';
        this.initVariable();

        if (mode === 'realtime') {
            cancelAnimationFrame(this.requestAnimationFrameRef);
            this.requestAnimationFrameRef = requestAnimationFrame((t) => this.moveChart(t));
        } else {
            cancelAnimationFrame(this.requestAnimationFrameRef);
        }
    }
    clear() {
        const width = this.coordinateManager.getCanvasWidth();
        const height = this.coordinateManager.getHeight();

        Object.keys(this.ctxMap).forEach((key: string) => {
            this.ctxMap[key].forEach((ctx: CanvasRenderingContext2D) => {
                ctx.clearRect(0, 0, width, height);
            });
        });

        this.scrollOrder = [0, 1];
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
