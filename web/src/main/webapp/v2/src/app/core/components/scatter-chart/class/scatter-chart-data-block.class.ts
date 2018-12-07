import { ScatterChartTransactionTypeManager } from './scatter-chart-transaction-type-manager.class';

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
    private countByType: {[key: string]: {[key: string]: number}} = {};

    private fromX: number;
    private toX: number;
    /*
        oPropertyIndex : {
            x: 0,
            y: 1,
            meta: 2,
            transactionId: 3,
            type: 4,
            groupCount: 5
        }
    */
    constructor(private originalData: IScatterData, private typeManager: ScatterChartTransactionTypeManager) {
        this.initVariable();
        this.initInnerDataStructure();
        this.classifyDataByAgent();
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
        this.originalData.scatter.dotList.forEach((tData: number[]) => {
            const agentName = this.getAgentName(tData);
            const typeName = this.typeManager.getNameByIndex(tData[DataIndex.TYPE]);
            const tNewData = tData.concat();

            tNewData[DataIndex.X] += this.from;
            this.transactionData.push(tNewData);
            this.transactionDataByAgent[agentName].push(tNewData);
            this.countByType[agentName][typeName]++;
        });
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
