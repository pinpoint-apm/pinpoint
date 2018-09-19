import * as moment from 'moment';

const DATE_TIME_FORMAT = 'YYYY-MM-DD-HH-mm-ss';
export class EndTime {
    public static formatDate(time: number): string {
        return moment(time).format(DATE_TIME_FORMAT);
    }
    public static newByNumber(time: number): EndTime {
        return new EndTime(EndTime.formatDate(time));
    }
    constructor(private endTimeStr: string) {}
    getEndTime(): string {
        return this.endTimeStr;
    }
    getDate(): Date {
        return moment(this.endTimeStr, DATE_TIME_FORMAT).toDate();
    }
    getMilliSecond(): number {
        return this.getDate().valueOf();
    }
    calcuStartTime(minute: number): EndTime {
        return new EndTime(moment(this.endTimeStr, DATE_TIME_FORMAT).subtract(minute, 'minutes').format(DATE_TIME_FORMAT));
    }
    calcuNextTime(minute: number): EndTime {
        return new EndTime(moment(this.endTimeStr, DATE_TIME_FORMAT).add(minute, 'minutes').format(DATE_TIME_FORMAT));
    }
    equals(target: EndTime) {
        if ( target ) {
            return this.endTimeStr === target.getEndTime();
        } else {
            return false;
        }
    }
}
