import { Component, OnInit, ViewEncapsulation, OnChanges, OnDestroy, SimpleChanges, Input, Output, EventEmitter, ViewChild, ElementRef, Renderer2 } from '@angular/core';
import { Timeline, ITimelineData, ITimelineEventSegment, TimelineUIEvent } from './class';

@Component({
    selector: 'pp-timeline',
    templateUrl: './timeline.component.html',
    styleUrls: ['./timeline.component.css'],
    encapsulation: ViewEncapsulation.None,
})
export class TimelineComponent implements OnInit, OnChanges, OnDestroy {
    @ViewChild('timeline', { static: true }) el: ElementRef;

    @Input() data: any;
    @Input() height: number;
    @Input() pointingTime: number;
    @Input() timelineStartTime: number;
    @Input() timelineEndTime: number;
    @Input() selectionStartTime: number;
    @Input() selectionEndTime: number;
    @Input() timezone: string;
    @Input() dateFormat: string[];

    @Output() outChangeTimelineUIEvent: EventEmitter<TimelineUIEvent> = new EventEmitter();
    @Output() outSelectEventStatus: EventEmitter<ITimelineEventSegment> = new EventEmitter();

    timeline: Timeline;
    constructor(
        private hostElRef: ElementRef,
        private renderer: Renderer2
    ) {}
    ngOnInit() {
        this.renderer.setStyle(this.hostElRef.nativeElement, 'display', 'block');
    }
    ngOnChanges(changes: SimpleChanges) {
        if (changes['data'] && changes['data'].currentValue) {
            this.initTimeline();
        }
        if (changes['timezone'] && this.timeline) {
            this.timeline.setTimezone(this.timezone);
        }
        if (changes['dateFormat'] && this.timeline) {
            this.timeline.setDateFormat(this.dateFormat);
        }
    }
    // selectionTime : 전체 조회 기간 중 선택 구간
    // start - from : timeline의 전체 조회 구간
    // pointingTime : 포인팅 지점
    initTimeline() {
        if (this.timeline) {
            this.timeline.resetTimeRangeAndSelectionZone(
                [this.selectionStartTime, this.selectionEndTime],
                [this.timelineStartTime, this.timelineEndTime],
                this.pointingTime
            );
            this.timeline.addData(this.data);
        } else {
            this.timeline = new Timeline(this.el.nativeElement, {
                'width': this.hostElRef.nativeElement.offsetWidth,
                'height': this.height,
                'timelineRange': [this.timelineStartTime, this.timelineEndTime],
                'selectionTimeRange': [this.selectionStartTime, this.selectionEndTime],
                'pointingTime': this.pointingTime,
                'timelineData': <ITimelineData>this.data,
                'timezone': this.timezone,
                'dateFormat': this.dateFormat,
                'statusColor': {
                    'BASE': 'rgba(187, 187, 187, .3)',
                    'UNKNOWN': 'rgba(220, 214, 214, .8)',
                    'RUNNING': 'rgba(0, 158, 0, .4 )',
                    'SHUTDOWN': 'rgba(209, 82, 96, .7)',
                    'UNSTABLE_RUNNING': 'rgba(255, 102, 0, .4)',
                    'EMPTY': 'rgba(165, 219, 245, .7)'
                }
            });
            this.timeline.onSelectEventStatus$.subscribe((eventSegment: ITimelineEventSegment) => {
                this.outSelectEventStatus.emit(eventSegment);
            });
            this.timeline.onChangeTimelineUIEvent$.subscribe((event: TimelineUIEvent) => {
                this.outChangeTimelineUIEvent.emit(event);
            });
        }
    }
    zoomIn(): void {
        this.timeline.zoomIn();
    }
    zoomOut(): void {
        this.timeline.zoomOut();
    }
    movePrev(): void {
        this.timeline.movePrev();
    }
    moveNext(): void {
        this.timeline.moveNext();
    }
    moveNow(): void {
        this.timeline.moveHead();
    }
    getTimelineRange(): number[] {
        return this.timeline.getTimelineRange();
    }
    updateData(data: any): void {
        this.timeline.addData(data);
    }
    ngOnDestroy() {
        // this.timeline.destroy();
    }
}
