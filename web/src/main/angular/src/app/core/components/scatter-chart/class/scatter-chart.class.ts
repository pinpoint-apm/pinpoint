import * as moment from 'moment-timezone';
import { Subject, Observable } from 'rxjs';
import { ScatterChartSizeCoordinateManager } from './scatter-chart-size-coordinate-manager.class';
import { ScatterChartRendererManager } from './scatter-chart-renderer-manager.class';
import { ScatterChartDataBlock } from './scatter-chart-data-block.class';
import { ScatterChartGridRenderer } from './scatter-chart-grid-renderer.class';
import { ScatterChartAxisRenderer } from './scatter-chart-axis-renderer.class';
import { ScatterChartTransactionTypeManager } from './scatter-chart-transaction-type-manager.class';
import { ScatterChartMouseManager } from './scatter-chart-mouse-manager.class';
import { sumObjByKey } from 'app/core/utils/util';
import { ScatterChartVirtualGridManager } from './scatter-chart-virtual-grid-manager.class';

export interface IOptions {
    mode: string;
    prefix: string;
    width: number;
    height: number;
    bubbleRadius: number;
    padding: {
        top: number;
        left: number;
        right: number;
        bottom: number;
    };
    axisLabelStyle: string;
    axisColor: string;
    textColor: string;
    lineColor: string;
    x: {
        from: number;
        to: number;
    };
    y: {
        from: number;
        to: number;
    };
    z: {
        from: number;
        to: number;
    };
    ticks: {
        x: number;
        y: number;
    };
    tickLength: {
        x: number;
        y: number;
    };
    gridAxisStyle: {
        lineDash: number[];
        lineWidth: number;
        globalAlpha: number;
        strokeStyle: string
    };
    axisUnit: {
        x: string;
        y: string;
    };
    showMouseGuideLine: boolean;
    animationDuration: number;
    timezone: string;
    dateFormat: string[];
}

export class ScatterChart {
    static MODE = {
        REALTIME: 'realtime',
        STATIC: 'static'
    };
    private options: IOptions;
    private virtualGridManager: ScatterChartVirtualGridManager;
    private typeManager: ScatterChartTransactionTypeManager;
    private gridRenderer: ScatterChartGridRenderer;
    private axisRenderer: ScatterChartAxisRenderer;
    private coordinateManager: ScatterChartSizeCoordinateManager;
    private rendererManager: ScatterChartRendererManager;
    private mouseManager: ScatterChartMouseManager;
    private dataBlocks: ScatterChartDataBlock[] = [];
    private agentList: string[] = [];
    private countMap = new Map<string, {[key: number]: {[key: string]: number}}>();
    private selectedAgent = '';

    private downloadElement: HTMLElement;

    private outTransactionCount = new Subject<{[key: string]: number}>();
    private outSelect = new Subject<any>();
    private outError = new Subject<any>();
    private outChangeRangeX = new Subject<{from: number, to: number}>();

    onSelect$: Observable<any>;
    onError$: Observable<any>;
    onChangeRangeX$: Observable<{from: number, to: number}>;
    onChangeTransactionCount$: Observable<{[key: string]: number}>;

    constructor(
        private mode: string,
        private element: any,
        private fromX: number,
        private toX: number,
        private fromY: number,
        private toY: number,
        private application: string,
        agent: string,
        private width: number,
        private height: number,
        private timezone: string,
        private dateFormat: string[],
        private enableServerSideScan: boolean,
        private sampleScatter: boolean
    ) {
        this.downloadElement = document.createElement('a');
        this.selectedAgent = agent;
        this.setOptions();
        this.initManagers();

        this.onSelect$ = this.outSelect.asObservable();
        this.onError$ = this.outError.asObservable();
        this.onChangeRangeX$ = this.outChangeRangeX.asObservable();
        this.onChangeTransactionCount$ = this.outTransactionCount.asObservable();
    }
    private isSelectedAgent(agent: string): boolean {
        return this.selectedAgent === '' || this.selectedAgent === agent;
    }
    private getTransactionCount(): {[key: string]: number} {
        const xRange = this.coordinateManager.getX();
        const mergeCount = (obj: {[key: number]: {[key: string]: number}}) => {
            return Object.entries(obj).reduce((acc1, [to, countOb]) => {
                if (Number(to) >= xRange.from) {
                    return sumObjByKey(acc1, countOb);
                } else {
                    return acc1;
                }
            }, {});
        };

        return [...this.countMap]
            .filter(([agent]: [string, any]) => this.isSelectedAgent(agent))
            .reduce((acc, [_, obj]) => sumObjByKey(acc, mergeCount(obj)), {});
    }
    private setOptions() {
        const computedStyle = getComputedStyle(document.body);
        const colors = {
            text: computedStyle.getPropertyValue('--chart-text'),
            line: computedStyle.getPropertyValue('--chart-line'),
            guideLine: computedStyle.getPropertyValue('--chart-guide-line'),
        };
        this.options = {
            mode: this.mode,
            prefix: 'scatter-chart-' + (Math.random() * 10000),
            width: this.width,
            height: this.height,
            bubbleRadius: 3,
            padding: {
                top: 20,
                left: 60,
                right: 40,
                bottom: 40
            },
            axisLabelStyle: 'font-size:10px; line-height: 12px; padding-top: 3px',
            axisColor: '#000',
            textColor: colors.text,
            lineColor: colors.line,
            x: {
                from: this.fromX,
                to: this.toX
            },
            y: {
                from: this.fromY,
                to: this.toY
            },
            z: {
                from: 0,
                to: 5
            },
            ticks: {
                x: 5,
                y: 5
            },
            tickLength: {
                x: 10,
                y: 10
            },
            gridAxisStyle: {
                lineDash: [1, 0],
                lineWidth: 1,
                globalAlpha: 1,
                strokeStyle : colors.guideLine,
            },
            axisUnit: {
                x: '',
                y: '(ms)'
            },
            showMouseGuideLine: true,
            animationDuration: 300,
            timezone: this.timezone,
            dateFormat: this.dateFormat
        };
    }
    private initManagers(): void {
        this.typeManager = new ScatterChartTransactionTypeManager(ScatterChartTransactionTypeManager.getDefaultTransactionTypeInfo());
        this.coordinateManager = new ScatterChartSizeCoordinateManager(this.options);
        this.virtualGridManager = new ScatterChartVirtualGridManager(this.coordinateManager);
        this.gridRenderer = new ScatterChartGridRenderer(this.options, this.coordinateManager, this.element);
        this.axisRenderer = new ScatterChartAxisRenderer(this.options, this.coordinateManager, this.element);
        this.rendererManager = new ScatterChartRendererManager(this.options, this.coordinateManager, this.element, this.typeManager);
        this.mouseManager = new ScatterChartMouseManager(this.options, this.coordinateManager, this.element);
        this.mouseManager.onDragArea$.subscribe((area: any) => {
            const fromX = this.coordinateManager.parseMouseXToXData(area.x.from);
            const toX = this.coordinateManager.parseMouseXToXData(area.x.to);
            const fromY = this.coordinateManager.parseMouseYToYData(area.y.from);
            const toY = this.coordinateManager.parseMouseYToYData(area.y.to);

            const {to: maxY} = this.coordinateManager.getY();

            if (this.hasDataByXY(fromX, toX, fromY, toY)) {
                this.outSelect.next({
                    x: {
                        from: fromX,
                        to: toX
                    },
                    y: {
                        from: fromY,
                        to: toY >= maxY ? Number.MAX_SAFE_INTEGER : toY
                    },
                    drag: area,
                    type: this.typeManager.getCheckedTypeNameList(),
                    agent: this.selectedAgent
                });
            }
        });
    }
    changeShowType(typeInfo: {name: string, checked: boolean}): void {
        this.typeManager.setChecked(typeInfo.name, typeInfo.checked);
        this.rendererManager.setTypeView(this.selectedAgent, typeInfo.name, typeInfo.checked);
    }
    reset(application: string, agent: string, fromX: number, toX: number, mode: string, typeCheck?: any): void {
        this.fromX = fromX;
        this.toX = toX;
        this.options.mode = this.mode = mode;
        this.coordinateManager.resetInitX(fromX);
        this.coordinateManager.setX(fromX, toX);
        this.typeManager.reset();
        this.application = application;
        this.selectedAgent = agent;

        this.agentList = [];
        this.axisRenderer.reset();
        this.rendererManager.reset(mode);

        this.dataBlocks = [];

        this.countMap.clear();

        if (typeCheck) {
            this.changeShowType(typeCheck);
        }
    }
    addAgent(agentList: string[]): void {
        agentList.forEach((agentName: string) => {
            if (this.agentList.lastIndexOf(agentName) === -1) {
                this.agentList.push(agentName);
            }
        });
        this.agentList.sort();
    }
    addData(dataBlock: ScatterChartDataBlock, nextRequestTime?: number): void {
        const xRange = this.coordinateManager.getX();
        const dataBlockRange = dataBlock.getXRange();
        if (this.options.mode === ScatterChart.MODE.STATIC && dataBlockRange.from >= xRange.to) {
            return;
        }
        this.addAgent(dataBlock.getAgentList());
        if (!this.enableServerSideScan) {
            this.dataBlocks.push(dataBlock);
        }
        this.setCount(dataBlock);
        // if (this._bPause === true) return;
        this.drawDataBlock(dataBlock);
        if (this.options.mode !== ScatterChart.MODE.STATIC) {
            this.moveChart(dataBlock, nextRequestTime || 300);
            this.removeBubble();
        }
        this.changeSelectedAgent(this.selectedAgent);
    }
    private setCount(dataBlock: ScatterChartDataBlock): void {
        const to = dataBlock.getXRange().to;

        this.agentList.forEach((agent: string) => {
            const countObj = this.typeManager.getTypeNameList().reduce((acc: {[key: string]: number}, curr: string) => {
                return {...acc, [curr]: dataBlock.getCount(agent, curr)};
            }, {});

            this.countMap.has(agent) ? this.countMap.get(agent)[to] = countObj : this.countMap.set(agent, {[to]: countObj});
        });
    }
    private drawDataBlock(dataBlock: ScatterChartDataBlock): void {
        const prefix = this.options.prefix;

        this.rendererManager.makeDataCanvas(dataBlock, dataBlock.getAgentList());
        this.agentList.forEach((agent: string) => {
            if (this.sampleScatter) {
                const dataByAgent = dataBlock.getSampledData()[agent];

                if (!dataByAgent) {
                    return;
                }

                Object.entries(dataByAgent).forEach(([type, data]: [string, {x: number, y: number, count: number}[]]) => {
                    data.forEach((d: any) => {
                        this.rendererManager.drawTransactionWithSample(`${agent}-${prefix}-${type}`, this.typeManager.getColorByName(type), d);
                    });
                });
            } else {
                for (let i = 0, nLen = dataBlock.countByAgent(agent); i < nLen; i++) {
                    const data = dataBlock.getDataByAgentAndIndex(agent, i);
                    const groupCount = dataBlock.getGroupCount(data);

                    if (groupCount !== 0) {
                        const typeIndex = dataBlock.getTypeIndex(data);
                        const typeName = this.typeManager.getNameByIndex(typeIndex);
                        const typeColor = this.typeManager.getColorByIndex(typeIndex);
                        this.rendererManager.drawTransaction(`${agent}-${prefix}-${typeName}`, typeColor, data);
                    }
                }
            }
        });
    }
    private moveChart(dataBlock: ScatterChartDataBlock, nextRequestTime: number): void {
        const xRange = this.coordinateManager.getX();
        const dataBlockXRange = dataBlock.getXRange();
        const duration = this.options.animationDuration;

        if (dataBlockXRange.from >= xRange.to) {
            const moveXTime = dataBlockXRange.to - xRange.to;
            const moveXValue = moveXTime * this.coordinateManager.getPixelPerTime();
            this.coordinateManager.setX(xRange.from + moveXTime, xRange.to + moveXTime);
            this.axisRenderer.updateAxisValue(true, duration * 2);
            // this.rendererManager.moveChart(Math.floor(moveXValue), nextRequestTime < duration ? 0 : duration);
            this.outChangeRangeX.next({
                from: xRange.from + moveXTime,
                to: xRange.to + moveXTime
            });
        }
    }
    private removeBubble() {
        const fromX = this.coordinateManager.getX().from;

        this.countMap.forEach((v: {[key: number]: {[key: string]: number}}) => {
            Object.keys(v).forEach((to: string) => {
                if (Number(to) < fromX) {
                    delete v[Number(to)];
                }
            });
        });

        if (!this.enableServerSideScan) {
            for (let i = 0 ; i < this.dataBlocks.length ; i++) {
                const dataBlock = this.dataBlocks[i];
                if (dataBlock.getXRange().to < fromX) {
                    this.dataBlocks.shift();
                    i--;
                } else {
                    break;
                }
            }
        }
    }
    getDataByRange(fromX: number, toX: number, fromY: number, toY: number, selectedAgent: string, selectedType: string[]): any[] {
        fromX = +fromX;
        toX = +toX;
        fromY = +fromY;
        toY = +toY;

        const data = [];
        const yRange = this.coordinateManager.getY();
        const typeChecker: { [key: string]: boolean } = {};
        selectedType.forEach((type: string) => {
            typeChecker[type] = true;
        });
        for (let i = 0 ; i < this.dataBlocks.length ; i++) {
            const dataBlock = this.dataBlocks[i];
            for (let j = 0, len = dataBlock.getTotalCount(); j < len ; j++) {
                const xRange = dataBlock.getXRange();
                if ( xRange.to < fromX || xRange.from > toX ) {
                    continue;
                }
                const transactionData = dataBlock.getDataByIndex(j);
                const agentName =  dataBlock.getAgentName(transactionData);
                if (this.isSelectedAgent(agentName)) {
                    const x = dataBlock.getX(transactionData);
                    const y = dataBlock.getY(transactionData);

                    if (this.isInRange(fromX, toX, x)) {
                        if (this.isInRange(fromY, toY, y) || (y > toY && toY >= yRange.to) || (y < fromY && fromY <= yRange.from)) {
                            if (typeChecker[this.typeManager.getNameByIndex(dataBlock.getTypeIndex(transactionData))] === true) {
                                data.push([dataBlock.getTransactionID(transactionData), x, y]);
                            }
                        }
                    }
                }

            }
        }
        return data;
    }
    hasDataByXY(fromX: number, toX: number, fromY: number, toY: number): boolean {
        if (this.enableServerSideScan) {
            // TODO: Optimization the area
            return true;
        } else {
            fromX = +fromX;
            toX = +toX;
            fromY = +fromY;
            toY = +toY;

            const yRange = this.coordinateManager.getY();
            for (let i = 0 ; i < this.dataBlocks.length ; i++) {
                const dataBlock = this.dataBlocks[i];
                for (let j = 0, len = dataBlock.getTotalCount() ; j < len ; j++) {
                    const transactionData = dataBlock.getDataByIndex(j);
                    const agentName =  dataBlock.getAgentName(transactionData);
                    if (this.isSelectedAgent(agentName)) {
                        const x = dataBlock.getX(transactionData);
                        const y = dataBlock.getY(transactionData);
                        if (this.isInRange(fromX, toX, x)) {
                            if (this.isInRange(fromY, toY, y) || (y > toY && toY >= yRange.to) || (y < fromY && fromY <= yRange.from)) {
                                if (this.typeManager.isCheckedByIndex(dataBlock.getTypeIndex(transactionData))) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }
    }
    private isInRange(from: number, to: number, value: number): boolean {
        return value >= from && value <= to;
    }
    downloadChartAsImage(extension: string): void {
        const xRange = this.coordinateManager.getX();
        const titleArea = 60;
        const countArea = 40;
        const width = this.coordinateManager.getWidth();
        const height = this.coordinateManager.getHeight() + titleArea + countArea;

        const elementDownloadCanvas = document.createElement('canvas');
        elementDownloadCanvas.setAttribute('width', width + 'px');
        elementDownloadCanvas.setAttribute('height', height + 'px');

        const ctxDownloadCanvas = elementDownloadCanvas.getContext('2d');
        ctxDownloadCanvas.fillStyle = '#FFFFFF';
        ctxDownloadCanvas.fillRect(0, 0, width, height);

        this.gridRenderer.drawToCanvas(ctxDownloadCanvas, titleArea);
        this.axisRenderer.drawToCanvas(ctxDownloadCanvas, titleArea);
        this.rendererManager.drawToCanvas(ctxDownloadCanvas, titleArea);

        // draw Title
        ctxDownloadCanvas.textAlign = 'left';
        ctxDownloadCanvas.font = '24px monospace';
        ctxDownloadCanvas.fillText(this.application, 10, 10);
        ctxDownloadCanvas.font = '14px monospace';
        ctxDownloadCanvas.fillText(moment(xRange.from).tz(this.timezone).format(this.dateFormat[0] + ' ' + this.dateFormat[1]) + ' ~ ' + moment(xRange.to).tz(this.timezone).format(this.dateFormat[0] + ' ' + this.dateFormat[1]), 10, 40);

        // draw Count By Type
        this.typeManager.getTypeNameList().forEach((typeName: string, index: number) => {
            const color = this.typeManager.getColorByName(typeName);
            const check = this.typeManager.isCheckedByName(typeName);
            const count = this.getTransactionCount()[typeName];

            ctxDownloadCanvas.textAlign = 'left';
            ctxDownloadCanvas.font = '16px monospace';
            if (check) {
                ctxDownloadCanvas.fillStyle = color;
                ctxDownloadCanvas.fillRect((index * 200) + 10, height - countArea + 10, 20, 20);
            } else {
                ctxDownloadCanvas.lineWidth = 4;
                ctxDownloadCanvas.strokeStyle = color;
                ctxDownloadCanvas.strokeRect((index * 200) + 10, height - countArea + 10, 20, 20);
            }
            ctxDownloadCanvas.fillStyle = '#000';
            ctxDownloadCanvas.fillText(`${typeName} : ${count}`, (index * 200) + 40, height - countArea + 10);
        });

        this.downloadElement.setAttribute('href', elementDownloadCanvas.toDataURL('image/' + extension));
        this.downloadElement.setAttribute('download', `Pinpoint_Scatter_Chart[${moment(xRange.from).tz(this.timezone).format(this.dateFormat[0] + '_' + this.dateFormat[1])}~${moment(xRange.to).tz(this.timezone).format(this.dateFormat[0] + '_' + this.dateFormat[1])}].png`);
        this.downloadElement.dispatchEvent(new MouseEvent('click'));
    }
    changeYRange(newYRange: {from: number, to: number}):  void {
        this.coordinateManager.setY(+newYRange.from, +newYRange.to);
    }
    changeSelectedAgent(agentName: string): void {
        this.selectedAgent = agentName;
        this.rendererManager.showSelectedAgent(agentName);
        this.outTransactionCount.next(this.getTransactionCount());
    }
    getCurrentAgent(): string {
        return this.selectedAgent;
    }
    isEmpty(): boolean {
        if (this.options.mode === ScatterChart.MODE.STATIC) {
            return this.countMap.size === 0;
        } else {
            return false;
        }
    }
    getTypeManager(): ScatterChartTransactionTypeManager {
        return this.typeManager;
    }
    getVirtualGridManager(): ScatterChartVirtualGridManager {
        return this.virtualGridManager;
    }
    getCoordManager(): ScatterChartSizeCoordinateManager {
        return this.coordinateManager;
    }
    setTimezone(timezone: string): void {
        this.options.timezone = this.timezone = timezone;
    }
    setDateFormat(dateFormat: string[]): void {
        this.options.dateFormat = this.dateFormat = dateFormat;
    }
}
