declare var Snap: any;
declare var mina: any;
import { Subject } from 'rxjs';

export class TimelineHandler {
    static HANDLER_IMAGE_WIDTH = 30;
    static HANDLER_IMAGE_HEIGHT = 18;

    previousX: number;
    handlerGrip: any;
    handlerGroup: any;
    onDragStart$: Subject<number> = new Subject();
    onDragging$: Subject<number> = new Subject();
    onDragEnd$: Subject<{ dragged: boolean, x: number }> = new Subject();

    constructor(private snap: any, private group: any, private options: { [key: string]: any }) {
        this.previousX = options.x;
        this.addElements();
        this.setX(options.x);
        this.addEvent();
    }
    addElements(): void {
        this.handlerGrip = this.snap.circle(0, 3, 5).attr({
            'fill': '#777af9',
            'cursor': 'pointer',
            'stroke': '#4E50C8',
            'stroke-width': '3px'
        });
        this.handlerGroup = this.group.g();
        this.handlerGroup.add(
            this.snap.line( 0, 0, 0, this.options.height ),
            this.snap.circle(0, 3, 7).attr({
                'fill': '#000',
                'filter': Snap.filter.shadow(0, 0, 2, '#000', .5)
            }),
            this.handlerGrip
        );
    }
    addEvent(): void {
        let lastX = -1;
        this.handlerGrip.click((event: any) => {
            event.stopPropagation();
        });
        this.handlerGrip.mousedown((event: any) => {
            event.stopPropagation();
        });
        this.handlerGrip.drag((dx: number, dy: number, x: number, y: number, event: any) => {
            const newX = x - this.options.margin;
            if ( this.isInRestrictionZone(newX) === false ) {
                return;
            }
            this.handlerGroup.attr({
                'transform': `translate(${newX}, 0)`
            });
            lastX = newX;
            this.onDragging$.next(newX);
        }, (x: number, y: number, event: any) => {
            event.stopPropagation();
            this.onDragStart$.next(x - this.options.margin);
        }, (event: any) => {
            event.stopPropagation();
            if ( this.previousX !== lastX && lastX !== -1 ) {
                this.onDragEnd$.next({ dragged: true, x: lastX });
                this.previousX = lastX;
            } else {
                this.onDragEnd$.next({ dragged: false, x: -1 });
            }
        });
    }
    setX(x: number): void {
        this.handlerGroup.animate({
            'transform': `translate(${x}, 0)`
        }, this.options.duration, mina.easeout);
    }
    isInRestrictionZone(x: number): boolean {
        return (x <= this.options.zone[0] || x >= this.options.zone[1]) ? false : true;
    }
    setZone(start: number, end: number): void {
        this.options.zone = [start, end];
    }
    setPositionAndZone(x: number, aZone: number[]): void {
        this.setX(x);
        this.onDragging$.next(x);
        this.setZone(aZone[0], aZone[1]);
    }
    destroy(): void {}
}
