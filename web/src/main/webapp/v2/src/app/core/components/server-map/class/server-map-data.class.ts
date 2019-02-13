import { NodeGroup } from './node-group.class';
import { LinkGroup } from './link-group.class';
import { MultiConnectNodeGroup } from './multi-connect-node-group.class';
import { Filter } from 'app/core/models';

export class ServerMapData {
    private nodeList: { [key: string]: any }[];
    private linkList: { [key: string]: any }[];
    private usableNodePropertyInServerMap: string[] = [
        'key',
        'isWas',
        'isQueue',
        'category',
        'hasAlert',
        'slowCount',
        'histogram',
        'errorCount',
        'totalCount',
        'serviceType',
        'isAuthorized',
        'instanceCount',
        'applicationName'
    ];
    private usableLinkPropertyInServerMap: string[] = [
        'to',
        'key',
        'from',
        'hasAlert',
        'slowCount',
        'targetInfo',
        'totalCount',
        'sourceInfo',
        'errorCount'
    ];
    private mergeableServiceType: { [key: string]: boolean } = {};
    private canNotMergeableServiceTypeList: string[] = ['USER'];
    private countMap: { [key: string]: any } = {};
    private nodeMap: { [key: string]: any } = {};
    private linkMap: { [key: string]: any } = {};
    private mergeStateMap: { [key: string]: boolean } = {};
    private groupServiceTypeMap: { [key: string]: boolean } = {};
    private originalNodeMap: { [key: string]: INodeInfo } = {};
    private originalLinkMap: { [key: string]: ILinkInfo } = {};

    constructor(
        private originalNodeList: INodeInfo[],
        private originalLinkList: ILinkInfo[],
        private filters?: Filter[]) {
        this.init();
    }
    reset(originalNodeList: INodeInfo[], originalLinkList: ILinkInfo[]) {
        this.originalNodeList = originalNodeList;
        this.originalLinkList = originalLinkList;
        this.init();
    }
    private init() {
        this.convertToMap();
        this.extractUsableData();
        this.extractInboundAndOutboundCountOfEachNode();
        this.extractServiceTypeWhichCanMerge();
        // option set merge
        // if ( showMergedStatus ) {
        this.mergeGroup();
        this.mergeMultiLinkGroup();
        this.addFilterFlag();
        // }
    }
    private convertToMap(): void {
        console.time('convertTimeToMapFromList');
        this.originalNodeList.forEach((value: INodeInfo) => {
            this.originalNodeMap[value.key] = value;
        });
        this.originalLinkList.forEach((value: ILinkInfo) => {
            this.originalLinkMap[value.key] = value;
        });
        console.timeEnd('convertTimeToMapFromList');
    }
    private extractUsableData(): void {
        this.nodeList = this.extractData(this.originalNodeList, this.usableNodePropertyInServerMap, this.nodeMap);
        this.linkList = this.extractData(this.originalLinkList, this.usableLinkPropertyInServerMap, this.linkMap);
    }
    // extract necessary data from source data.
    private extractData(dataList: any[], keys: string[], map: any): any[] {
        const necessaryData = [];
        dataList.forEach((data) => {
            const oNew = {};
            keys.forEach((key) => {
                oNew[key] = data[key];
            });
            necessaryData.push(oNew);
            map[oNew['key']] = oNew;
        });
        return necessaryData;
    }
    /**
     * collect in and out node count of each node
     */
    private extractInboundAndOutboundCountOfEachNode(): void {
        this.nodeList.forEach((node: any) => {
            this.countMap[node.key] = {
                inCount: 0,
                outCount: 0
            };
        });
        this.linkList.forEach((link: any) => {
            this.countMap[link.to].inCount++;
            this.countMap[link.from].outCount++;
        });
    }
    // extract serviceType list which can merge from source data.
    private extractServiceTypeWhichCanMerge() {
        this.mergeableServiceType = {};
        this.nodeList.forEach((node) => {
            if (this.canMergeType(node)) {
                this.mergeableServiceType[node.serviceType] = true;
            }
        });
    }
    private canMergeType(nodeData: any): boolean {
        return nodeData.isWas === false && this.canNotMergeableServiceTypeList.indexOf(nodeData.serviceType) === -1;
    }
    private isRootNode(key: string) {
        return this.countMap[key].inCount === 0;
    }
    private isLeafNode(key: string) {
        return this.countMap[key].outCount === 0;
    }
    private mergeGroup(): void {
        console.time('mergeGroup()');
        // from 이 동일한 servcieType을 하나의 데이터로 묶음.
        const collectMergeLink = {};
        this.linkList.forEach((link) => {
            if (this.hasMergeableNode(link) === false) {
                return;
            }
            if ((link.from in collectMergeLink) === false) {
                collectMergeLink[link.from] = {};
            }
            if ((link.targetInfo.serviceType in collectMergeLink[link.from]) === false) {
                collectMergeLink[link.from][link.targetInfo.serviceType] = {
                    relatedLink: []
                };
            }
            // same from-key and same service-type
            collectMergeLink[link.from][link.targetInfo.serviceType].relatedLink.push(link);
        });
        const removeNodeKeys = {};
        const removeLinkKeys = {};
        for (const nodeKey in collectMergeLink) {
            if (collectMergeLink.hasOwnProperty(nodeKey)) {
                for (const type in collectMergeLink[nodeKey]) {
                    if (collectMergeLink[nodeKey][type].relatedLink.length < 2 || this.mergeStateMap[type] === false) {
                        continue;
                    }
                    const nodeGroup = new NodeGroup(type);
                    const linkGroup = new LinkGroup(nodeKey, nodeGroup.getGroupKey());

                    collectMergeLink[nodeKey][type].relatedLink.forEach((link) => {
                        nodeGroup.addNodeData(this.nodeMap[link.to]);
                        linkGroup.addLinkData(link);
                        removeNodeKeys[link.to] = true; // link.to is key of target node.
                        removeLinkKeys[link.key] = true;
                        delete this.nodeMap[link.to];
                        delete this.linkMap[link.key];
                    });
                    this.countMap[nodeGroup.getGroupKey()] = {
                        inCount: 1,
                        outCount: 0
                    };
                    this.nodeList.push(nodeGroup.sortNodeData().getNodeGroupData());
                    this.linkList.push(linkGroup.sortLinkData().getLinkGroupData());
                    this.groupServiceTypeMap[nodeGroup.getGroupServiceType()] = true;
                    this.mergeStateMap[type] = true;
                }
            }
        }
        this.removeByKey(this.nodeList, removeNodeKeys);
        this.removeByKey(this.linkList, removeLinkKeys);
        console.timeEnd('mergeGroup()');
    }
    private hasMergeableNode(link: any): boolean {
        if (this.mergeableServiceType[link.targetInfo.serviceType] !== true) {
            return false;
        }
        if (this.countMap[link.to].inCount !== 1) {
            return false;
        }
        if (this.isLeafNode(link.to) === false) {
            return false;
        }
        return true;
    }
    setMergeState({name, state}: IServerMapMergeState): void {
        this.mergeStateMap[name] = state;
    }
    resetMergeState(): void {
        this.extractUsableData();
        this.mergeGroup();
        this.mergeMultiLinkGroup();
    }
    removeByKey(data: any, removeList: any) {
        const removeIndex: Array<number> = [];
        data.forEach((thing, index) => {
            if (removeList[thing.key] === true) {
                removeIndex.push(index);
            }
        });
        removeIndex.sort(function (v1, v2) {
            return v1 - v2;
        });
        for (let i = removeIndex.length - 1; i >= 0; i--) {
            data.splice(removeIndex[i], 1);
        }
    }
    mergeMultiLinkGroup(): void {
        console.time('mergeMultiLinkGroup()');
        // 일단 두번째 병합 조건에 해당하는 노드들을 추림
        const targetNodeList = this.getMergeTargetNodes();
        const checkedNodes = {};
        const removeNodeKeys = {};
        const removeLinkKeys = {};

        targetNodeList.forEach((outerNode) => {
            const outerLoopNodeKey = outerNode.key;
            if (checkedNodes[outerLoopNodeKey] === true) {
                return;
            }
            checkedNodes[outerLoopNodeKey] = true;
            const outerLoopNodeFromKeys: Array<string> = this.getFromNodeKeys(outerLoopNodeKey);
            const mergeTargetLinks: any = {};
            const mergeTargetNodeList: Array<any> = [];

            targetNodeList.forEach((innerNode) => {
                const innerLoopNodeKey = innerNode.key;
                if (checkedNodes[innerLoopNodeKey] === true) {
                    return;
                }
                if (outerNode.serviceType !== innerNode.serviceType) {
                    return;
                }
                const innerLoopNodeFromKeys: Array<string> = this.getFromNodeKeys(innerLoopNodeKey);
                if (this.hasSameNodeList(outerLoopNodeFromKeys, innerLoopNodeFromKeys) === false) {
                    return;
                }
                checkedNodes[innerLoopNodeKey] = true;

                this.extractConnectLink(mergeTargetLinks, innerLoopNodeFromKeys, innerLoopNodeKey);
                mergeTargetNodeList.push(innerNode);
            });
            if (mergeTargetNodeList.length > 0) {
                this.extractConnectLink(mergeTargetLinks, outerLoopNodeFromKeys, outerLoopNodeKey);
                mergeTargetNodeList.push(outerNode);

                const multiConnectNodeGroup = new MultiConnectNodeGroup(outerNode.serviceType);
                mergeTargetNodeList.forEach((node) => {
                    multiConnectNodeGroup.addNodeData(node);
                    removeNodeKeys[node.key] = true;
                    delete this.nodeMap[node.key];
                });

                for (const fromKey in mergeTargetLinks) {
                    if (mergeTargetLinks.hasOwnProperty(fromKey)) {
                        multiConnectNodeGroup.addSubNodeGroup(fromKey);
                        const linkGroup = new LinkGroup(fromKey, multiConnectNodeGroup.getGroupKey());
                        for (let i = 0; i < mergeTargetLinks[fromKey].length; i++) {
                            const link = mergeTargetLinks[fromKey][i];
                            multiConnectNodeGroup.addSubNodeGroupData(fromKey, link);
                            linkGroup.addLinkData(link);
                            removeLinkKeys[link.key] = true;
                            delete this.linkMap[link.key];
                        }
                        this.linkList.push(linkGroup.sortLinkData().getLinkGroupData());
                    }
                }
                this.nodeList.push(multiConnectNodeGroup.sortNodeData().getNodeGroupData());
                this.groupServiceTypeMap[multiConnectNodeGroup.getGroupServiceType()] = true;
            }
        });
        this.removeByKey(this.nodeList, removeNodeKeys);
        this.removeByKey(this.linkList, removeLinkKeys);
        console.timeEnd('mergeMultiLinkGroup()');
    }
    private extractConnectLink(mergeTargetLinks: any, fromKeys: Array<string>, key: string): void {
        fromKeys.forEach((fromKey) => {
            if ((fromKey in mergeTargetLinks) === false) {
                mergeTargetLinks[fromKey] = [];
            }
            mergeTargetLinks[fromKey].push(this.linkMap[fromKey + '~' + key]);
        });
    }
    private getMergeTargetNodes(): any {
        const targetNodeList = [];
        this.nodeList.forEach((node) => {
            if (this.countMap[node.key].outCount > 0) {
                return;
            }
            if (this.countMap[node.key].inCount < 2) {
                return;
            }
            targetNodeList.push(node);
        });
        return targetNodeList;
    }
    private hasSameNodeList(firstNodeList: Array<string>, secondNodeList: Array<string>): boolean {
        if (firstNodeList.length !== secondNodeList.length) {
            return false;
        }
        for (let i = 0; i < firstNodeList.length; i++) {
            if (secondNodeList.indexOf(firstNodeList[i]) === -1) {
                return false;
            }
        }
        return true;
    }
    private getFromNodeKeys(nodeKey: string): any {
        const fromNodeKeys = [];
        this.linkList.forEach((link) => {
            if (link.to === nodeKey) {
                fromNodeKeys.push(link.from);
            }
        });
        return fromNodeKeys;
    }
    getNodeList(): { [key: string]: any }[] {
        return this.nodeList;
    }
    getLinkList(): { [key: string]: any }[] {
        return this.linkList;
    }
    getGroupTypes(): Array<string> {
        const types: Array<string> = [];
        for (const type in this.groupServiceTypeMap) {
            if (this.groupServiceTypeMap.hasOwnProperty(type)) {
                types.push(type);
            }
        }
        return types;
    }
    getMergeState(): any {
        return Object.keys(this.mergeStateMap).reduce((accumulator, key) => {
            accumulator[key] = this.mergeStateMap[key];
            return accumulator;
        }, {});
    }
    getMergedNodeData(key: string): any {
        return this.nodeList.find((node: {[key: string]: any}) => node.key === key);
    }
    getMergedLinkData(key: string): any {
        return this.linkList.find((link: {[key: string]: any}) => link.key === key);
    }
    getNodeData(key: string): INodeInfo {
        return this.originalNodeMap[key];
    }
    getLinkData(key: string): ILinkInfo {
        return this.originalLinkMap[key];
    }
    addFilterFlag(): void {
        if (this.filters) {
            // this.nodeList.forEach((node: any) => {
            //     this.filters.forEach((filter: Filter) => {
            //         if ( filter.getFromKey() === node.key || filter.getToKey() === node.key ) {
            //             node['isFiltered'] = true;
            //             return;
            //         }
            //     });
            // });
            this.linkList.forEach((link: any) => {
                this.filters.forEach((filter: Filter) => {
                    if (filter.getFromKey() === link.from && filter.getToKey() === link.to) {
                        link['isFiltered'] = true;
                        return;
                    }
                });
            });
        }
    }
    getNodeCount(): number {
        return this.nodeList.length;
    }
}
