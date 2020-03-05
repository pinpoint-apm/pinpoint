import { IShortNodeInfo } from './server-map-data.class';
// interface ShortCutNode {
//     key: string;
//     category: string;
//     serviceType: string;
//     mergedNodes: any[];
//     instanceCount: number;
//     isAuthorized: boolean;
//     serviceTypeCode: string;
//     topCountNodes: any[];
//     applicationName: string;
// }

const SPECIAL_STR = {
    KEY_SEPERATOR: '^',
    SEPERATOR: '_',
    NAME_PREFIX: 'MERGE',
    GROUP_POSTFIX: 'GROUP'
};
export class NodeGroup {
    TOP_LIST_MAX_COUNT = 3;
    applicationName: string;
    groupKey: string;
    groupType: string;
    nodeData: IShortNodeInfo;
    static isGroupKey(key: string): boolean {
        return new RegExp(
            '.*' +
            SPECIAL_STR.GROUP_POSTFIX +
            '\\' + SPECIAL_STR.KEY_SEPERATOR +
            SPECIAL_STR.NAME_PREFIX +
            '\\' + SPECIAL_STR.SEPERATOR +
            '\\' + 'd{7}' +
            '.*',
            'g'
        ).test(key);
    }
    constructor(protected type: string) {
        this.init();
    }
    init() {
        this.applicationName = SPECIAL_STR.NAME_PREFIX + SPECIAL_STR.SEPERATOR + this.randomValue();
        this.groupType = this.type + SPECIAL_STR.SEPERATOR + SPECIAL_STR.GROUP_POSTFIX;
        this.groupKey = this.groupType + SPECIAL_STR.KEY_SEPERATOR + this.applicationName;
        this.initTemplateSet();
    }
    protected initTemplateSet() {
        this.nodeData = {
            'key': this.groupKey,
            'category': this.groupType,
            'mergedNodes': [],
            'serviceType': this.groupType,
            'isWas': false,
            'isAuthorized': true,
            'instanceCount': 0,
            'topCountNodes': [],
            'serviceTypeCode': '',
            'applicationName': this.applicationName
        };
    }
    protected randomValue(): string {
        return Math.random().toString().slice(2, 9);
    }
    protected setTopCountNodes(): void {
        this.nodeData['topCountNodes'].length = 0;
        this.nodeData['topCountNodes'].push({
            'applicationName': `Total: ${this.nodeData['mergedNodes'].length}`,
            'totalCount': this.nodeData['mergedNodes'].reduce((preValue, nowNode) => { return preValue + nowNode.totalCount; }, 0),
            'tableHeader': true
        });
        for (let i = 0; i < Math.min(this.nodeData['mergedNodes'].length, this.TOP_LIST_MAX_COUNT); i++) {
            this.nodeData['topCountNodes'].push(this.nodeData['mergedNodes'][i]);
        }
        if (this.nodeData['mergedNodes'].length > this.TOP_LIST_MAX_COUNT) {
            this.nodeData['topCountNodes'].push({
                'applicationName': '...',
                'totalCount': ''
            });
        }
    }
    addNodeData(node: IShortNodeInfo): void {
        delete node.category;
        this.nodeData['instanceCount'] += node.instanceCount;
        this.nodeData['mergedNodes'].push(node);
    }
    sortNodeData(): NodeGroup {
        this.nodeData['mergedNodes'].sort((v1: IShortNodeInfo, v2: IShortNodeInfo) => {
            return v2.totalCount - v1.totalCount;
        });
        return this;
    }
    getGroupKey(): string {
        return this.groupKey;
    }
    getType(): string {
        return this.type;
    }
    getGroupServiceType(): string {
        return this.groupType;
    }
    getNodeGroupData(): IShortNodeInfo {
        this.setTopCountNodes();
        return this.nodeData;
    }
}
