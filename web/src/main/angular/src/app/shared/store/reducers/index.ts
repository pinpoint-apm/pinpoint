import { ActionReducerMap, createSelector, createFeatureSelector } from '@ngrx/store';

import * as admin from './admin.reducer';
import * as agentSelectionForSideBar from './agent-selection-for-side-bar.reducer';
import * as applicationList from './application-list.reducer';
import * as dateFormat from './date-format.reducer';
import * as favoriteApplicationList from './favorite-application-list.reducer';
import * as scatterChart from './scatter-chart.reducer';
import * as serverAndAgent from './server-and-agent.reducer';
import * as serverList from './server-list.reducer';
import * as timeline from './timeline.reducer';
import * as timezone from './timezone.reducer';
import * as transactionTimelineData from './transaction-timeline-data.reducer';
import * as transactionDetailData from './transaction-detail-data.reducer';
import * as transactionData from './transaction-info.reducer';
import * as serverMapLoadingState from './server-map-loading-state.reducer';
import * as uiState from './ui-state.reducer';
import * as chartLayout from './inspector-chart-layout-info.reducer';
import * as transactionViewType from './transaction-view-type.reducer';
import * as chartYMax from './chart-y-max.reducer';
import * as language from './language.reducer';
import * as hostGroupList from './host-group-list.reducer';
import { IApplicationListState } from './application-list.reducer';
import { IFavoriteApplicationListState } from './favorite-application-list.reducer';
import { IHostGroupListState } from './host-group-list.reducer';

export interface AppState {
    agentSelection: string;
    agentSelectionForServerList: IAgentSelection;
    applicationList: IApplicationListState;
    favoriteApplicationList: IFavoriteApplicationListState;
    dateFormat: number;
    scatterChart: IScatterData;
    serverList: any;
    serverMapLoadingState: string;
    timezone: string;
    transactionData: ITransactionMetaData;
    transactionDetailData: ITransactionDetailData;
    transactionTimelineData: ITransactionTimelineData;
    adminAgentList: { [key: string]: IAgent[] };
    serverAndAgent: string;
    uiState: IUIState;
    timeline: ITimelineInfo;
    applicationInspectorChartLayout: IChartLayoutInfoResponse;
    agentInspectorChartLayout: IChartLayoutInfoResponse;
    transactionViewType: string;
    responseAvgMaxChartYMax: number;
    responseSummaryChartYMax: number;
    loadChartYMax: number;
    loadAvgMaxChartYMax: number;
    language: string;
    hostGroupList: IHostGroupListState;
}

export const STORE_KEY: {[key: string]: keyof AppState} = {
    AGENT_SELECTION: 'agentSelection',
    APPLICATION_LIST: 'applicationList',
    FAVORITE_APPLICATION_LIST: 'favoriteApplicationList',
    AGENT_SELECTION_FOR_SERVER_LIST: 'agentSelectionForServerList',
    DATE_FORMAT: 'dateFormat',
    SCATTER_CHART: 'scatterChart',
    SERVER_LIST: 'serverList',
    SERVER_MAP_LOADING_STATE: 'serverMapLoadingState',
    TIMEZONE: 'timezone',
    TRANSACTION_DATA: 'transactionData',
    TRANSACTION_DETAIL_DATA: 'transactionDetailData',
    TRANSACTION_TIMELINE_DATA: 'transactionTimelineData',
    ADMIN_AGENT_LIST: 'adminAgentList',
    SERVER_AND_AGENT: 'serverAndAgent',
    UI_STATE: 'uiState',
    TIMELINE: 'timeline',
    APPLICATION_INSPECTOR_CHART_LAYOUT: 'applicationInspectorChartLayout',
    AGENT_INSPECTOR_CHART_LAYOUT: 'agentInspectorChartLayout',
    TRANSACTION_VIEW_TYPE: 'transactionViewType',
    RESPONSE_AVG_MAX_CHART_Y_MAX: 'responseAvgMaxChartYMax',
    RESPONSE_SUMMARY_CHART_Y_MAX: 'responseSummaryChartYMax',
    LOAD_CHART_Y_MAX: 'loadChartYMax',
    LOAD_AVG_MAX_CHART_Y_MAX: 'loadAvgMaxChartYMax',
    LANGUAGE: 'language',
};

export const reducers: ActionReducerMap<any> = {
    // [STORE_KEY.AGENT_INFo]: agentInfoReducer 방식은 빌드시 에러가 발생 함.
    agentSelection: agentSelectionForSideBar.Reducer,
    applicationList: applicationList.Reducer,
    favoriteApplicationList: favoriteApplicationList.Reducer,
    dateFormat: dateFormat.Reducer,
    scatterChart: scatterChart.Reducer,
    serverList: serverList.Reducer,
    serverMapLoadingState: serverMapLoadingState.Reducer,
    timezone: timezone.Reducer,
    transactionData: transactionData.Reducer,
    transactionDetailData: transactionDetailData.Reducer,
    transactionTimelineData: transactionTimelineData.Reducer,
    adminAgentList: admin.Reducer,
    serverAndAgent: serverAndAgent.Reducer,
    uiState: uiState.Reducer,
    timeline: timeline.Reducer,
    applicationInspectorChartLayout: chartLayout.ApplicationInspectorChartLayoutReducer,
    agentInspectorChartLayout: chartLayout.AgentInspectorChartLayoutReducer,
    transactionViewType: transactionViewType.Reducer,
    responseSummaryChartYMax: chartYMax.ResponseSummaryChartYMaxReducer,
    responseAvgMaxChartYMax: chartYMax.ResponseAvgMaxChartYMaxReducer,
    loadChartYMax: chartYMax.LoadChartYMaxReducer,
    loadAvgMaxChartYMax: chartYMax.LoadAvgMaxChartYMaxReducer,
    language: language.Reducer,
    hostGroupList: hostGroupList.Reducer,
};

export const Actions = {
    'ChangeTimezone': timezone.ChangeTimezone,
    'ChangeDateFormat': dateFormat.ChangeDateFormat,
    'ChangeAgent': agentSelectionForSideBar.ChangeAgent,
    'UpdateTransactionData': transactionData.UpdateTransactionData,
    'UpdateTransactionDetailData': transactionDetailData.UpdateTransactionDetailData,
    'UpdateTransactionTimelineData': transactionTimelineData.UpdateTransactionTimelineData,
    'UpdateServerList': serverList.UpdateServerList,
    'AddScatterChartData': scatterChart.AddScatterChartData,
    'UpdateServerMapLoadingState': serverMapLoadingState.UpdateServerMapLoadingState,
    'UpdateFilterOfServerAndAgentList': serverAndAgent.UpdateFilterOfServerAndAgentList,
    'UpdateAdminAgentList': admin.UpdateAdminAgentList,
    'ChangeInfoPerServerVisibleState': uiState.ChangeInfoPerServerVisibleState,
    'UpdateTimelineData': timeline.UpdateTimelineData,
    'UpdateApplicationInspectorChartLayout': chartLayout.UpdateApplicationInspectorChartLayoutInfo,
    'UpdateAgentInspectorChartLayout': chartLayout.UpdateAgentInspectorChartLayoutInfo,
    'ChangeTransactionViewType': transactionViewType.ChangeTransactionViewType,
    'UpdateResponseSummaryChartYMax': chartYMax.UpdateResponseSummaryChartYMax,
    'UpdateResponseAvgMaxChartYMax': chartYMax.UpdateResponseAvgMaxChartYMax,
    'UpdateLoadChartYMax': chartYMax.UpdateLoadChartYMax,
    'UpdateLoadAvgMaxChartYMax': chartYMax.UpdateLoadAvgMaxChartYMax,
    'ChangeLanguage': language.ChangeLanguage
};

const getUI = createFeatureSelector('uiState');
export const selectInfoPerServerVisibleState = createSelector(
    getUI,
    (state: IUIState) => state['infoPerServer']
);

const getTimeline = createFeatureSelector('timeline');
export const selectTimelineRange = createSelector(
    getTimeline,
    (state: ITimelineInfo) => state['range']
);
export const selectTimelineSelectionRange = createSelector(
    getTimeline,
    (state: ITimelineInfo) => state['selectionRange']
);
export const selectTimelineSelectedTime = createSelector(
    getTimeline,
    (state: ITimelineInfo) => state['selectedTime']
);
