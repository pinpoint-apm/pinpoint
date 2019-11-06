export class ServerMapNodeClickExtraParam {
    static INSTANCE_COUNT = 'instanceCount';
    static REQUEST_RED = 'red';
    static REQUEST_ORANGE = 'orange';
    static REQUEST_GREEN = 'green';
    constructor(private type: string) {}
    isInstanceCount(): boolean {
        return this.type === ServerMapNodeClickExtraParam.INSTANCE_COUNT;
    }
    isRed(): boolean {
        return this.type === ServerMapNodeClickExtraParam.REQUEST_RED;
    }
    isOrange(): boolean {
        return this.type === ServerMapNodeClickExtraParam.REQUEST_ORANGE;
    }
    isGreen(): boolean {
        return this.type === ServerMapNodeClickExtraParam.REQUEST_GREEN;
    }
}
