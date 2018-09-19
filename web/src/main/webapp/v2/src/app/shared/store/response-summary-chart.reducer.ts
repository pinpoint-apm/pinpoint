import { Action } from '@ngrx/store';

const CHANGE_RESPONSE_SUMMARY_CHART_Y_MAX = 'CHANGE_RESPONSE_SUMMARY_Y_MAX';

export class ChangeResponseSummaryChartYMax implements Action {
    readonly type = CHANGE_RESPONSE_SUMMARY_CHART_Y_MAX;
    constructor(public payload: number) {}
}

export function Reducer(state = 0, action: ChangeResponseSummaryChartYMax) {
    switch (action.type) {
        case CHANGE_RESPONSE_SUMMARY_CHART_Y_MAX:
            return action.payload;
        default:
            return state;
    }
}

