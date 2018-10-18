import { Action } from '@ngrx/store';

const CHANGE_LOAD_CHART_Y_MAX = 'CHANGE_LOAD_Y_MAX';

export class ChangeLoadChartYMax implements Action {
    readonly type = CHANGE_LOAD_CHART_Y_MAX;
    constructor(public payload: number) {}
}

export function Reducer(state = 0, action: ChangeLoadChartYMax) {
    switch (action.type) {
        case CHANGE_LOAD_CHART_Y_MAX:
            return action.payload;
        default:
            return state;
    }
}

