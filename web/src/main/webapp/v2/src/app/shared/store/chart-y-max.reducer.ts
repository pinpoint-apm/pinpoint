import { Action } from '@ngrx/store';

const UPDATE_RESPONSE_SUMMARY_CHART_Y_MAX = 'UPDATE_RESPONSE_SUMMARY_CHART_Y_MAX';
const UPDATE_LOAD_CHART_Y_MAX = 'UPDATE_LOAD_CHART_Y_MAX';

export class UpdateResponseSummaryChartYMax implements Action {
    readonly type = UPDATE_RESPONSE_SUMMARY_CHART_Y_MAX;
    constructor(public payload: number) {}
}

export class UpdateLoadChartYMax implements Action {
    readonly type = UPDATE_LOAD_CHART_Y_MAX;
    constructor(public payload: number) {}
}

export function ResponseSummaryChartYMaxReducer(state: number, action: UpdateResponseSummaryChartYMax): number {
    switch (action.type) {
        case UPDATE_RESPONSE_SUMMARY_CHART_Y_MAX:
            return action.payload;
        default:
            return state;
    }
}

export function LoadChartYMaxReducer(state: number, action: UpdateLoadChartYMax): number {
    switch (action.type) {
        case UPDATE_LOAD_CHART_Y_MAX:
            return action.payload;
        default:
            return state;
    }
}
