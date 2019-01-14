/*
    from server
    filterTargetRpcList : [{
        rpc: string,
        rpcServiceTypeCode: number
    }, ...]
    > URL string size를 줄이기 위해 다음과 같이 변경 함.
    url
    {
        [toNode.applicationName] : [ rpc, rpcServiceTypeCode, rpc, rpcServiceTypeCode ... ],
        ...
    }
*/
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
    static makeParam(currentHint: string, addedHint: IServerFormat): string {
        if (addedHint) {
            const parsedCurrntHint = JSON.parse(currentHint || '{}');
            const currentHintKeys = Object.keys(parsedCurrntHint);

            if (currentHintKeys.length === 0) {
                return '/' + encodeURIComponent(JSON.stringify(HintParamMaker.makeToUrlFormatFromServerFormat(addedHint)));
            } else {
                const urlFormatOfCurrentHint = HintParamMaker.makeToServerFormatFromUrlFormat(parsedCurrntHint);
                const mergedFormat = HintParamMaker.mergeFormat(urlFormatOfCurrentHint, addedHint);
                return '/' + encodeURIComponent(JSON.stringify(HintParamMaker.makeToUrlFormatFromServerFormat(mergedFormat)));
            }
        } else {
            return '/' + currentHint;
        }
    }
    static makeToUrlFormatFromServerFormat(addedHint: IServerFormat): IUrlFormat {
        const addedHintKeys = Object.keys(addedHint);
        const urlFormat: IUrlFormat  = {};
        addedHintKeys.forEach((key: string) => {
            urlFormat[key] = addedHint[key].reduce((acc: any, data: any) => {
                acc.push(data.rpc, data.rpcServiceTypeCode);
                return acc;
            }, []);
        });
        return urlFormat;
    }
    static makeToServerFormatFromUrlFormat(urlHint: IUrlFormat): IServerFormat {
        const newFormat: IServerFormat = {};
        const urlHintKeys = Object.keys(urlHint);
        urlHintKeys.forEach((key: string) => {
            const urlHintValue = urlHint[key];
            const value = [];
            for (let i = 0 ; i < urlHintValue.length ; i = i + 2) {
                value.push({
                    rpc: urlHintValue[i],
                    rpcServiceTypeCode: urlHintValue[i + 1]
                });
            }
            newFormat[key] = value;
        });
        return newFormat;
    }
    static mergeFormat(urlFormat: IServerFormat, addedFormat: IServerFormat): IServerFormat {
        const mergedFormat: IServerFormat = {};
        const urlFormatKeys = Object.keys(urlFormat);
        const addedFormatKeys = Object.keys(addedFormat);

        urlFormatKeys.forEach((key: string) => {
            mergedFormat[key] = urlFormat[key];
        });
        addedFormatKeys.forEach((key: string) => {
            if (mergedFormat[key]) {
                mergedFormat[key] = mergedFormat[key].concat(addedFormat[key]);
                const m = mergedFormat[key];
                for (let i = 0 ; i < m.length ; i++) {
                    for (let j = i + 1 ; j < m.length ; j++ ) {
                        if (m[i].rpc === m[j].rpc && m[i].rpcServiceTypeCode === m[j].rpcServiceTypeCode) {
                            m.splice(j, 1);
                            j--;
                        }
                    }
                }
            } else {
                mergedFormat[key] = addedFormat[key];
            }
        });
        return mergedFormat;
    }
}
