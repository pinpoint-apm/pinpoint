import { Application, Period, EndTime } from 'app/core/models';

export interface IUrlPathId<T> {
    get(): T;
    equals(value: IUrlPathId<T>): boolean;
    toString(): string;
}

export class UrlApplication implements IUrlPathId<IApplication> {
    constructor(private application: IApplication) {}
    equals(value: IUrlPathId<IApplication>): boolean {
        if (value === null) {
            return false;
        }
        return this.application.equals(value.get());
    }
    get(): IApplication {
        return this.application;
    }
    toString(): string {
        return this.application.toString();
    }
}
export class UrlPeriod implements IUrlPathId<Period> {
    constructor(private period: Period) {}
    equals(value: IUrlPathId<Period>): boolean {
        if (value === null) {
            return false;
        }
        return this.period.equals(value.get());
    }
    get(): Period {
        return this.period;
    }
    toString(): string {
        return this.period.toString();
    }
}
export class UrlEndTime implements IUrlPathId<EndTime> {
    constructor(private endTime: EndTime) {}
    equals(value: IUrlPathId<EndTime>): boolean {
        if (value === null) {
            return false;
        }
        return this.endTime.equals(value.get());
    }
    get(): EndTime {
        return this.endTime;
    }
    toString(): string {
        return this.endTime.toString();
    }
}
export class UrlGeneral<T> implements IUrlPathId<T> {
    constructor(private value: T) {}
    equals(target: IUrlPathId<T>): boolean {
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

export class UrlPathId {
    static APPLICATION = 'application';
    static PERIOD = 'period';
    static END_TIME = 'endTime';
    // static FILTER = 'filter';
    // static HINT = 'hint';
    static REAL_TIME = 'realtime';
    static AGENT_ID = 'agentId';
    static TRANSACTION_INFO = 'transactionInfo';
    static TRACE_ID = 'traceId';
    static FOCUS_TIMESTAMP = 'focusTimestamp';
    static SPAN_ID = 'spanId';
    static PAGE = 'page';
    static GENERAL = 'general';
    static FAVORITE = 'favorite';
    static USER_GROUP = 'userGroup';
    static ALARM = 'alarm';
    static INSTALLATION = 'installation';
    static HELP = 'help';
    static CHART_MANAGER = 'chartManager';
    static AGENT_STATISTIC = 'agentStatistic';
    static AGENT_MANAGEMENT = 'agentManagement';

    constructor() {}
    static getPathIdList(): string[] {
        return [
            UrlPathId.AGENT_ID,
            UrlPathId.APPLICATION,
            UrlPathId.END_TIME,
            // UrlPathId.FILTER,
            UrlPathId.FOCUS_TIMESTAMP,
            // UrlPathId.HINT,
            UrlPathId.PAGE,
            UrlPathId.PERIOD,
            UrlPathId.REAL_TIME,
            UrlPathId.SPAN_ID,
            UrlPathId.TRACE_ID,
            UrlPathId.TRANSACTION_INFO,
            UrlPathId.AGENT_STATISTIC,
            UrlPathId.AGENT_MANAGEMENT
        ];
    }
}

export class UrlPathIdFactory {
    constructor() {}
    static createPath(paramName: string, paramValue: string): IUrlPathId<any> {
        switch (paramName) {
            case UrlPathId.APPLICATION:
                const params = paramValue.split('@');
                return new UrlApplication(new Application(params[0], params[1], 0)) as IUrlPathId<IApplication>;
            case UrlPathId.PERIOD:
                return new UrlPeriod(new Period(Period.parseToMinute(paramValue))) as IUrlPathId<Period>;
            case UrlPathId.END_TIME:
                return new UrlEndTime(new EndTime(paramValue)) as IUrlPathId<EndTime>;
            case UrlPathId.PAGE:
                return new UrlGeneral(Number(paramValue)) as IUrlPathId<number>;
            // case UrlPathId.FILTER:
            // case UrlPathId.HINT:
            case UrlPathId.REAL_TIME:
            case UrlPathId.AGENT_ID:
            case UrlPathId.TRANSACTION_INFO:
            case UrlPathId.TRACE_ID:
            case UrlPathId.FOCUS_TIMESTAMP:
            case UrlPathId.SPAN_ID:
            case UrlPathId.AGENT_STATISTIC:
            case UrlPathId.AGENT_MANAGEMENT:
                return new UrlGeneral(paramValue) as IUrlPathId<string>;
            default:
                return new UrlGeneral(paramValue) as IUrlPathId<string>;
        }
    }
}
