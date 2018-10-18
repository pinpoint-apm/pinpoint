import * as moment from 'moment-timezone';

export class TimelinePositionManager {
    static X_AXIS_TICKS = 5;

    width: number;
    startTime: number;
    endTime: number;
    timePerPoint: number;
    xAxisTicks: number;
    pointingTime: number;
    pointingPosition: number;
    minTimeRange: number;
    maxSelectionTimeRange;
    aSelectionTimeRange: number[] = [];
    aSelectionPosition: number[] = [];

    constructor(private options: any) {
        this.width = options.width;
        this.minTimeRange = options.minTimeRange;
        this.maxSelectionTimeRange = options.maxSelectionTimeRange;

        this.initInnerVar();
        this.setTimelineRange(options.timelineRange[0], options.timelineRange[1]);
        this.initSelectionTimeRange(options.selectionTimeRange || []);
        this.resetSelectionByTime();
        this.xAxisTicks = options.xAxisTicks || TimelinePositionManager.X_AXIS_TICKS;
        this.setPointingTime( options.pointingTime );
    }
    initInnerVar(): void {
        this.aSelectionTimeRange = [];
        this.aSelectionPosition = [];
    }
    setTimelineRange(start: number, end: number): void {
        this.startTime = start;
        this.endTime = end;
        this.calcuTimePerPoint();
    }
    initSelectionTimeRange(aTime: number[]): void {
        if ( aTime.length !== 2 ) {
            aTime = [ this.startTime, this.endTime ];
        }
        if ( aTime[1] - aTime[0] > this.maxSelectionTimeRange ) {
            aTime[0] = aTime[1] - this.maxSelectionTimeRange;
        }
        this.setSelectionTimeRange(aTime[0], aTime[1]);
    }
    resetSelectionByTime(): void {
        this.setSelectionPosition(this.getPositionByTime(this.aSelectionTimeRange[0]), this.getPositionByTime(this.aSelectionTimeRange[1]));
    }

    calcuTimePerPoint(): void {
        this.timePerPoint = ( this.endTime - this.startTime ) / this.width;
    }
    setSelectionTimeRange(start: number, end: number): void {
        this.aSelectionTimeRange[0] = start === null ? this.aSelectionTimeRange[0] : start;
        this.aSelectionTimeRange[1] = end === null ? this.aSelectionTimeRange[1] : end;
    }
    setSelectionPosition(start: number, end: number): void {
        this.aSelectionPosition[0] = start === null ? this.aSelectionPosition[0] : start;
        this.aSelectionPosition[1] = end === null ? this.aSelectionPosition[1] : end;
    }
    getPositionByTime(time: number): number {
        return Math.floor((time - this.startTime) / this.timePerPoint);
    }
    setPointingTime(time: number ): void {
        this.pointingTime = time;
        this.pointingPosition = this.getPositionByTime(time);
    }
    isInMaxSelectionTimeRange(start: number, end: number): boolean {
        return (end - start) <= this.maxSelectionTimeRange;
    }
    getNewSelectionTimeRangeFromStart(start: number): number[] {
        return [start, start + this.maxSelectionTimeRange];
    }
    getNewSelectionTimeRangeFromEnd(end: number): number[] {
        return [end - this.maxSelectionTimeRange, end];
    }
    isBeforeSliderStartTime(time: number): boolean {
        return time < this.startTime;
    }
    isAfterSliderEndTime(time: number): boolean {
        return time > this.endTime;
    }
    getTimelineEndTime(): number {
        return this.endTime;
    }
    setWidth(width: number): void {
        this.width = width;
        this.calcuTimePerPoint();
        this.reset();
    }
    getTimelineEndPosition(): number {
        return this.width;
    }
    getTimelineStartTimeStr(): string {
        return this.formatDate(new Date(this.startTime));
    }
    getTimelineEndTimeStr(): string {
        return this.formatDate( new Date(this.endTime) );
    }
    getFullTimeStr(x: number): string {
        return this.formatDate( new Date(this.getTimeFromPosition(x)) );
    }
    formatDate(d: Date): string {
        return moment(d).tz(this.options.timezone).format(this.options.dateFormat[0]);
    }
    isInSelectionZone(): boolean {
        return (this.pointingTime >= this.aSelectionTimeRange[0] && this.pointingTime <= this.aSelectionTimeRange[1] ) ? true : false;
    }
    isInTimelineRange(time: number): boolean {
        return (time >= this.startTime && time <= this.endTime ) ? true : false;
    }
    getTimelineRange(): number[] {
        return [this.startTime, this.endTime];
    }
    getSelectionTimeRange(): number[] {
        return [this.aSelectionTimeRange[0], this.aSelectionTimeRange[1]];
    }
    getSelectionPosition(): number[] {
        return [this.aSelectionPosition[0], this.aSelectionPosition[1]];
    }
    getPointingPosition(): number {
        return this.pointingPosition;
    }
    getPointingTime(): number {
        return this.pointingTime;
    }
    getPrevTime(): number {
        const gap = this.aSelectionTimeRange[1] - this.aSelectionTimeRange[0];
        return this.aSelectionTimeRange[0] - Math.floor(gap / 2) - 1;
    }
    getNextTime(): number {
        const gap = this.aSelectionTimeRange[1] - this.aSelectionTimeRange[0];
        const nextTime = this.aSelectionTimeRange[1] + Math.floor(gap / 2) + 1;
        if ( nextTime > Date.now() ) {
            return Date.now();
        } else {
            return nextTime;
        }
    }
    getTimeFromPosition(x: number): number {
        return this.startTime + Math.floor(this.timePerPoint * x);
    }
    calcuSelectionZone(): void {
        const currentSelectionSize = this.aSelectionTimeRange[1] - this.aSelectionTimeRange[0];
        const currentSelectionHalfSize = Math.round( currentSelectionSize / 2 );
        let selectionStart = this.pointingTime - currentSelectionHalfSize;
        let selectionEnd = this.pointingTime + currentSelectionHalfSize;
        if ( selectionStart < this.startTime ) {
            selectionEnd = selectionStart + currentSelectionSize;
            selectionStart = this.startTime;
        } else if ( selectionEnd > this.endTime ) {
            selectionStart = this.endTime - currentSelectionSize;
            selectionEnd = this.endTime;
        }
        this.setSelectionTimeRange(selectionStart, selectionEnd);
        this.setSelectionPosition(this.getPositionByTime(selectionStart), this.getPositionByTime(selectionEnd));
    }
    getXAxisPositionData(): any {
        const max = TimelinePositionManager.X_AXIS_TICKS + 1;
        const space = Math.floor(this.width / max);
        const a = [];
        for ( let i = 0 ; i < max ; i++ ) {
            if ( i === 0 ) {
                continue;
            }
            const x = i * space;
            a.push( {
                x: x,
                time: this.getTimeStr(x)
            });
        }
        return a;
    }
    getTimeStr(x: number): string {
        const timeX = Math.floor( x * this.timePerPoint ) + this.startTime;
        return moment(new Date(timeX)).tz(this.options.timezone).format(this.options.dateFormat[1]) + ' ' + moment(new Date(timeX)).tz(this.options.timezone).format(this.options.dateFormat[2]);
    }
    setSelectionStartTime(time: number): void {
        this.setSelectionTimeRange(time, null);
        this.setSelectionPosition(this.getPositionByTime(time), null);
    }
    setSelectionEndTime(time: number): void {
        this.setSelectionTimeRange( null, time );
        this.setSelectionPosition( null, this.getPositionByTime( time ) );
    }
    setSelectionStartPosition(x: number): void {
        this.setSelectionTimeRange(this.getTimeFromPosition(x), null);
        this.setSelectionPosition(x, null);
    }
    setSelectionEndPosition(x: number): void {
        this.setSelectionTimeRange(null, this.getTimeFromPosition(x));
        this.setSelectionPosition(null, x);
    }
    zoomIn(): void {
        // 선택 영역 중심으로 확대
        if ( this.startTime === this.aSelectionTimeRange[0] && this.endTime === this.aSelectionTimeRange[1] ) {
            return;
        }
        const quarterTimeline = Math.floor((this.endTime - this.startTime) / 4);
        let tempStartTime = this.pointingTime - quarterTimeline;
        let tempEndTime = this.pointingTime + quarterTimeline;

        if ( tempEndTime - tempStartTime < this.minTimeRange ) {
            const minHalf = Math.floor(this.minTimeRange / 2);
            tempStartTime = this.pointingTime - minHalf;
            tempEndTime = this.pointingTime + minHalf;
        }
        let gap;
        if ( this.aSelectionTimeRange[0] < tempStartTime ) {
            gap = tempStartTime - this.aSelectionTimeRange[0];
            const tempSelectionEndTime = (this.aSelectionTimeRange[1] + gap > tempEndTime) ? tempEndTime : this.aSelectionTimeRange[1] + gap;
            this.setSelectionTimeRange(tempStartTime, tempSelectionEndTime);
        }
        if ( this.aSelectionTimeRange[1] > tempEndTime ) {
            gap = this.aSelectionTimeRange[1] - tempEndTime;
            const tempSelectionStartTime = (this.aSelectionTimeRange[0] - gap < tempStartTime) ? tempStartTime : this.aSelectionTimeRange[0] - gap;
            this.setSelectionTimeRange(tempSelectionStartTime, tempEndTime);
        }
        this.setTimelineRange(tempStartTime, tempEndTime);
        this.reset();
    }
    zoomOut(): void {
        const one = this.endTime - this.startTime;
        const tempCenterTime = this.aSelectionTimeRange[0] + Math.floor((this.aSelectionTimeRange[1] - this.aSelectionTimeRange[0] ) / 2);
        this.setTimelineRange(tempCenterTime - one, tempCenterTime + one);
        this.reset();
    }
    resetBySelectTime(time: number, bIsNow: boolean): void {
        const halfSliderTimeRange = Math.floor((this.endTime - this.startTime) / 2);
        const halfSelectionTimeRange = Math.floor((this.aSelectionTimeRange[1] - this.aSelectionTimeRange[0] ) / 2);
        if ( bIsNow === true ) {
            this.setTimelineRange(time - halfSliderTimeRange * 2, time);
            this.setSelectionTimeRange(time - halfSelectionTimeRange * 2, time);
        } else {
            this.setTimelineRange(time - halfSliderTimeRange, time + halfSliderTimeRange);
            this.setSelectionTimeRange(time - halfSelectionTimeRange, time + halfSelectionTimeRange);
        }
        this.resetSelectionByTime();
        this.setPointingTime(time);
    }
    reset(): void {
        this.setPointingTime(this.pointingTime);
        this.resetSelectionByTime();
    }
    setTimezone(timezone: string): void {
        this.options.timezone = timezone;
    }
    setDateFormat(dateFormat: string[]): void {
        this.options.dateFormat = dateFormat;
    }
    destroy(): void {}
}
