import * as moment from 'moment-timezone';
import { Subject, BehaviorSubject, Observable } from 'rxjs';
import { ScatterChartSizeCoordinateManager } from './scatter-chart-size-coordinate-manager.class';
import { ScatterChartRendererManager } from './scatter-chart-renderer-manager.class';
import { ScatterChartDataBlock } from './scatter-chart-data-block.class';
import { ScatterChartGridRenderer } from './scatter-chart-grid-renderer.class';
import { ScatterChartAxisRenderer } from './scatter-chart-axis-renderer.class';
import { ScatterChartTransactionTypeManager } from './scatter-chart-transaction-type-manager.class';
import { ScatterChartMouseManager } from './scatter-chart-mouse-manager.class';

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
    private typeManager: ScatterChartTransactionTypeManager;
    private gridRenderer: ScatterChartGridRenderer;
    private axisRenderer: ScatterChartAxisRenderer;
    private coordinateManager: ScatterChartSizeCoordinateManager;
    private rendererManager: ScatterChartRendererManager;
    private mouseManager: ScatterChartMouseManager;
    private dataBlocks: ScatterChartDataBlock[] = [];
    private agentList: string[] = [];
    private selectedAgent = '';

    private downloadElement: HTMLElement;

    private outTransactionCount: BehaviorSubject<{[key: string]: {count: number, checked: boolean}}>;
    private outSelect: Subject<any> = new Subject();
    private outError: Subject<any> = new Subject();
    private outChangeRangeX: Subject<{from: number, to: number}> = new Subject();
    onSelect$: Observable<any>;
    onError$: Observable<any>;
    onChangeRangeX$: Observable<{from: number, to: number}>;
    onChangeTransactionCount$: Observable<{[key: string]: {count: number, checked: boolean}}>;

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
        private dateFormat: string[]
    ) {
        this.downloadElement = document.createElement('a');
        this.selectedAgent = agent;
        this.setOptions();
        this.initManagers();

        this.onSelect$ = this.outSelect.asObservable();
        this.onError$ = this.outError.asObservable();
        this.onChangeRangeX$ = this.outChangeRangeX.asObservable();
        this.outTransactionCount = new BehaviorSubject(this.getTransactionCount(true));
        this.onChangeTransactionCount$ = this.outTransactionCount.asObservable();
    }
    private isAllowedAgent(selectedAgent: string, agent: string): boolean {
        return selectedAgent === '' || selectedAgent === agent;
    }
    private getTransactionCount(isInit: boolean) {
        const count: {[key: string]: {count: number, checked: boolean}} = {};
        const xRange = this.coordinateManager.getX();
        // if (isInit) {
            this.typeManager.getTypeNameList().forEach((typeName: string) => {
                count[typeName] = {
                    count: 0,
                    checked: this.typeManager.isCheckedByName(typeName)
                };
            });
        if (isInit === false) {
            this.typeManager.getTypeNameList().forEach((typeName: string) => {
                count[typeName].count = 0;
                this.dataBlocks.forEach((dataBlock: ScatterChartDataBlock) => {
                    const dataBlockXRange = dataBlock.getXRange();
                    if (dataBlockXRange.to >= xRange.from) {
                        const agentList = dataBlock.getAgentList();
                        agentList.forEach((agentName: string) => {
                            if (this.isAllowedAgent(this.selectedAgent, agentName)) {
                                count[typeName].count += dataBlock.getCount(agentName, typeName, xRange.from, xRange.to);
                            }
                        });
                    }
                });
            });
        }
        return count;
    }
    private setOptions() {
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
            lineColor: '#3D3D3D',
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
                strokeStyle : '#e3e3e3'
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
        this.gridRenderer = new ScatterChartGridRenderer(this.options, this.coordinateManager, this.element);
        this.axisRenderer = new ScatterChartAxisRenderer(this.options, this.coordinateManager, this.element);
        this.rendererManager = new ScatterChartRendererManager(this.options, this.coordinateManager, this.element, this.typeManager);
        this.mouseManager = new ScatterChartMouseManager(this.options, this.coordinateManager, this.element);
        this.mouseManager.onDragArea$.subscribe((area: any) => {
            const fromX = this.coordinateManager.parseMouseXToXData(area.x.from);
            const toX = this.coordinateManager.parseMouseXToXData(area.x.to);
            const fromY = this.coordinateManager.parseMouseYToYData(area.y.from);
            const toY = this.coordinateManager.parseMouseYToYData(area.y.to);
            if (this.hasDataByXY(fromX, toX, fromY, toY)) {
                this.outSelect.next({
                    x: {
                        from: fromX,
                        to: toX
                    },
                    y: {
                        from: fromY,
                        to: toY
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
    private redrawDataBlock(): void {
        this.dataBlocks.forEach((dataBlock: ScatterChartDataBlock) => {
            this.drawDataBlock(dataBlock);
        });
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

        this.dataBlocks = [];
        this.agentList = [];
        this.axisRenderer.reset();
        this.rendererManager.reset();
        if (typeCheck) {
            this.changeShowType(typeCheck);
        }
        this.outTransactionCount.next(this.getTransactionCount(true));
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
        this.dataBlocks.push(dataBlock);
        // if (this._bPause === true) return;
        this.drawDataBlock(dataBlock);
        if (this.options.mode !== ScatterChart.MODE.STATIC) {
            this.moveChart(dataBlock, nextRequestTime || 300);
            this.removeBubble();
        }
        this.changeSelectedAgent(this.selectedAgent);
    }
    private drawDataBlock(dataBlock: ScatterChartDataBlock): void {
        const prefix = this.options.prefix;
        this.rendererManager.makeDataCanvas(dataBlock, dataBlock.getAgentList());
        this.agentList.forEach((agentName: string) => {
            for (let i = 0, nLen = dataBlock.countByAgent(agentName) ; i < nLen ; i++) {
                const data = dataBlock.getDataByAgentAndIndex(agentName, i);
                const groupCount = dataBlock.getGroupCount(data);
                if (groupCount !== 0) {
                    const typeIndex = dataBlock.getTypeIndex(data);
                    const typeName = this.typeManager.getNameByIndex(typeIndex);
                    const typeColor = this.typeManager.getColorByIndex(typeIndex);
                    this.rendererManager.drawTransaction(`${agentName}-${prefix}-${typeName}`, typeColor, data);
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
            this.rendererManager.moveChart(Math.floor(moveXValue), nextRequestTime < duration ? 0 : duration);
            this.outChangeRangeX.next({
                from: xRange.from + moveXTime,
                to: xRange.to + moveXTime
            });
        }
    }
    private removeBubble() {
        const fromX = this.coordinateManager.getX().from;
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
                if (this.isAllowedAgent(selectedAgent, agentName)) {
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
                if (this.isAllowedAgent(this.selectedAgent, agentName)) {
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
    private isInRange(from: number, to: number, value: number): boolean {
        return value >= from && value <= to;
    }
    // //destroy = function() {
    // //	var self = this;
    // //	this._unbindAllEvents();
    // //	//this._empty();
    // //	$.each(this, function (property, content) {
    // //		delete self[property];
    // //	});
    // //	this._bDestroied = true;
    // //
    // //};
    // //_empty = function() {
    // //	this._$elContainer.empty();
    // //};
    // //_unbindAllEvents = function() {
    // //	// this is for drag-selecting. it should be unbinded.
    // //	jQuery(document).unbind('mousemove').unbind('mouseup');
    // //};
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
            let sumType = 0;
            this.dataBlocks.forEach((dataBlock: ScatterChartDataBlock) => {
                sumType += dataBlock.getCount(this.selectedAgent, typeName);
            });
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
            ctxDownloadCanvas.fillText(`${typeName} : ${sumType}`, (index * 200) + 40, height - countArea + 10);
        });

        this.downloadElement.setAttribute('href', elementDownloadCanvas.toDataURL('image/' + extension));
        this.downloadElement.setAttribute('download', `Pinpoint_Scatter_Chart[${moment(xRange.from).tz(this.timezone).format(this.dateFormat[0] + '_' + this.dateFormat[1])}~${moment(xRange.to).tz(this.timezone).format(this.dateFormat[0] + '_' + this.dateFormat[1])}].png`);
        this.downloadElement.dispatchEvent(new MouseEvent('click'));
    }
    // _hideServerError() {
    //     this._$elContainer.css({
    //         'backgroundImage': 'none'
    //     });
    // }
    // _showServerError() {
    //     this._$elContainer.css({
    //         'backgroundImage': 'url(' + this.option('errorImage') + ')',
    //         'backgroundRepeat': 'no-repeat',
    //         'backgroundPosition': '88% 21%',
    //         'backgroundSize': '30px 30px'
    //     });
    // }
    redraw() {
        this.rendererManager.clear();
        this.axisRenderer.updateAxisValue();
        this.redrawDataBlock();
    }
    // abort() {
    //     this._bPause = true;
    //     if (this._oDataLoadManager) {
    //         this._oDataLoadManager.abort();
    //     }
    // }
    changeYRange(newYRange: {from: number, to: number}):  void {
        this.fromY = +newYRange.from;
        this.toY = +newYRange.to;
        this.coordinateManager.setY(+newYRange.from, +newYRange.to);
        this.redraw();
    }
    changeSelectedAgent(agentName: string): void {
        this.selectedAgent = agentName;
        this.rendererManager.showSelectedAgent(agentName);
        this.outTransactionCount.next(this.getTransactionCount(false));
    }
    getCurrentAgent(): string {
        return this.selectedAgent;
    }
    isEmpty(): boolean {
        if (this.options.mode === ScatterChart.MODE.STATIC) {
            let empty = true;
            this.dataBlocks.forEach((dataBlock: ScatterChartDataBlock) => {
                empty = empty && dataBlock.isEmpty();
            });
            return empty;
        } else {
            return false;
        }
    }
    getTypeManager(): ScatterChartTransactionTypeManager {
        return this.typeManager;
    }
    setTimezone(timezone: string): void {
        this.options.timezone = this.timezone = timezone;
    }
    setDateFormat(dateFormat: string[]): void {
        this.options.dateFormat = this.dateFormat = dateFormat;
    }
}
