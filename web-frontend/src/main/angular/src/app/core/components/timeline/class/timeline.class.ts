declare var Snap: any;

import { Subject } from 'rxjs';
import { TimelineData, ITimelineData, ITimelineStatusSegment, ITimelineEventSegment } from './timeline-data.class';
import { TimelinePositionManager } from './timeline-position-manager.class';
import { TimelineLoadingIndicator } from './timeline-loading-indicator.class';
import { TimelineBackground } from './timeline-background.class';
import { TimelineStateLine } from './timeline-state-line.class';
import { TimelineSelectionZone } from './timeline-selection-zone.class';
import { TimelineSelectionPoint } from './timeline-selection-point.class';
import { TimelineHandler } from './timeline-handler.class';
import { TimelineSignboard } from './timeline-signboard.class';
import { TimelineSelectionManager } from './timeline-selection-manager.class';
import { TimelineXAxis } from './timeline-x-axis.class';
import { TimelineEvents } from './timeline-events.class';
import { TimelineUIEvent } from './timeline-ui-event';

// TODO: Reset 확인
// TODO: Resize 확인
// TODO: 데이터 업데이트시 동작 확인
// TODO: timeline-selection-manager.onSetPointingByTime의 timeline.reset() 확인
export class Timeline {
    static MAX_TIME_RANGE = 604800000; // 7day
    static GROUP_TYPE: { [key: string]: string } = {
        TOP_BASE: 'TOP-BASE',
        CONTENT_BASE: 'CONTENT-BASE',
        BOTTOM_BASE: 'BOTTOM-BASE'
    };
    static DRAWING_ORDER: { [key: string]: number } = {
        'background': 0,
        'state-line': 3,
        'selection-zone': 5,
        'x-axis': 10,
        'events': 10,
        'time-signboard': 15,
        'selection-point': 15,
        'left-handler': 25,
        'right-handler': 25,
        'guide': 30,
        'loading': 100
    };
    static DEFAULT_STATUS_COLOR: { [key: string]: string } = {
        'BASE': 'rgba(187, 187, 187, 0.3)',
        'UNKNOWN': 'rgba(220, 214, 214, 0.8)',
        'RUNNING': 'rgba(0, 158, 0, 0.4)',
        'SHUTDOWN': 'rgba(209, 82, 96, 0.7)',
        'UNSTABLE_RUNNING': 'rgba(255, 102, 0, 0.4)',
        'EMPTY': 'rgba(165, 219, 245, 0.7)'
    };
    private snap: any;

    private oLoading: TimelineLoadingIndicator;
    private oBackground: TimelineBackground;
    private oTimelineData: TimelineData;
    private oPositionManager: TimelinePositionManager;
    private oStateLine: TimelineStateLine;
    private oSelectionManager: TimelineSelectionManager;
    private oXAxis: TimelineXAxis;
    private oEvents: TimelineEvents;

    private svgGroups: { [key: string]: any} = {};
    private options: { [key: string]: any } = {
        'top': 0,
        'left': 0,
        'duration': 50,
        'xAxisTicks': 5,
        'minTimeRange': 6000,   // 6sec
        'eventZoneHeight': 30,        // 하단 이벤트 영역의 height
        'headerZoneHeight': 20,       // 상단 시간 표시영역의 height
        'stateLineThickness': 4,       // 상태선의 두께
        'maxSelectionTimeRange': Timeline.MAX_TIME_RANGE,	// 7day
        'headerTextTopPadding': 10,   // 상단 상태선과 시간 text의 간격
        'pointerRadius': 6
    };
    onChangeTimelineUIEvent$: Subject<TimelineUIEvent> = new Subject();
    onSelectEventStatus$: Subject<ITimelineEventSegment> = new Subject();

    constructor(private element: any, options: { [key: string]: any }) {
        this.snap = Snap(element);
        this.snap.attr({'height': options.height });
        this.initOptions(options);
        this.initDataClass(options.timelineData);
        this.checkOffset();
        this.initControlClass();
        this.addEventListener();
    }
    initOptions(options: { [key: string]: any }) {
        Object.keys(options).forEach((key: string) => {
            this.options[key] = options[key];
        });
        this.setDefaultStatusColor();
        this.checkOffset();
    }
    setDefaultStatusColor() {
        if ( this.options['statusColor'] ) {
            Object.keys(Timeline.DEFAULT_STATUS_COLOR).forEach((key: string) => {
                if ( !this.options['statusColor'][key] ) {
                    this.options['statusColor'][key] = Timeline.DEFAULT_STATUS_COLOR[key];
                }
            });
        } else {
            const statusColor = {};
            Object.keys(Timeline.DEFAULT_STATUS_COLOR).forEach((key: string) => {
                statusColor[key] = Timeline.DEFAULT_STATUS_COLOR[key];
            });
            this.options['statusColor'] = statusColor;
        }
    }
    initDataClass(timelineData: ITimelineData) {
        this.oTimelineData = new TimelineData(timelineData || {
            agentEventTimeline: {
                timelineSegments: []
            },
            agentStatusTimeline: {
                includeWarning: false,
                timelineSegments: []
            }
        });
    }
    checkOffset() {
        const offset = this.element.getBoundingClientRect();
        this.options.top = offset.top;
        this.options.left = offset.left;
    }
    initControlClass() {
        this.oPositionManager = new TimelinePositionManager( {
            'width': this.options.width,
            'xAxisTicks': this.options.xAxisTicks,
            'pointingTime': this.options.pointingTime,
            'minTimeRange': this.options.minTimeRange,
            'timelineRange': this.options.timelineRange,
            'selectionTimeRange': this.options.selectionTimeRange,
            'maxSelectionTimeRange': this.options.maxSelectionTimeRange,
            'timezone': this.options.timezone,
            'dateFormat': this.options.dateFormat
        });
        const contentZoneHeight = this.options.height - this.options.headerZoneHeight - this.options.eventZoneHeight;

        this.oLoading = new TimelineLoadingIndicator(this.snap, this.getGroup('loading', Timeline.GROUP_TYPE.TOP_BASE, Timeline.DRAWING_ORDER['loading']), {
            'size': 30,
            'width': this.options.width,
            'height': this.options.height,
            'duration': 2000
        });
        this.oBackground = new TimelineBackground(this.snap, this.getGroup('background', Timeline.GROUP_TYPE.CONTENT_BASE, Timeline.DRAWING_ORDER['background']), {
            'top': 0,
            'left': 0,
            'size': 30,
            'width': this.options.width,
            'height': contentZoneHeight,
            'duration': this.options.duration
        });
        this.oStateLine = new TimelineStateLine(this.snap, this.getGroup('state-line', Timeline.GROUP_TYPE.CONTENT_BASE, Timeline.DRAWING_ORDER['state-line']), {
            'width': this.options.width,
            'height': contentZoneHeight,
            'duration': this.options.duration,
            'thickness': this.options.stateLineThickness,
            'statusColor': this.options.statusColor
        }, this.oPositionManager);

        const aSelectionZone = this.oPositionManager.getSelectionPosition();
        const oSelectionZone = new TimelineSelectionZone(this.snap, this.getGroup('selection-zone', Timeline.GROUP_TYPE.CONTENT_BASE, Timeline.DRAWING_ORDER['selection-zone']), {
            'top': 0,
            'left': aSelectionZone[0],
            'width': aSelectionZone[1] - aSelectionZone[0],
            'height': contentZoneHeight,
            'duration': this.options.duration
        }, this.oPositionManager);
        const oSelectionPoint = new TimelineSelectionPoint(this.snap, this.getGroup('selection-point', Timeline.GROUP_TYPE.CONTENT_BASE, Timeline.DRAWING_ORDER['selection-point']), {
            'y': this.options.headerZoneHeight,
            'x': this.oPositionManager.getPointingPosition(),
            'radius': this.options.pointerRadius,
            'height': contentZoneHeight,
            'duration': this.options.duration
        });
        const oLeftHandler = new TimelineHandler(this.snap, this.getGroup('left-handler', Timeline.GROUP_TYPE.CONTENT_BASE, Timeline.DRAWING_ORDER['left-handler']), {
            'x': aSelectionZone[0],
            'zone': [0, aSelectionZone[1]],
            'height': contentZoneHeight,
            'margin': this.options.left,
            'duration': this.options.duration
        });
        const oRightHandler = new TimelineHandler(this.snap, this.getGroup('right-handler', Timeline.GROUP_TYPE.CONTENT_BASE, Timeline.DRAWING_ORDER['right-handler']), {
            'x': aSelectionZone[1],
            'zone': [ aSelectionZone[0], this.oPositionManager.getTimelineEndPosition() ],
            'height': contentZoneHeight,
            'margin': this.options.left,
            'duration': this.options.duration
        });
        const oLeftTimeSignboard = new TimelineSignboard(this.snap, this.getGroup('time-left-signboard', Timeline.GROUP_TYPE.CONTENT_BASE, Timeline.DRAWING_ORDER['time-signboard']), {
            'x': aSelectionZone[0],
            'direction': 'left'
        }, this.oPositionManager);
        const oRightTimeSignboard = new TimelineSignboard(this.snap, this.getGroup('time-right-signboard', Timeline.GROUP_TYPE.CONTENT_BASE, Timeline.DRAWING_ORDER['time-signboard']), {
            'x': aSelectionZone[1],
            'direction': 'right'
        }, this.oPositionManager);
        this.oSelectionManager = new TimelineSelectionManager({
            'headerZoneHeight': this.options.headerZoneHeight,
            'contentZoneHeight': contentZoneHeight
        }, oSelectionZone, oSelectionPoint, oLeftHandler, oRightHandler, oLeftTimeSignboard, oRightTimeSignboard, this.oPositionManager);

        this.oXAxis = new TimelineXAxis(this.snap, this.getGroup('x-axis', Timeline.GROUP_TYPE.TOP_BASE, Timeline.DRAWING_ORDER['x-axis']), {
            'endY': this.options.height - this.options.eventZoneHeight,
            'width': this.options.width,
            'textY': this.options.headerTextTopPadding,
            'startY': this.options.headerZoneHeight,
            'duration': this.options.duration
        }, this.oPositionManager);
        this.oEvents = new TimelineEvents(this.snap, this.getGroup('events', Timeline.GROUP_TYPE.BOTTOM_BASE, Timeline.DRAWING_ORDER['events']), {
            'duration': this.options.duration
        }, this.oPositionManager );

        this.oLoading.hide();
    }
    getGroup(name: string, type: string, zIndex: number) {
        if ( this.svgGroups[name] ) {
            return this.svgGroups[name];
        }
        const g: any = this.svgGroups[name] = this.snap.g().attr({
            'class': name,
            'data-order': zIndex
        });
        this.setTransform(g, type);
        this.sortGroup(g, zIndex);
        return g;
    }
    setTransform(newGroup: any, type: string) {
        switch ( type ) {
            case Timeline.GROUP_TYPE.TOP_BASE:
                newGroup.attr({ 'transform': `translate(0, 0)` });
                break;
            case Timeline.GROUP_TYPE.CONTENT_BASE:
                newGroup.attr({ 'transform': `translate(0, ${this.options.headerZoneHeight})` });
                break;
            case Timeline.GROUP_TYPE.BOTTOM_BASE:
                newGroup.attr({ 'transform': `translate(0, ${this.options.height - this.options.eventZoneHeight})` });
                break;
        }
    }
    sortGroup(newGroup: any, zIndex: number) {
        const aGroups = this.snap.selectAll('g');
        let afterGroup = null;
        for ( let i = aGroups.length - 1; i >= 0 ; i-- ) {
            if ( aGroups[i] === newGroup ) {
                continue;
            }
            if ( zIndex < Math.floor(aGroups[i].attr('data-order')) ) {
                afterGroup = aGroups[i];
            }
        }
        if ( afterGroup !== null ) {
            afterGroup.before(newGroup);
        }
    }
    addEventListener() {
        let mousedownX = -1;
        window.addEventListener('resize', () => {
            this.checkOffset();
            this.resize();
        });
        this.snap.mousedown((event: any, x: number, y: number) => {
            mousedownX = x;
        });
        this.oEvents.clickEvent$.subscribe((eventData: ITimelineEventSegment) => {
            this.onSelectEventStatus$.next(eventData);
        });
        this.oTimelineData.onChangeStatusData$.subscribe((statusData: ITimelineStatusSegment[]) => {
            this.oStateLine.updateData(statusData);
        });
        this.oTimelineData.onChangeEventData$.subscribe((eventData: ITimelineEventSegment[]) => {
            this.oEvents.updateData(eventData);
        });
        this.oSelectionManager.onChangeTimelineUIEvent$.subscribe((event: TimelineUIEvent) => {
            this.onChangeTimelineUIEvent$.next(event);
        });
        this.oSelectionManager.onReset$.subscribe(() => {
            this.reset();
        });

        const eventStartPosition = this.options.headerZoneHeight;
        const eventEndPosition = this.options.height - this.options.eventZoneHeight;
        this.snap.click((event: any, x: number, y: number) => {
            if ( mousedownX !== x ) {
                return;
            }
            if ( event.offsetY > eventStartPosition && event.offsetY < eventEndPosition ) {
                this.oSelectionManager.onSetPointingByPosition(event.offsetX);
            }
        });
    }
    addData(oNewData: any) {
        this.oLoading.show();
        this.oTimelineData.addData( oNewData );
        this.oLoading.hide();
    }
    reset() {
        this.oXAxis.reset();
        this.oSelectionManager.reset();
        this.oStateLine.reset();
        this.oEvents.reset();
    }
    resize() {
        this.oPositionManager.setWidth(this.element.getBoundingClientRect().width);
        this.oBackground.reset(this.oPositionManager.getTimelineEndPosition());
        this.reset();
    }
    zoomIn() {
        // 1/2배씩
        this.oPositionManager.zoomIn();
        this.reset();
        this.fireChangedRangeEvent();
    }
    zoomOut() {
        // 2배씩
        this.oPositionManager.zoomOut();
        this.reset();
        this.fireChangedRangeEvent();
    }
    private fireChangedRangeEvent(): void {
        const event = new TimelineUIEvent();
        event.setOnRange();
        event.setData(
            this.oPositionManager.getPointingTime(),
            this.oPositionManager.getSelectionTimeRange(),
            this.oPositionManager.getTimelineRange()
        );
        this.onChangeTimelineUIEvent$.next(event);
    }
    resetTimeRangeAndSelectionZone(aSelectionFromTo: number[], aFromTo: number[], selectedTime: number) {
        this.oPositionManager.setTimelineRange(aFromTo[0], aFromTo[1]);
        this.oPositionManager.setSelectionStartTime(aSelectionFromTo[0]);
        this.oPositionManager.setSelectionEndTime(aSelectionFromTo[1]);
        this.oPositionManager.setPointingTime(selectedTime || aSelectionFromTo[1]);
        this.emptyData();
        this.reset();
    }
    movePrev() {
        this.oSelectionManager.onSetPointingByTime(this.oPositionManager.getPrevTime());
    }
    moveNext() {
        this.oSelectionManager.onSetPointingByTime(this.oPositionManager.getNextTime());
    }
    moveHead() {
        this.oSelectionManager.onSetPointingByTime(Date.now(), true);
    }
    getTimelineRange() {
        return this.oPositionManager.getTimelineRange();
    }
    getSelectionTimeRange() {
        return this.oPositionManager.getSelectionTimeRange();
    }
    emptyData() {
        this.oLoading.show();
        this.oTimelineData.emptyData();
        this.oEvents.emptyData();
        this.oStateLine.emptyData();
        this.oLoading.hide();
    }
    setPointingTime(time: number): void {
        this.oSelectionManager.onSetPointingByTime(time);
    }
    getPointingTime() {
        return this.oPositionManager.getPointingTime();
    }
    setTimezone(timezone: string): void {
        this.options.timezone = timezone;
        this.oPositionManager.setTimezone(timezone);
        this.reset();
    }
    setDateFormat(dateFormat: string[]): void {
        this.options.dateFormat = dateFormat;
        this.oPositionManager.setDateFormat(dateFormat);
        this.reset();
    }
    destroy() {
        this.oLoading.destroy();
        this.oBackground.destroy();
        this.oTimelineData.destroy();
        this.oPositionManager.destroy();
        this.oStateLine.destroy();
        this.oSelectionManager.destroy();
        this.oXAxis.destroy();
        this.oEvents.destroy();
    }
}
