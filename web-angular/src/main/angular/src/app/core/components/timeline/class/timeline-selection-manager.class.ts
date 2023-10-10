import { Subject, Subscription } from 'rxjs';
import { TimelinePositionManager } from './timeline-position-manager.class';
import { TimelineSelectionZone } from './timeline-selection-zone.class';
import { TimelineSelectionPoint } from './timeline-selection-point.class';
import { TimelineHandler } from './timeline-handler.class';
import { TimelineSignboard } from './timeline-signboard.class';
import { TimelineUIEvent } from './timeline-ui-event';

export class TimelineSelectionManager {

    unsubscribe: Subscription;
    onChangeTimelineUIEvent$: Subject<TimelineUIEvent> = new Subject();
    onReset$: Subject<null> = new Subject();
    constructor(
        private options: { [key: string]: any },
        private oSelectionZone: TimelineSelectionZone,
        private oSelectionPoint: TimelineSelectionPoint,
        private oLeftHandler: TimelineHandler,
        private oRightHandler: TimelineHandler,
        private oLeftTimeSignboard: TimelineSignboard,
        private oRightTimeSignboard: TimelineSignboard,
        private oPositionManager: TimelinePositionManager
    ) {
        this.initClass();
    }
    initClass(): void {
        this.unsubscribe = this.oLeftHandler.onDragStart$.subscribe((x: number) => {
            this.oLeftTimeSignboard.onDragStart(x);
        });
        this.unsubscribe.add(this.oLeftHandler.onDragging$.subscribe((x: number) => {
            this.oSelectionZone.onDragXStart(x);
            this.oLeftTimeSignboard.onDrag(x);
        }));
        this.unsubscribe.add(this.oLeftHandler.onDragEnd$.subscribe((res: { dragged: boolean, x: number }) => {
            this.oLeftTimeSignboard.onDragEnd();
            if (res.dragged) {
                this.movedLeftHandler(res.x);
            }
        }));

        this.unsubscribe.add(this.oRightHandler.onDragStart$.subscribe((x: number) => {
            this.oRightTimeSignboard.onDragStart(x);
        }));
        this.unsubscribe.add(this.oRightHandler.onDragging$.subscribe((x: number) => {
            this.oSelectionZone.onDragXEnd(x);
            this.oRightTimeSignboard.onDrag(x);
        }));
        this.unsubscribe.add(this.oRightHandler.onDragEnd$.subscribe((res: { dragged: boolean, x: number }) => {
            this.oRightTimeSignboard.onDragEnd();
            if (res.dragged) {
                this.movedRightHandler(res.x);
            }
        }));
    }
    movedLeftHandler(x: number): void {
        let selectedTime = this.oPositionManager.getPointingTime();
        const event = new TimelineUIEvent();
        const aCurrentSelectionTimeRange = this.oPositionManager.getSelectionTimeRange();
        const newLeftTime = this.oPositionManager.getTimeFromPosition(x);
        if ( this.oPositionManager.isInMaxSelectionTimeRange(newLeftTime, aCurrentSelectionTimeRange[1])) {
            this.oRightHandler.setZone(x, this.oPositionManager.getTimelineEndPosition());
            this.oPositionManager.setSelectionStartPosition(x);

            if ( this.oPositionManager.isInSelectionZone() === false ) {
                this.oPositionManager.setPointingTime(newLeftTime);
                this.oSelectionPoint.setPointing(x);
                selectedTime = newLeftTime;
                event.setOnChangedSelectedTime();
            }
        } else {
            const aNewSelectionTimeSeries = this.oPositionManager.getNewSelectionTimeRangeFromStart(newLeftTime);
            const newRightX = this.oPositionManager.getPositionByTime(aNewSelectionTimeSeries[1]);
            this.oRightHandler.setZone(x, this.oPositionManager.getTimelineEndPosition());
            this.oPositionManager.setSelectionStartPosition(x);
            this.oRightHandler.setX(newRightX);
            this.oRightTimeSignboard.onDrag(newRightX);
            this.oLeftHandler.setZone(0, newRightX);
            this.oPositionManager.setSelectionEndPosition(newRightX);

            if ( this.oPositionManager.isInSelectionZone() === false ) {
                this.oPositionManager.setPointingTime(aNewSelectionTimeSeries[1]);
                this.oSelectionPoint.setPointing(newRightX);
                selectedTime = aNewSelectionTimeSeries[1];
                event.setOnChangedSelectedTime();
            }
        }
        this.oSelectionZone.redraw();
        event.setOnChangedSelectionRange();
        event.setData(
            selectedTime,
            this.oPositionManager.getSelectionTimeRange(),
            this.oPositionManager.getTimelineRange()
        );
        this.onChangeTimelineUIEvent$.next(event);
    }
    movedRightHandler(x: number): void {
        let selectedTime = this.oPositionManager.getPointingTime();
        const event = new TimelineUIEvent();
        const aCurrentSelectionTimeRange = this.oPositionManager.getSelectionTimeRange();
        const newRightTime = this.oPositionManager.getTimeFromPosition(x);
        if ( this.oPositionManager.isInMaxSelectionTimeRange(aCurrentSelectionTimeRange[0], newRightTime) ) {
            this.oLeftHandler.setZone(0, x);
            this.oPositionManager.setSelectionEndPosition(x);

            if ( this.oPositionManager.isInSelectionZone() === false ) {
                this.oPositionManager.setPointingTime(newRightTime);
                this.oSelectionPoint.setPointing(x);
                selectedTime = newRightTime;
                event.setOnChangedSelectedTime();
            }
        } else {
            const aNewSelectionTimeSeries = this.oPositionManager.getNewSelectionTimeRangeFromEnd(newRightTime);
            const newLeftX = this.oPositionManager.getPositionByTime(aNewSelectionTimeSeries[0]);
            this.oLeftHandler.setZone(0, x);
            this.oPositionManager.setSelectionEndPosition(x);
            this.oLeftHandler.setX(newLeftX);
            this.oLeftTimeSignboard.onDrag(newLeftX);
            this.oRightHandler.setZone(newLeftX, this.oPositionManager.getTimelineEndPosition());
            this.oPositionManager.setSelectionStartPosition(newLeftX);

            if ( this.oPositionManager.isInSelectionZone() === false ) {
                this.oPositionManager.setPointingTime(aNewSelectionTimeSeries[0]);
                this.oSelectionPoint.setPointing(newLeftX);
                selectedTime = aNewSelectionTimeSeries[0];
                event.setOnChangedSelectedTime();
            }
        }
        this.oSelectionZone.redraw();
        event.setOnChangedSelectionRange();
        event.setData(
            selectedTime,
            this.oPositionManager.getSelectionTimeRange(),
            this.oPositionManager.getTimelineRange()
        );
        this.onChangeTimelineUIEvent$.next(event);
    }
    moveSelectionAndHandler(): void {
        const aNewSelectionZone = this.oPositionManager.getSelectionPosition();
        this.oLeftHandler.setPositionAndZone(aNewSelectionZone[0], [0, aNewSelectionZone[1]]);
        this.oRightHandler.setPositionAndZone(aNewSelectionZone[1], [aNewSelectionZone[0], this.oPositionManager.getTimelineEndPosition()]);
        this.oSelectionZone.redraw();
    }
    onSetPointingByPosition(x: number): void {
        this.onSetPointingByTime(this.oPositionManager.getTimeFromPosition(x));
    }
    onSetPointingByTime(time: number, bIsNow?: boolean): void {
        const event = new TimelineUIEvent();
        event.setOnChangedSelectedTime();
        if ( this.oPositionManager.isInTimelineRange(time) ) {
            this.oPositionManager.setPointingTime(time);
            if ( this.oPositionManager.isInSelectionZone() === false ) {
                this.oPositionManager.calcuSelectionZone();
                this.moveSelectionAndHandler();
                event.setOnChangedSelectionRange();
            }
            this.oSelectionPoint.setPointing(this.oPositionManager.getPointingPosition());
        } else {
            this.oPositionManager.resetBySelectTime(time, bIsNow);
            this.onReset$.next();
            event.setOnChangedSelectionRange();
        }
        event.setData(
            this.oPositionManager.getPointingTime(),
            this.oPositionManager.getSelectionTimeRange(),
            this.oPositionManager.getTimelineRange()
        );
        this.onChangeTimelineUIEvent$.next(event);
    }
    reset(): void {
        const aNewSelectionZone = this.oPositionManager.getSelectionPosition();
        this.oLeftHandler.setPositionAndZone(aNewSelectionZone[0], [0, aNewSelectionZone[1]]);
        this.oRightHandler.setPositionAndZone(aNewSelectionZone[1], [aNewSelectionZone[0], this.oPositionManager.getTimelineEndPosition()]);
        this.oSelectionZone.redraw();
        this.oSelectionPoint.setPointing(this.oPositionManager.getPointingPosition());
    }
    destroy(): void {
        this.unsubscribe.unsubscribe();
    }
}
