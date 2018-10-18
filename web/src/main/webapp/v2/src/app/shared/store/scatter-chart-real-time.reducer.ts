import { Action } from '@ngrx/store';

const UPDATE_REAL_TIME_SCATTER_CHART_X_RANGE = 'UPDATE_REAL_TIME_SCATTER_CHART_X_RANGE';

export class UpdateRealTimeScatterChartXRange implements Action {
    readonly type = UPDATE_REAL_TIME_SCATTER_CHART_X_RANGE;
    constructor(public payload: IScatterXRange) {}
}

export function Reducer(state: IScatterXRange = {from: -1, to: -1}, action: UpdateRealTimeScatterChartXRange): IScatterXRange {
    switch (action.type) {
        case UPDATE_REAL_TIME_SCATTER_CHART_X_RANGE:
            if (state.from === action.payload.from && state.to === action.payload.to) {
                return state;
            }
            return action.payload;
        default:
            return state;
    }
}
