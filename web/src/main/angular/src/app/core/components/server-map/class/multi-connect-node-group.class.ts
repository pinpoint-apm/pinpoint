import { NodeGroup } from './node-group.class';
import { IShortNodeInfo } from './server-map-data.class';

export class MultiConnectNodeGroup extends NodeGroup {
    // KEY_SEPERATOR: string = '^';
    // SEPERATOR: string = '_';
    NAME_PREFIX = 'MULTI_MERGE';
    // GROUP_POSTFIX: string = 'GROUP';
    // TOP_LIST_MAX_COUNT: number = 3;
    // applicationName: string;
    // groupKey: string;
    // groupType: string;
    // nodeData: any;
    nodeData: IShortNodeInfo;
    subNodeGroup: Array<any>;
    subNodeGroupMap: any;
    constructor(protected type: string) {
        super(type);
        // this.init();
    }
    init() {
        super.init();
        // this.applicationName = this.NAME_PREFIX + this.SEPERATOR + this.randomValue();
        // this.groupType = this.type + this.SEPERATOR + this.GROUP_POSTFIX;
        // this.groupKey = this.groupType + this.KEY_SEPERATOR + this.applicationName;
        this.subNodeGroup = [];
        this.subNodeGroupMap = {};
    }
    initTemplateSet() {
        this.nodeData = {
            'key': this.groupKey,
            'isWas': false,
            'category': this.groupType,
            'serviceType': this.groupType,
            'mergedNodes': [],
            'isAuthorized': false,
            'instanceCount': 0,
            'topCountNodes': [],
            'applicationName': this.applicationName,
            'mergedSourceNodes': []
        };
    }
    // private randomValue(): string {
    //     return Math.floor(Math.random() * 10000000).toString();
    // }
    // private setTopCountNodes(): void {
    //     this.nodeData['topCountNodes'].length = 0;
    //     this.nodeData['topCountNodes'].push({
    //         'applicationName': `Total : ${this.nodeData['mergedNodes'].length}`,
    //         'totalCount': this.nodeData['mergedNodes'].reduce((preValue, nowNode) => { return preValue + nowNode.totalCount; }, 0),
    //         'tableHeader': true
    //     });
    //     for (let i = 0 ; i < Math.min(this.nodeData['mergedNodes'].length, this.TOP_LIST_MAX_COUNT) ; i++ ) {
    //         this.nodeData['topCountNodes'].push(this.nodeData['mergedNodes'][i]);
    //     }
    //     if ( this.nodeData['mergedNodes'].length > this.TOP_LIST_MAX_COUNT ) {
    //        this.nodeData['topCountNodes'].push({
    //             'applicationName': '...',
    //             'totalCount': ''
    //         });
    //     }
    // }
    // addNodeData(node: any): void {
    //     delete node.category;
    //     this.nodeData['instanceCount'] += node.instanceCount;
    //     this.nodeData['mergedNodes'].push(node);
    // }
    addSubNodeGroup(key: string): void {
        const subNodeGroup = {
            group: [],
            isLast: false,
            applicationName: key
        };
        this.subNodeGroupMap[key] = subNodeGroup;
        this.subNodeGroup.push(subNodeGroup);
    }
    addSubNodeGroupData(key: string, link: any): void {
        this.subNodeGroupMap[key].group.push({
            key: link.to,
            hasAlert: link.hasAlert || false,
            totalCount: link.totalCount,
            serviceType: link.serviceType,
            applicationName: link.targetInfo.applicationName
        });
    }
    sortNodeData(): MultiConnectNodeGroup {
        function fnSort(v1, v2) {
            return v2.totalCount - v1.totalCount;
        }
        this.nodeData['mergedNodes'].sort(fnSort);
        this.subNodeGroup.sort(fnSort);
        this.subNodeGroup.forEach((subGroup) => {
            subGroup['group'].sort(fnSort);
        });
        return this;
    }
    // getGroupKey(): string{
    //     return this.groupKey;
    // }
    // getGroupServiceType(): string {
    //     return this.groupType;
    // }

    getNodeGroupData(): IShortNodeInfo {
        this.nodeData.mergedSourceNodes = this.subNodeGroup;
        return super.getNodeGroupData();
    }
}
