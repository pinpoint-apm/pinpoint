const ONE_HOUR = 60;
const ONE_DAY = 1440;
const MINUTE = 'm';
const HOUR = 'h';
const DAY = 'd';

export class Period {
    private viewValue: string;
    public static parseToMinute(time: string): number {
        const timeChar = time.substr(-1).toLowerCase();
        const yourTime = parseInt(time, 10);
        switch ( timeChar ) {
            case MINUTE:
                return yourTime;
            case HOUR:
                return yourTime * ONE_HOUR;
            case DAY:
                return yourTime * ONE_DAY;
            default:
                return yourTime;
        }
    }
    constructor(private minute: number, private prefix?: string, private postfix?: string) {
        this.calcuDisplay();
    }
    private calcuDisplay() {
        if (this.minute < ONE_HOUR ) {
            this.viewValue = this.minute + MINUTE;
        } else if (this.minute < ONE_DAY) {
            if ( this.minute % ONE_HOUR === 0 ) {
                this.viewValue = (this.minute / ONE_HOUR) + HOUR;
            } else {
                this.viewValue = this.minute + MINUTE;
            }
        } else {
            if ( this.minute % ONE_DAY === 0 ) {
                this.viewValue = (this.minute / ONE_DAY) + DAY;
            } else {
                this.viewValue = this.minute + MINUTE;
            }
        }
    }

    getValueWithTime(): string {
        return this.viewValue;
    }
    getValueWithAddedWords(): string {
        const prefix = this.prefix ? this.prefix + ' ' : '';
        const postfix = this.postfix ? this.postfix + ' ' : '';
        return prefix + this.getValueWithTime() + postfix;
    }
    getValue(): number {
        return this.minute;
    }
    getMiliSeconds(): number {
        return this.minute * 60 * 1000;
    }
    equalValue(target: number): boolean {
        return this.minute === target;
    }
    equals(target: Period): boolean {
        if ( target ) {
            return this.getValue() === target.getValue();
        } else {
            return false;
        }
    }
}
