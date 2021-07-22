import { Action } from '@ngrx/store';

const APPLICATION_INSPECTOR_CHART_LAYOUT_INFO = 'APPLICATION_INSPECTOR_CHART_LAYOUT_INFO';
const AGENT_INSPECTOR_CHART_LAYOUT_INFO = 'AGENT_INSPECTOR_CHART_LAYOUT_INFO';

export class UpdateApplicationInspectorChartLayoutInfo implements Action {
    readonly type = APPLICATION_INSPECTOR_CHART_LAYOUT_INFO;
    constructor(public payload: {[key: string]: IChartLayoutInfo[]}) {}
}
export class UpdateAgentInspectorChartLayoutInfo implements Action {
    readonly type = AGENT_INSPECTOR_CHART_LAYOUT_INFO;
    constructor(public payload: {[key: string]: IChartLayoutInfo[]}) {}
}

export function ApplicationInspectorChartLayoutReducer(state: IChartLayoutInfoResponse, action: UpdateApplicationInspectorChartLayoutInfo): IChartLayoutInfoResponse {
    switch (action.type) {
        case APPLICATION_INSPECTOR_CHART_LAYOUT_INFO:
            if (state && state.applicationInspectorChart) {
                if (isSameInnerData(state.applicationInspectorChart, action.payload.applicationInspectorChart)) {
                    return state;
                }
            }
            return action.payload;
        default:
            return state;
    }
}
export function AgentInspectorChartLayoutReducer(state: IChartLayoutInfoResponse, action: UpdateAgentInspectorChartLayoutInfo): IChartLayoutInfoResponse {
    switch (action.type) {
        case AGENT_INSPECTOR_CHART_LAYOUT_INFO:
            if (state && state.agentInspectorChart) {
                if (isSameInnerData(state.agentInspectorChart, action.payload.agentInspectorChart)) {
                    return state;
                }
            }
            return action.payload;
        default:
            return state;
    }
}

function isSameInnerData(savedDataList: IChartLayoutInfo[], newDataList: IChartLayoutInfo[]): boolean {
    for (let i = 0 ; i < savedDataList.length ; i++) {
        const savedData = savedDataList[i];
        const findedIndex = newDataList.findIndex((newData: IChartLayoutInfo) => {
            return savedData.chartName === newData.chartName;
        });
        if (findedIndex === -1) {
            return false;
        }
        const findedData = newDataList[findedIndex];
        if (savedData.index !== findedData.index || savedData.visible !== findedData.visible) {
            return false;
        }
    }
    return true;
}

