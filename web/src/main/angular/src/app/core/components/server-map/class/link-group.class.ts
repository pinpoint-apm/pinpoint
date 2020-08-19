export class LinkGroup {
    SEPERATOR = '~';
    linkData: any;
    constructor(private fromNodeKey: string) {
        this.init();
    }
    init() {
        this.linkData = {
            'key': '',
            'from': this.fromNodeKey,
            'to': '',
            'hasAlert': false,
            'isMerged': true,
            'slowCount': 0,
            'histogram': {},
            'sourceInfo': {},
            'targetInfo': [],
            'totalCount': 0,
            'errorCount': 0
        };
    }
    addLinkData(link: any): void {
        this.linkData.totalCount += link.totalCount;
        this.linkData.errorCount += link.errorCount;
        this.linkData.slowCount += link.slowCount;
        this.linkData.sourceInfo = link.sourceInfo;
        if (link.hasAlert) {
            this.linkData.hasAlert = link.hasAlert;
        }
        this.linkData['targetInfo'].push(link);
    }
    sortLinkData(): LinkGroup {
        this.linkData['targetInfo'].sort((v1, v2) => {
            return v2.totalCount - v1.totalCount;
        });
        return this;
    }
    getLinkGroupData(): any {
        return this.linkData;
    }

    setGroupKey(toNodeKey: string): void {
        this.linkData.to = toNodeKey;
        this.linkData.key = `${this.linkData.from}${this.SEPERATOR}${this.linkData.to}`;
    }
}
