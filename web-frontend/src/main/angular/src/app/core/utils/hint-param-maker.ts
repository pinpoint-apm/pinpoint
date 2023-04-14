import { isEmpty } from 'app/core/utils/util';

interface IUrlFormat {
    [key: string]: any[];
}
interface IServerFormat {
    [key: string]: {
        rpc: string,
        rpcServiceTypeCode: number
    }[];
}
export class HintParamMaker {
    static makeParam(currHintStr: string, addedHint: IServerFormat): IUrlFormat {
        const currHint = currHintStr ? JSON.parse(currHintStr) : {};

        if (addedHint) {
            if (isEmpty(currHint)) {
                return HintParamMaker.makeUrlFormat(addedHint);
            } else {
                const currHintOnServerFormat = HintParamMaker.makeServerFormat(currHint);
                const mergedFormat = HintParamMaker.mergeHint(currHintOnServerFormat, addedHint);

                return HintParamMaker.makeUrlFormat(mergedFormat);
            }
        } else {
            return currHint;
        }
    }

    /**
     * addedHint: {
     *   app1: [{rpc: dasd, rpcServiceTypeCode: 1234}, {rpc: asdsaf, rpcServiceTypeCode: 5678}]
     *   app2: [{rpc: dasd, rpcServiceTypeCode: 1234}, {rpc: asdsaf, rpcServiceTypeCode: 5678}]
     * }
     *
     * return format
     * {
     *   app1: [dsad, 1234, asdsaf, 5678],
     *   app2: [dsad, 1234, asdsaf, 5678]
     * }
     * */
    static makeUrlFormat(addedHint: IServerFormat): IUrlFormat {
        return Object.entries(addedHint).reduce((acc: IUrlFormat, [key, value]: [string, {[key: string]: any}[]]) => {
            return {...acc, [key]: value.map((obj: {[key: string]: any}) => Object.values(obj)).flat()};
        }, {} as IUrlFormat);
    }

    /**
     * urlHint: {
     *   app1: [dasd, 1234, asdsaf, 5678],
     *   app2: [dasd, 1234, asdsaf, 5678]
     * }
     *
     * return format
     * {
     *   app1: [{rpc: dasd, rpcCode: 1234}, {rpc: asdsaf, rpcCode: 5678}]
     *   app2: [{rpc: dasd, rpcCode: 1234}, {rpc: asdsaf, rpcCode: 5678}]
     * }
     */
    static makeServerFormat(urlHint: IUrlFormat): IServerFormat {
        return Object.entries(urlHint).reduce((acc: IServerFormat, [key, value]: [string, any[]]) => {
            return {...acc, [key]: value.reduce((acc2: {[key: string]: any}[], curr: any, i: number) => {
                return i % 2 ? (acc2[acc2.length - 1].rpcServiceTypeCode = curr, acc2) : [...acc2, {rpc: curr}];
            }, [])};
        }, {} as IServerFormat);
    }

    static mergeHint(currHint: IServerFormat, addedHint: IServerFormat): IServerFormat {
        const mergedKeys = [...new Set<string>([...Object.keys(currHint), ...Object.keys(addedHint)])];
        const uniqueKeys = mergedKeys.filter((key: string) => !(currHint.hasOwnProperty(key) && addedHint.hasOwnProperty(key)));

        return mergedKeys.reduce((acc: IServerFormat, key: string) => {
            return {
                ...acc,
                [key]: uniqueKeys.includes(key)
                    ? currHint[key] || addedHint[key]
                    : [...currHint[key], ...addedHint[key]].filter(({rpc: rpc1, rpcServiceTypeCode: rpcCode1}, i: number, arr: {[key: string]: any}[]) => {
                        return !arr.slice(0, i).some(({rpc: rpc2, rpcServiceCode: rpcCode2}) => rpc1 === rpc2 && rpcCode1 === rpcCode2);
                    })
            };
        }, {} as IServerFormat);
    }
}
