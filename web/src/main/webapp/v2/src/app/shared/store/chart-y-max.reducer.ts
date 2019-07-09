import { Action } from '@ngrx/store';

const UPDATE_RESPONSE_SUMMARY_CHART_Y_MAX = 'UPDATE_RESPONSE_SUMMARY_CHART_Y_MAX';

export class UpdateResponseSummaryChartYMax implements Action {
    readonly type = UPDATE_RESPONSE_SUMMARY_CHART_Y_MAX;
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
