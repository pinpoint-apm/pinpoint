import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { filter } from 'rxjs/operators';

import { WindowRefService } from 'app/shared/services/window-ref.service';
import { WebAppSettingDataService } from 'app/shared/services/web-app-setting-data.service';

export enum TRACKED_EVENT_LIST {
    TOGGLE_HELP_VIEWER = 'Toggle HelpViewer',
    VERSION = 'Version',
    SELECT_APPLICATION = 'Select Application',
    SELECT_PERIOD = 'Select Period',
    SEARCH_NODE = 'Search Node',
    SELECT_APPLICATION_IN_SEARCH_RESULT = 'Select Application in Search Result',
    CLICK_NODE = 'Click Node',
    CLICK_NODE_IN_GROUPED_VIEW = 'Click Node in Grouped View',
    SHOW_GROUPED_NODE_VIEW = 'Show Grouped Node View',
    CLICK_LINK = 'Click Link',
    CLICK_LINK_IN_GROUPED_VIEW = 'Click Link in GROUPED View',
    SHOW_GROUPED_LINK_VIEW = 'Show Grouped Link View',
    CLICK_SCATTER_SETTING = 'Click Scatter Setting',
    DOWNLOAD_SCATTER = 'Download Scatter',
    GO_TO_FULL_SCREEN_SCATTER = 'Go to FullScreen Scatter',
    CLICK_RESPONSE_GRAPH = 'Click Response Graph',
    CLICK_LOAD_GRAPH = 'Click Load Graph',
    SHOW_SERVER_LIST = 'Show Server List',
    OPEN_INSPECTOR = 'Open Inspector',
    CLICK_FILTER_TRANSACTION = 'Click Filter Transaction',
    OPEN_FILTER_TRANSACTION_WIZARD = 'Open Filter Transaction Wizard',
    CLICK_callTree = 'Click CallTree Tab',
    CLICK_serverMap = 'Click ServerMap Tab',
    CLICK_timeline = 'Click Timeline Tab',
    SELECT_TRANSACTION = 'Select Transaction',
    OPEN_TRANSACTION_VIEW = 'Open Transaction View',
    CLICK_heap = 'Click Heap in Transaction View',
    CLICK_nonHeap = 'Click Non Heap in Transaction View',
    CLICK_cpu = 'Click CPU Load in Transaction View',
    REFRESH_SERVER_MAP = 'Refresh Server Map',
    SET_SERVER_MAP_OPTION = 'Set ServerMap Option',
    PIN_UP_REAL_TIME_CHART = 'Pin up RealTime Chart',
    REMOVE_PIN_ON_REAL_TIME_CHART = 'Remove Pin on RealTime Chart',
    TOGGLE_SERVER_TYPE_DETAIL = 'Toggle Server Type Detail',
    SELECT_AGENT = 'Select Agent',
    SET_PERIOD_AS_REAL_TIME = 'Set Period as RealTime',
    OPEN_THREAD_DUMP = 'Open Thread Dump',
    OPEN_CONFIGURATION_POPUP = 'Open Configuration Popup',
    SET_BOUND_IN_CONFIGURATION = 'Set Bound in Configuration',
    SET_SEARCH_PERIOD_IN_CONFIGURATION = 'Set Search Period in Configuration',
    SET_FAVORITE_APPLICATION_IN_CONFIGURATION = 'Set Favorite Application in Configuration',
    SET_TIMEZONE_IN_CONFIGURATION = 'Set Timezone in Configuration',
    SET_DATE_FORMAT_IN_CONFIGURATION = 'Set Date Format in Configuration',
    TOGGLE_PERIOD_SELECT_TYPE = 'Toggle Period Select Type',
    CLICK_MORE_STATE_BUTTON = 'Click MORE State Button',
    CLICK_RELOAD_APPLICATION_LIST_BUTTON = 'Click Reload Application List Button',
    CLICK_FIXED_PERIOD_MOVE_BUTTON = 'Click Fixed Period Move Button',
    OPEN_INSPECTOR_WITH_AGENT = 'Open Inspector with Agent',
    OPEN_TRANSACTION_LIST = 'Open Transaction List',
    GO_TO_APPLICATION_INSPECTOR = 'Go to Application Inspector',
    GO_TO_AGENT_INSPECTOR = 'Go To Agent Inspector',
    ZOOM_IN_TIMELINE = 'Zoom in Timeline',
    ZOOM_OUT_TIMELINE = 'Zoom out Timeline',
    MOVE_TO_PREV_ON_TIMELINE = 'Move to Prev on Timeline',
    MOVE_TO_NEXT_ON_TIMELINE = 'Move to Next on Timeline',
    MOVE_TO_NOW_ON_TIMELINE = 'Move to Now on Timeline',
    CHANGE_POINTING_TIME_ON_TIMELINE = 'Change Pointing Time on Timeline',
    CHANGE_SELECTION_RANGE_ON_TIMELINE = 'Change Selection Range on Timeline',
    SEARCH_AGENT = 'Search Agent',
    SEARCH_TRANSACTION = 'Search Transaction',
    SELECT_SQL = 'Select SQL',
    OPEN_TRANSACTION_DETAIL = 'Open Transaction Detail',
    CHANGE_SCATTER_CHART_STATE = 'Change Scatter Chart State',
    TOGGLE_SERVER_MAP_MERGE_STATE = 'Toggle ServerMap Merge State',
    SELECT_TRANSACTION_IN_TIMELINE = 'Select Transaction in Timeline',
}

@Injectable()
export class AnalyticsService {
    private currentPage: string;

    constructor(
        private windowRefService: WindowRefService,
        private webAppSettingDataService: WebAppSettingDataService
    ) {}
    private isAllowed(): Observable<boolean> {
        return this.webAppSettingDataService.isDataUsageAllowed().pipe(
            filter((result: boolean) => {
                return result;
            })
        );
    }

    trackPage(pageName: string): void {
        this.isAllowed().subscribe((result: boolean) => {
            if (this.windowRefService.nativeWindow.ga && typeof ga === 'function') {
                this.currentPage = pageName;
                ga('set', 'page', `/${pageName}`);
                ga('send', 'pageview');
            }
        });
    }
    /**
     *  eventCategory: 각 페이지 별 라우팅 주소
     *  eventAction: 액션 정보 ex. 동영상 다운로드
     *  eventLabel: 액션에 대한 추가 정보(Optional) ex. 동영상 이름
     *  eventValue: 액션에 대한 추가 정보2(Optional) 단, 수치로 제공 ex. 동영상 다운로드 액션 이벤트 발생 시, 다운로드 시간.
     */
    trackEvent(eventAction: string, eventLabel?: string, eventValue?: number): void {
        this.isAllowed().subscribe((result: boolean) => {
            if (this.windowRefService.nativeWindow.ga && typeof ga === 'function') {
                ga('send', 'event', { eventCategory: this.currentPage, eventAction, eventLabel, eventValue });
            }
        });
    }
}
