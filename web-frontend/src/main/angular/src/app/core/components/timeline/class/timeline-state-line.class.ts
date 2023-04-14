declare var Snap: any;
declare var mina: any;
import { TimelinePositionManager } from './timeline-position-manager.class';
import { ITimelineStatusSegment } from './timeline-data.class';

export class TimelineStateLine {
    static ID_SPLITER = '+';
    static ID_PREFIX_BASE = 'base-';
    static ID_POSTFIX = '+state-line';

    statusData: ITimelineStatusSegment[];
    aBaseLine: any[] = [];
    aLineElement: any[] = [];

    constructor(private snap: any, private group: any, private options: { [key: string]: any }, private oPositionManager: TimelinePositionManager) {
        this.initBaseLine();
    }
    initBaseLine(): void {
        this.aBaseLine.push(
            this.makeRect(0, this.options.width, 0, this.options.height, this.options.statusColor['BASE'], TimelineStateLine.ID_PREFIX_BASE + Date.now())
        );
        this.aBaseLine.forEach((line: any) => {
            this.group.add(line);
        });
    }
    addStatus(oStatus: ITimelineStatusSegment, index: number): void {
        if ( this.isOutsideOfTimeline(oStatus.startTimestamp, oStatus.endTimestamp) ) {
            return;
        }
        this.addLine(
            this.oPositionManager.getPositionByTime(oStatus.startTimestamp),
            this.getX2(oStatus.endTimestamp),
            this.options.statusColor[oStatus.value],
            this.makeID(oStatus)
        );
    }
    isOutsideOfTimeline(start: number, end: number): boolean {
        return this.oPositionManager.isInTimelineRange(start) === false && this.oPositionManager.isInTimelineRange(end) === false;
    }
    makeID(oStatus: ITimelineStatusSegment): string {
        return oStatus.endTimestamp + TimelineStateLine.ID_POSTFIX;
    }
    addLine(x: number, x2: number, backgroundColor: string, id: string): void {
        const line = this.makeRect(x, x2, 0, this.options.height, backgroundColor, id);
        this.aLineElement.push(line);
        this.group.add(line);
    }
    makeRect(x: number, x2: number, y: number, y2: number, color: string, id: string): any {
        return this.snap.rect(x, y, x2, y2).attr({
            'fill': color,
            'data-id': id
        });
    }
    getX2(end: number): number {
        return this.oPositionManager.getPositionByTime(end === -1 ? this.oPositionManager.getTimelineEndTime() : end);
    }
    updateData(statusData: ITimelineStatusSegment[]): void {
        this.statusData = statusData;
        this.updateRender();
    }
    updateRender(): void {
        let index;
        const curLen = this.aLineElement.length;
        const newLen = this.statusData.length;
        if ( curLen === newLen ) {
            for ( index = 0 ; index < curLen ; index++ ) {
                this.show(this.aLineElement[index]);
            }
        } else if ( curLen > newLen ) {
            for ( index = newLen ; index < curLen ; index++ ) {
                this.hide(this.aLineElement[index]);
            }
        } else { // curLen < newLen
            for ( index = 0 ; index < curLen ; index++ ) {
                this.show(this.aLineElement[index]);
            }
            for ( index = curLen ; index < newLen ; index++ ) {
                this.addStatus(this.statusData[index], index);
            }
        }
        this.reset();
    }
    emptyData(): void {
        this.aLineElement.forEach((line: any) => {
            this.hide(line);
        });
    }
    reset() {
        for ( let i = 0 ; i < this.aLineElement.length ; i++ ) {
            const line = this.aLineElement[i];
            const oStatus = this.statusData[i];
            if ( oStatus ) {
                const x = this.oPositionManager.getPositionByTime(oStatus.startTimestamp);
                this.show(line);
                line.animate({
                    'x': x,
                    'width': this.getX2(oStatus.endTimestamp) - x
                }, this.options.duration, mina.easeOut, () => {
                    line.attr('fill', this.options.statusColor[oStatus.value]);
                });
            } else {
                this.hide(line);
            }
        }
        const endPosition = this.oPositionManager.getTimelineEndPosition();
        this.aBaseLine.forEach((elBase: any) => {
            elBase.animate({
                'width': endPosition
            }, this.options.duration);
        });
    }
    show(el: any) {
        el.attr('display', 'block');
    }
    hide(el: any) {
        el.attr('display', 'none');
    }
    destroy(): void {}
}
