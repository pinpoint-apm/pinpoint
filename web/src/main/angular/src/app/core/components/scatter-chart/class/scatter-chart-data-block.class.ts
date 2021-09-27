import { isEmpty } from 'app/core/utils/util';
import { ScatterChartSizeCoordinateManager } from './scatter-chart-size-coordinate-manager.class';
import { ScatterChartTransactionTypeManager } from './scatter-chart-transaction-type-manager.class';
import { ScatterChartVirtualGridManager } from './scatter-chart-virtual-grid-manager.class';

export enum DataIndex {
    X,
    Y,
    META,
    TRANSACTION_ID,
    TYPE,
    GROUP_COUNT
}
export enum MetadataIndex {
    AGENT_NAME, TRANSACTION_PREFIX_1, TRANSACTION_PREFIX_2
}

export class ScatterChartDataBlock {
    private from: number;
    private to: number;

    private agentMetadata: { [key: string]: any[] };
    private transactionData: number[][];

    private agentList: string[] = [];
    private transactionDataByAgent: {[key: string]: any} = {};
    private dataByGrid: {[key: string]: any} = {};
    private sampledData: {[key: string]: any} = {}; // Could be formatted like number[][] but for now, keep the old format.
    private countByType: {[key: string]: {[key: string]: number}} = {};

    private fromX: number;
    private toX: number;

    // private worker = new Worker('../scatter-chart.worker', { type: 'module' });

    constructor(
        private originalData: IScatterData,
        private typeManager: ScatterChartTransactionTypeManager,
        private virtualGridManager: ScatterChartVirtualGridManager,
        private coordinateManager: ScatterChartSizeCoordinateManager
    ) {

        this.initVariable();
        this.initInnerDataStructure();
        this.classifyDataByAgent();
        this.sampleData();
    }

    private initVariable() {
        this.from = this.originalData.from;
        this.to = this.originalData.to;
        if (this.originalData.complete) {
            this.fromX = this.originalData.from;
            this.toX = this.originalData.to;
        } else {
            this.fromX = this.originalData.resultFrom;
            this.toX = this.originalData.resultTo;
        }
        this.agentMetadata = this.originalData.scatter.metadata;
        this.transactionData = [];
    }
    private initInnerDataStructure(): void {
        Object.keys(this.agentMetadata).forEach((key: string) => {
            const metaInfo = this.agentMetadata[key];
            const agentName = metaInfo[MetadataIndex.AGENT_NAME];
            this.transactionDataByAgent[agentName] = [];
            this.dataByGrid[agentName] = {};

            this.countByType[agentName] = {};
            this.typeManager.getTypeNameList().forEach((typeName: string) => {
                this.countByType[agentName][typeName] = 0;
            });
            if (this.agentList.indexOf(agentName) === -1) {
                this.agentList.push(agentName);
            }
        });
        this.agentList.sort();
    }
    private classifyDataByAgent(): void {
        // TODO: 이거 웹워커로 다시한번 트라이해보자.
        this.originalData.scatter.dotList.forEach((tData: number[]) => {
            const agentName = this.getAgentName(tData);
            const typeName = this.typeManager.getNameByIndex(tData[DataIndex.TYPE]);
            const tNewData = tData.concat();

            tNewData[DataIndex.X] += this.from;
            this.transactionData.push(tNewData);
            this.transactionDataByAgent[agentName].push(tNewData);
            this.countByType[agentName][typeName]++;

            /**
             * this.transactionData = [dot1, dot2, dot3, ...]
             * this.transactionDataByAgent = {agent1: [dot1, dot2, ...], agent2: [dot3, dot4, ...]} <- 실제로 요거가지고 그림.
             * this.countByType = {agent1: {success: 103, fail: 100}, agent2: {success: 102, fail: 100}}
             */
            if (tData[DataIndex.GROUP_COUNT] === 0) {
                return;
            }

            // Determine which grid each dot is belonged to in the virtual-grid
            const {x0, y0, x1, y1} = this.virtualGridManager.getGrid(tNewData); // {x0: 1, y0: 2, x1: 3, y1: 4}
            const gridKey = `${x0}-${y0}-${x1}-${y1}`;

            // {agent1: {[`${x1}-${x2}-${y1}-${y2}`]: {success: [dot1, dot2, ...], fail: [dot3, dot4, ...]}, ... }, agent2: ...}
            if (this.dataByGrid[agentName][gridKey]) {
                this.dataByGrid[agentName][gridKey][typeName] ? this.dataByGrid[agentName][gridKey][typeName].push(tNewData) : this.dataByGrid[agentName][gridKey][typeName] = [tNewData];
            } else {
                this.dataByGrid[agentName][gridKey] = {};
                this.dataByGrid[agentName][gridKey][typeName] = [tNewData];
            }
        });
    }

    private sampleData(): void {
        // this.sampledData = {agent1: {success: [{x, y, count}, {x, y, count}...], fail: [{x, y, count}, {x, y, count}...]}, agent2: [{x, y, count}, {x, y, count}], ...}
        this.sampledData = Object.entries(this.dataByGrid).reduce((dataByAgent: any, [agent, dataByGrid]: [string, any]) => {
            // dataByGrid: {[`${x1}-${x2}-${y1}-${y2}`]: {success: [dot1, dot2, ...], fail: [dot3, dot4, ...]}, ... }
            return {...dataByAgent, [agent]: Object.values(dataByGrid).reduce((dataByType: any, dotData: any) => {
                // dotData: {success: [dot1, dot2, ...], fail: [dot3, dot4, ...]}
                const sampledData = Object.entries(dotData).reduce((sampled: {[key: string]: any}, [type, dotList]: [string, number[][]]) => {
                    const d = dotList.reduce((a: any, dot: number[], i: number) => {
                        const {x, y} = this.coordinateManager.getCoord(dot);
                        const count = dot[DataIndex.GROUP_COUNT];

                        return {
                            x: (x + a.x * i) / (i + 1),
                            y: (y + a.y * i) / (i + 1),
                            count: count + a.count
                        };
                    }, {x: 0, y: 0, count: 0});

                    // return {...sampled, [type]: d};
                    return {...sampled, [type]: {x: Math.round(d.x), y: Math.round(d.y), count: d.count}};
                }, {});

                // sampledData : {success: {x: 0, y: 0, count: 0}, fail: {x: 1, y: 1, count: 1}}
                Object.keys(sampledData).forEach((type: string) => {
                    (isEmpty(dataByType) || !dataByType[type]) ? dataByType[type] = [sampledData[type]] : dataByType[type].push(sampledData[type]);
                });

                return dataByType;
            }, {})};
        }, {} as any);
    }

    getSampledData(): any {
        return this.sampledData;
    }

    getAgentName(data: number[] | string): string {
        if (typeof data === 'string') {
            return this.agentMetadata[data][MetadataIndex.AGENT_NAME];
        } else {
            return this.agentMetadata[data[DataIndex.META]][MetadataIndex.AGENT_NAME];
        }
    }
    getDataByAgentAndIndex(agent: string, index: number): number[] {
        return this.transactionDataByAgent[agent][index];
    }
    getGroupCount(data: number[]): number {
        return data[DataIndex.GROUP_COUNT];
    }
    getDataByIndex(index: number): number[] {
        return this.transactionData[index];
    }
    getTotalCount(): number {
        return this.transactionData.length;
    }
    countByAgent(agent: string): number {
        if (this.transactionDataByAgent[agent]) {
            return this.transactionDataByAgent[agent].length;
        } else {
            return 0;
        }
    }
    getCount(agentName: string, type: string, fromX?: number, toX?: number): number {
        if (fromX && toX) {
            return this.getCountOfRange(agentName, type, fromX, toX);
        } else {
            if (agentName === '') {
                let sumType = 0;
                Object.keys(this.countByType).forEach((innerAgentName: string) => {
                    sumType += this.countByType[innerAgentName][type];
                });
                return sumType;
            } else {
                if (this.countByType[agentName]) {
                    return this.countByType[agentName][type];
                } else {
                    return 0;
                }
            }
        }
    }
    private getCountOfRange(agentName: string, type: string, fromX: number, toX: number): number {
        // @TODO: agentName : ALL 인 경우 처리
        let sum = 0;
        const length = this.transactionDataByAgent[agentName].length;
        if (this.fromX >= toX || this.toX <= fromX || length === 0 || (agentName in this.countByType) === false) {
            return sum;
        }
        if (this.fromX >= fromX && this.toX <= toX) {
            return this.getCount(agentName, type);
        }
        for (let i = 0 ; i < length ; i++) {
            const data = this.transactionDataByAgent[agentName][i];
            if (data[DataIndex.X] < fromX) {
                break;
            }
            if (type === this.typeManager.getNameByIndex(data[DataIndex.TYPE])) {
                if (data[DataIndex.X] <= toX) {
                    sum++;
                }
            }
        }
        return sum;
    }
    getTransactionID(data: number[]): string {
        const oMeta = this.agentMetadata[data[DataIndex.META]];
        return `${oMeta[MetadataIndex.TRANSACTION_PREFIX_1]}^${oMeta[MetadataIndex.TRANSACTION_PREFIX_2]}^${data[DataIndex.TRANSACTION_ID]}`;
    }
    getX(data: number[]): number {
        return data[DataIndex.X];
    }
    getY(data: number[]): number {
        return data[DataIndex.Y];
    }
    getTypeIndex(data: number[]): number {
        return data[DataIndex.TYPE];
    }
    getXRange(): {from: number, to: number} {
        return {
            'from': this.fromX,
            'to': this.toX
        };
    }
    isEmpty(): boolean {
        return this.transactionData.length === 0;
    }
    getAgentList(): string[] {
        return this.agentList;
    }
}

