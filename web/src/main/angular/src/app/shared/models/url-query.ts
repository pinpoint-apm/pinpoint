export interface IUrlQuery<T> {
    get(): T;
    equals(value: IUrlQuery<T>): boolean;
    toString(): string;
}

export class UrlQueryClass<T> implements IUrlQuery<T> {
    constructor(private value: T) {}
    equals(target: IUrlQuery<T>): boolean {
        if (target === null) {
            return false;
        }
        return this.value.toString() === target.toString();
    }
    get(): T {
        return this.value;
    }
    toString(): string {
        return this.value.toString();
    }
}

export class UrlQuery {
    static FILTER = 'filter';
    static HINT = 'hint';
    static BIDIRECTIONAL = 'bidirectional';
    static INBOUND = 'inbound';
    static OUTBOUND = 'outbound';
    static WAS_ONLY = 'wasOnly';
    static ACTIVE_ONLY = 'activeOnly';
    static DRAG_INFO = 'dragInfo';
    static TRANSACTION_INFO = 'transactionInfo';

    constructor() {}
    static getQueryList(): string[] {
        return [
            UrlQuery.FILTER,
            UrlQuery.HINT,
            UrlQuery.BIDIRECTIONAL,
            UrlQuery.INBOUND,
            UrlQuery.OUTBOUND,
            UrlQuery.WAS_ONLY,
            UrlQuery.ACTIVE_ONLY,
            UrlQuery.DRAG_INFO,
            UrlQuery.TRANSACTION_INFO
        ];
    }
}

export class UrlQueryFactory {
    constructor() {}
    static createQuery(queryName: string, queryValue: string): IUrlQuery<string | boolean | number> {
        switch (queryName) {
            case UrlQuery.INBOUND:
            case UrlQuery.OUTBOUND:
                return new UrlQueryClass<number>(Number(queryValue)) as IUrlQuery<number>;
            case UrlQuery.BIDIRECTIONAL:
            case UrlQuery.WAS_ONLY:
            case UrlQuery.ACTIVE_ONLY:
                return new UrlQueryClass<boolean>(queryValue === 'true') as IUrlQuery<boolean>;
            case UrlQuery.FILTER:
            case UrlQuery.HINT:
            case UrlQuery.DRAG_INFO:
            case UrlQuery.TRANSACTION_INFO:
                return new UrlQueryClass<string>(queryValue) as IUrlQuery<string>;
            default:
                return new UrlQueryClass<string>(queryValue) as IUrlQuery<string>;
        }
    }
}
