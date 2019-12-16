import { Component, OnInit, ElementRef, Input, Output, EventEmitter, AfterViewInit, HostBinding } from '@angular/core';
import { Observable } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

import { WindowRefService, DynamicPopup, PopupConstant } from 'app/shared/services';

export enum HELP_VIEWER_LIST {
    NAVBAR = 'HELP_VIEWER.NAVBAR',
    RESPONSE_SUMMARY = 'HELP_VIEWER.RESPONSE_SUMMARY',
    LOAD = 'HELP_VIEWER.LOAD',
    SERVER_MAP = 'HELP_VIEWER.SERVER_MAP',
    REAL_TIME = 'HELP_VIEWER.REAL_TIME',
    CALL_TREE = 'HELP_VIEWER.CALL_TREE',
    SCATTER = 'HELP_VIEWER.SCATTER',
    AGENT_LIST = 'HELP_VIEWER.INSPECTOR.AGENT_LIST',
    AGENT_JVM_HEAP = 'HELP_VIEWER.INSPECTOR.AGENT_CHART.HEAP',
    AGENT_JVM_NON_HEAP = 'HELP_VIEWER.INSPECTOR.AGENT_CHART.NON_HEAP',
    AGENT_CPU = 'HELP_VIEWER.INSPECTOR.AGENT_CHART.CPU_USAGE',
    AGENT_TPS = 'HELP_VIEWER.INSPECTOR.AGENT_CHART.TPS',
    AGENT_ACTIVE_THREAD = 'HELP_VIEWER.INSPECTOR.AGENT_CHART.ACTIVE_THREAD',
    AGENT_RESPONSE_TIME = 'HELP_VIEWER.INSPECTOR.AGENT_CHART.RESPONSE_TIME',
    AGENT_DATA_SOURCE = 'HELP_VIEWER.INSPECTOR.AGENT_CHART.DATA_SOURCE',
    AGENT_OPEN_FILE_DESCRIPTOR = 'HELP_VIEWER.INSPECTOR.AGENT_CHART.OPEN_FILE_DESCRIPTOR',
    AGENT_DIRECT_BUFFER_COUNT = 'HELP_VIEWER.INSPECTOR.AGENT_CHART.DIRECT_BUFFER_COUNT',
    AGENT_DIRECT_BUFFER_MEMORY = 'HELP_VIEWER.INSPECTOR.AGENT_CHART.DIRECT_BUFFER_MEMORY',
    AGENT_MAPPED_BUFFER_COUNT = 'HELP_VIEWER.INSPECTOR.AGENT_CHART.MAPPED_BUFFER_COUNT',
    AGENT_MAPPED_BUFFER_MEMORY = 'HELP_VIEWER.INSPECTOR.AGENT_CHART.MAPPED_BUFFER_MEMORY',
    APPLICATION_JVM_HEAP = 'HELP_VIEWER.INSPECTOR.APPLICATION_CHART.HEAP',
    APPLICATION_JVM_NON_HEAP = 'HELP_VIEWER.INSPECTOR.APPLICATION_CHART.NON_HEAP',
    APPLICATION_JVM_CPU = 'HELP_VIEWER.INSPECTOR.APPLICATION_CHART.JVM_CPU_USAGE',
    APPLICATION_SYSTEM_CPU = 'HELP_VIEWER.INSPECTOR.APPLICATION_CHART.SYSTEM_CPU_USAGE',
    APPLICATION_TPS = 'HELP_VIEWER.INSPECTOR.APPLICATION_CHART.TPS',
    APPLICATION_ACTIVE_THREAD = 'HELP_VIEWER.INSPECTOR.APPLICATION_CHART.ACTIVE_THREAD',
    APPLICATION_RESPONSE_TIME = 'HELP_VIEWER.INSPECTOR.APPLICATION_CHART.RESPONSE_TIME',
    APPLICATION_DATA_SOURCE = 'HELP_VIEWER.INSPECTOR.APPLICATION_CHART.DATA_SOURCE',
    APPLICATION_OPEN_FILE_DESCRIPTOR = 'HELP_VIEWER.INSPECTOR.APPLICATION_CHART.OPEN_FILE_DESCRIPTOR',
    APPLICATION_DIRECT_BUFFER_COUNT = 'HELP_VIEWER.INSPECTOR.APPLICATION_CHART.DIRECT_BUFFER_COUNT',
    APPLICATION_DIRECT_BUFFER_MEMORY = 'HELP_VIEWER.INSPECTOR.APPLICATION_CHART.DIRECT_BUFFER_MEMORY',
    APPLICATION_MAPPED_BUFFER_COUNT = 'HELP_VIEWER.INSPECTOR.APPLICATION_CHART.MAPPED_BUFFER_COUNT',
    APPLICATION_MAPPED_BUFFER_MEMORY = 'HELP_VIEWER.INSPECTOR.APPLICATION_CHART.MAPPED_BUFFER_MEMORY',
}

const enum HELP_VIEWER_WIDTH_STATE {
    OK,
    LEFT_OVERFLOW,
    RIGHT_OVERFLOW
}

const enum HELP_VIEWER_HEIGHT_STATE {
    OK,
    DOWN_OVERFLOW
}

@Component({
    selector: 'pp-help-viewer-popup-container',
    templateUrl: './help-viewer-popup-container.component.html',
    styleUrls: ['./help-viewer-popup-container.component.css'],
})
export class HelpViewerPopupContainerComponent implements OnInit, AfterViewInit, DynamicPopup {
    @Input() data: HELP_VIEWER_LIST;
    @Input() coord: ICoordinate;
    @Output() outCreated = new EventEmitter<ICoordinate>();
    @Output() outClose = new EventEmitter<void>();
    @Output() outReInit = new EventEmitter<{[key: string]: any}>();
    @HostBinding('class') styleClass: string;

    private startPoint = 28; // 클릭한 버튼을 기준으로 꼭지점에서 살짝 밀어주는 길이
    data$: Observable<{[key: string]: any}[]>;
    tooltipTriangleStyle: {[key: string]: any} = {
        'border-bottom': `${PopupConstant.TOOLTIP_TRIANGLE_HEIGHT}px solid #fff`,
        'border-right': `${PopupConstant.TOOLTIP_TRIANGLE_HEIGHT}px solid transparent`,
        'border-left': `${PopupConstant.TOOLTIP_TRIANGLE_HEIGHT}px solid transparent`,
        'transform-origin': `50% -${PopupConstant.SPACE_FROM_BUTTON}px`,
    };

    constructor(
        private elementRef: ElementRef,
        private translateService: TranslateService,
        private windowRefService: WindowRefService,
    ) {}

    ngOnInit() {
        this.setStyleClass(this.data);
        this.data$ = this.getHelpViewerText(this.data);
    }

    ngAfterViewInit() {
        this.outCreated.emit(this.getPosition(this.coord));
    }

    onInputChange({data, coord}: {data: HELP_VIEWER_LIST, coord: ICoordinate}): void {
        if (this.coord) {
            const { coordX: x1, coordY: y1 } = this.coord;
            const { coordX: x2, coordY: y2 } = coord;

            x1 === x2 && y1 === y2 ? this.outClose.emit() : this.outReInit.emit({ data, coord });
        }
    }

    private setStyleClass(data: HELP_VIEWER_LIST): void {
        const className = Object.keys(HELP_VIEWER_LIST).find((cur: keyof typeof HELP_VIEWER_LIST) => {
            return HELP_VIEWER_LIST[cur]  === data;
        }).toLowerCase();

        this.styleClass = `popup ${className}`;
    }

    private setPosition(coordY: number, coordX: number): ICoordinate {
        return { coordX, coordY };
    }

    private getPosition({coordX, coordY}: ICoordinate): ICoordinate {
        /**
         * HelpViewer위치 띄워주는 기준
         * Width기준: event.clientX - TOOLTIP_CONSTANT.INDENT_WIDTH(왼쪽으로 살짝 밀어줄 너비) + width 의 overflow여부
         * Height기준: event.clientY + TOOLTIP_CONSTANT.TRIANGLE_HEIGHT(말풍선 삼각형 높이) + height 의 overflow여부
         * 1. Width: OK, Height: OK => 클릭한 버튼 기준 밑에 위치
         * 2. Width: OK, Height: Overflow => 클릭한 버튼 기준 위에 위치
         * 3. Width: Left Overflow, Height: OK => 클릭한 버튼 기준 오른쪽, 밑방향으로 위치
         * 4. Width: Right Overflow, Height: OK => 클릭한 버튼 기준 왼쪽, 밑방향으로 위치
         * 5. Width: Left Overflow, Height: Overflow => 클릭한 기준 오른쪽, 윗방향으로 위치
         * 6. Width: Right Overflow, Height: Overflow => 클릭한 기준 왼쪽, 윗방향으로 위치
        */
        const width = this.elementRef.nativeElement.offsetWidth;
        const height = this.elementRef.nativeElement.offsetHeight;
        const widthState = this.checkWidth(width);
        const heightState = this.checkHeight(height);
        let pos: ICoordinate;

        switch (widthState) {
            case HELP_VIEWER_WIDTH_STATE.OK:
                switch (heightState) {
                    case HELP_VIEWER_HEIGHT_STATE.OK:
                        this.setTooltipTriangleStyle(coordY + PopupConstant.SPACE_FROM_BUTTON, coordX - PopupConstant.TOOLTIP_TRIANGLE_WIDTH / 2, '');
                        pos = this.setPosition(coordY + PopupConstant.SPACE_FROM_BUTTON + PopupConstant.TOOLTIP_TRIANGLE_HEIGHT, coordX - this.startPoint);
                        break;
                    case HELP_VIEWER_HEIGHT_STATE.DOWN_OVERFLOW:
                        this.setTooltipTriangleStyle(coordY + PopupConstant.SPACE_FROM_BUTTON, coordX - PopupConstant.TOOLTIP_TRIANGLE_WIDTH / 2, 'rotate(-180deg)');
                        pos = this.setPosition(coordY - height - PopupConstant.SPACE_FROM_BUTTON - PopupConstant.TOOLTIP_TRIANGLE_HEIGHT, coordX - this.startPoint);
                        break;
                }
                break;
            case HELP_VIEWER_WIDTH_STATE.LEFT_OVERFLOW:
                switch (heightState) {
                    case HELP_VIEWER_HEIGHT_STATE.OK:
                        this.setTooltipTriangleStyle(coordY + PopupConstant.SPACE_FROM_BUTTON, coordX - PopupConstant.TOOLTIP_TRIANGLE_WIDTH / 2, 'rotate(-90deg)');
                        pos = this.setPosition(coordY - this.startPoint, coordX + PopupConstant.SPACE_FROM_BUTTON + PopupConstant.TOOLTIP_TRIANGLE_HEIGHT);
                        break;
                    case HELP_VIEWER_HEIGHT_STATE.DOWN_OVERFLOW:
                        this.setTooltipTriangleStyle(coordY + PopupConstant.SPACE_FROM_BUTTON, coordX - PopupConstant.TOOLTIP_TRIANGLE_WIDTH / 2, 'rotate(-90deg)');
                        pos = this.setPosition(coordY - height + this.startPoint, coordX + PopupConstant.SPACE_FROM_BUTTON + PopupConstant.TOOLTIP_TRIANGLE_HEIGHT);
                        break;
                }
                break;
            case HELP_VIEWER_WIDTH_STATE.RIGHT_OVERFLOW:
                switch (heightState) {
                    case HELP_VIEWER_HEIGHT_STATE.OK:
                        this.setTooltipTriangleStyle(coordY + PopupConstant.SPACE_FROM_BUTTON, coordX - PopupConstant.TOOLTIP_TRIANGLE_WIDTH / 2, 'rotate(90deg)');
                        pos = this.setPosition(coordY - this.startPoint, coordX - width - PopupConstant.SPACE_FROM_BUTTON - PopupConstant.TOOLTIP_TRIANGLE_HEIGHT);
                        break;
                    case HELP_VIEWER_HEIGHT_STATE.DOWN_OVERFLOW:
                        this.setTooltipTriangleStyle(coordY + PopupConstant.SPACE_FROM_BUTTON, coordX - PopupConstant.TOOLTIP_TRIANGLE_WIDTH / 2, 'rotate(90deg)');
                        pos = this.setPosition(coordY - height + this.startPoint, coordX - width - PopupConstant.SPACE_FROM_BUTTON - PopupConstant.TOOLTIP_TRIANGLE_HEIGHT);
                        break;
                }
                break;
        }

        return pos;
    }

    private checkWidth(width: number): HELP_VIEWER_WIDTH_STATE {
        const value = this.coord.coordX - this.startPoint;
        const windowWidth = this.windowRefService.nativeWindow.innerWidth;

        return (value >= 0 && value + width <= windowWidth) ? HELP_VIEWER_WIDTH_STATE.OK
            : value < 0 ? HELP_VIEWER_WIDTH_STATE.LEFT_OVERFLOW
            : HELP_VIEWER_WIDTH_STATE.RIGHT_OVERFLOW;
    }

    private checkHeight(height: number): HELP_VIEWER_HEIGHT_STATE {
        const value = this.coord.coordY + PopupConstant.SPACE_FROM_BUTTON + height;
        const windowHeight = this.windowRefService.nativeWindow.innerHeight;

        return value <= windowHeight ? HELP_VIEWER_HEIGHT_STATE.OK : HELP_VIEWER_HEIGHT_STATE.DOWN_OVERFLOW;
    }

    private setTooltipTriangleStyle(top: number, left: number, transform: string): void {
        this.tooltipTriangleStyle = {
            ...this.tooltipTriangleStyle,
            ...{
                left: left < 0 ? 0 : `${left}px`,
                top: top < 0 ? 0 : `${top}px`,
                transform
            }
        };
    }

    private getHelpViewerText(viewerType: HELP_VIEWER_LIST): Observable<{[key: string]: any}[]> {
        return this.translateService.get(viewerType);
    }

    onClickOutside(): void {
        this.outClose.emit();
    }
}
