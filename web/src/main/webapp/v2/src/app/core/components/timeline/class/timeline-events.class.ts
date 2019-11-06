declare var Snap: any;
import { Subject } from 'rxjs';
import { ITimelineEventSegment } from './timeline-data.class';
import { TimelinePositionManager } from './timeline-position-manager.class';

export class TimelineEvents {
    private aEventGroupElement: any[] = [];
    private aEventStatusData: ITimelineEventSegment[];
    clickEvent$: Subject<ITimelineEventSegment> = new Subject();
    constructor(private snap: any, private group: any, private options: { [key: string]: any }, private oPositionManager: TimelinePositionManager ) {
        this.initOptions();
        this.addEvents();
    }
    initOptions() {
        const options = {
            'y': 4,
            'barLength': 4,
            'gapBarNCircle': 2,
            'circleRadius': 8,
            'filter': this.snap.filter( Snap.filter.shadow(1, 1, 1, '#000', 0.3))
        };
        Object.keys(options).forEach((key) => {
            this.options[key] = options[key];
        });
    }
    addEvents() {
        this.group.click((event: any, x: number, y: number) => {
            const targetElement = this.getElement(event, 'g');
            const dataIndex = Math.floor(targetElement.getAttribute('data-id'));
            // const dataTime = targetElement.getAttribute('data-time');
            this.clickEvent$.next(this.aEventStatusData[dataIndex]);
        });
    }
    getElement(event: any, elementName: string): any | null {
        let target = event.target;
        while (target) {
            if ( target.tagName.toLowerCase() === elementName ) {
                break;
            }
            target = target.parentElement;
        }
        return target;
    }
    updateData(data: ITimelineEventSegment[]): void {
        this.aEventStatusData = data;
        this.emptyData();
        this.reset();
    }
    emptyData(): void {
        this.aEventGroupElement.forEach((g: any) => {
            g.attr('display', 'none');
        });
    }
    reset(): void {
        const oldLen = this.aEventGroupElement.length;
        const newLen = this.aEventStatusData.length;
        let index;

        if ( oldLen === newLen ) {
            for ( index = 0 ; index < newLen ; index++ ) {
                this.reposition(this.aEventGroupElement[index], index);
            }
        } else if ( oldLen > newLen ) {
            for ( index = newLen ; index < oldLen ; index++ ) {
                this.aEventGroupElement[index].attr('display', 'none');
            }
            for ( index = 0 ; index < newLen ; index++ ) {
                this.reposition(this.aEventGroupElement[index], index);
            }
        } else { // oldLen < newLen
            for ( index = 0 ; index < oldLen ; index++ ) {
                this.reposition(this.aEventGroupElement[index], index);
            }
            for ( index = oldLen ; index < newLen ; index++ ) {
                this.addEventElement(this.aEventStatusData[index], index);
            }
        }
    }
    reposition(elEventGroup: any, index: number): void {
        const oEvent = this.aEventStatusData[index];
        const time = oEvent.startTimestamp + (oEvent.endTimestamp - oEvent.startTimestamp) / 2;
        const x = this.oPositionManager.getPositionByTime(time);
        const oTextInfo = this.getEventTextInfo(oEvent.value.totalCount);
        elEventGroup[2].attr({
            x: oTextInfo.x,
            y: oTextInfo.y,
            text: oTextInfo.text
        });
        elEventGroup.attr('display', 'block');
        elEventGroup.animate({
            'transform': `translate(${x}, 0)`
        }, this.options.duration);
    }
    addEventElement(oEvent: ITimelineEventSegment, index: number): void {
        this.group.add(this.makeElement(oEvent, index));
    }
    makeElement(oEvent: ITimelineEventSegment, index: number): any {
        const time = oEvent.startTimestamp + (oEvent.endTimestamp - oEvent.startTimestamp) / 2;
        const oTextInfo = this.getEventTextInfo(oEvent.value.totalCount);
        const elEventGroup = this.group.g().attr({
            'data-id': index,
            'data-time': time,
            'transform': `translate(${this.oPositionManager.getPositionByTime(time)}, 0)`
        });
        elEventGroup.add(
            this.snap.line(0, this.options.y, 0, this.options.y + this.options.barLength),
            this.snap.circle(0, this.options.y + this.options.circleRadius + this.options.gapBarNCircle + this.options.barLength, this.options.circleRadius).attr({
                'class': 'event',
                'filter': this.options.filter,
                'data-time': time
            }),
            this.snap.text(oTextInfo.x, oTextInfo.y, oTextInfo.text).attr({
                'class': 'event'
            })
        );
        this.aEventGroupElement.push(elEventGroup);
        return elEventGroup;
    }
    getEventTextInfo(totalCount: number): { x: number, y: number, text: string } {
        return {
            x: totalCount < 10 ? -(this.options.circleRadius / 3 ) : -(this.options.circleRadius / 4 ) * 3,
            y: this.options.y + this.options.circleRadius + (this.options.circleRadius / 2) + this.options.gapBarNCircle + this.options.barLength,
            text: totalCount >= 100 ? '...' : totalCount + ''
        };
    }
    destroy(): void {}
}
