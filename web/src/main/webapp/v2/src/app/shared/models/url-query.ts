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
    static BIDIRECTIONAL = 'bidirectional';
    static INBOUND = 'inbound';
    static OUTBOUND = 'outbound';
    static WAS_ONLY = 'wasOnly';

    constructor() {}
    static getQueryList(): string[] {
        return [
            UrlQuery.BIDIRECTIONAL,
            UrlQuery.INBOUND,
            UrlQuery.OUTBOUND,
            UrlQuery.WAS_ONLY,
        ];
    }
}

export class UrlQueryFactory {
    constructor() {}
    static createQuery<T>(queryName: string, queryValue?: T): IUrlQuery<T> {
        switch (queryName) {
            case UrlQuery.INBOUND:
            case UrlQuery.OUTBOUND:
                return new UrlQueryClass<T>(queryValue) as IUrlQuery<T>;
            case UrlQuery.BIDIRECTIONAL:
            case UrlQuery.WAS_ONLY:
                return new UrlQueryClass<T>(queryValue) as IUrlQuery<T>;
            default:
                return new UrlQueryClass<T>(queryValue) as IUrlQuery<T>;
        }
    }
}
