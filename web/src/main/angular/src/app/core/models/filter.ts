export const enum ResponseRange {
    MIN = 0,
    MAX = 30000
}

const enum FilterType {
    NODE,
    LINK
}

export class Filter {
    // paramName = fa
    fromApplication = '';
    // paramName = fst
    fromServiceType = '';
    // paramName = a
    application = '';
    // paramName = st
    serviceType = '';
    // paramName = ta
    toApplication = '';
    // paramName = tst
    toServiceType = '';
    // paramName = ie
    transactionResult: null | boolean = null;
    // paramName = rf
    responseFrom?: number;
    // paramName = rt
    responseTo?: number;
    // paramName = url
    urlPattern?: string;
    // paramName = fan
    fromAgentName?: string;
    // paramName = tan
    toAgentName?: string;
    // paramName = an
    agentName?: string;
    // node or link
    type: FilterType;

    static instanceFromString(str: string): Filter[] {
        const returnFilter: Filter[] = [];
        let aFilterFromStr: any;
        try {
            aFilterFromStr = JSON.parse(str);
        } catch (exception) {
            return returnFilter;
        }

        for (let i = 0; i < aFilterFromStr.length; i++) {
            const filterFromStr = aFilterFromStr[i];
            const newFilter = new Filter(
                filterFromStr.fa,
                filterFromStr.fst,
                filterFromStr.ta,
                filterFromStr.tst,
                filterFromStr.ie,
                filterFromStr.a,
                filterFromStr.st,
                filterFromStr.rf,
                filterFromStr.rt === 'max' ? ResponseRange.MAX : filterFromStr.rt
            );
            if (filterFromStr.url) {
                newFilter.setUrlPattern(window.atob(filterFromStr.url));
            }
            if (filterFromStr.fan) {
                newFilter.setFromAgentName(filterFromStr.fan);
            }
            if (filterFromStr.tan) {
                newFilter.setToAgentName(filterFromStr.tan);
            }
            returnFilter.push(newFilter);
        }
        return returnFilter;
    }

    constructor(fa: string, fst: string, ta: string, tst: string, ie: null | boolean = null, a: string = null, st: string = null, rf = ResponseRange.MIN, rt = ResponseRange.MAX) {
        this.fromApplication = fa;
        this.fromServiceType = fst;
        this.toApplication = ta;
        this.toServiceType = tst;
        this.transactionResult = ie;
        this.application = a;
        this.serviceType = st;
        this.responseFrom = rf;
        this.responseTo = rt;
        this.type = this.getFilterType();
    }

    private getFilterType(): FilterType {
        return this.application === null && this.serviceType === null
            ? FilterType.LINK
            : FilterType.NODE;
    }

    equal(filter: Filter): boolean {
        return this.type !== filter.type ? false
            : filter.type === FilterType.NODE ? this.application === filter.application && this.serviceType === filter.serviceType
            : this.fromApplication === filter.fromApplication &&
            this.fromServiceType === filter.fromServiceType &&
            this.toApplication === filter.toApplication &&
            this.toServiceType === filter.toServiceType;
    }

    setUrlPattern(url: string): void {
        this.urlPattern = url;
    }

    setFromAgentName(fan: string): void {
        this.fromAgentName = fan;
    }

    setToAgentName(tan: string): void {
        this.toAgentName = tan;
    }

    setAgentName(an: string): void {
        this.agentName = an;
    }

    getToKey(): string {
        return `${this.toApplication}^${this.toServiceType}`;
    }

    getFromKey(): string {
        return `${this.fromApplication}^${this.fromServiceType}`;
    }

    getTransactionResultStr(): string {
        if (this.transactionResult === true) {
            return 'Failed Only';
        } else if (this.transactionResult === false) {
            return 'Success Only';
        }
        return 'Success + Failed';
    }

    toParamFormat(): {[key: string]: any} {
        const param: {[key: string]: any} = {
            fa: this.fromApplication,
            fst: this.fromServiceType,
            ta: this.toApplication,
            tst: this.toServiceType,
            a: this.application,
            st: this.serviceType,
            ie: this.transactionResult,
            rf: this.responseFrom,
            rt: this.responseTo === ResponseRange.MAX ? 'max' : this.responseTo
        };
        if (this.urlPattern) {
            param['url'] = window.btoa(this.urlPattern);
        }
        if (this.fromAgentName) {
            param['fan'] = this.fromAgentName;
        }
        if (this.toAgentName) {
            param['tan'] = this.toAgentName;
        }
        if (this.agentName) {
            param['an'] = this.agentName;
        }
        return param;
    }
}
