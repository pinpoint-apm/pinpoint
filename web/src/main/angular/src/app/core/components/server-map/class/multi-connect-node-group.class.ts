import { NodeGroup } from './node-group.class';

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
    nodeData: INodeInfo;
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
            'category': this.groupType,
            'mergedNodes': [],
            'serviceType': this.groupType,
            'isWas': false,
            'isAuthorized': false,
            'isMerged': true,
            'instanceCount': 0,
            'topCountNodes': [],
            'applicationName': '',
            'mergedSourceNodes': []
        };
    }
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

    getNodeGroupData(): INodeInfo {
        this.nodeData.mergedSourceNodes = this.subNodeGroup;
        return super.getNodeGroupData();
    }
}
