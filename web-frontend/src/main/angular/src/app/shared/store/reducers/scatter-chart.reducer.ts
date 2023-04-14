import { Action } from '@ngrx/store';

const ADD_SCATTER_CHART_DATA = 'UPDATE_SCATTER_CHART_DATA';

export class AddScatterChartData implements Action {
    readonly type = ADD_SCATTER_CHART_DATA;
    constructor(public payload: IScatterData) {}
}

export function Reducer(state: IScatterData[] = [], action: AddScatterChartData): IScatterData[] {
    switch (action.type) {
        case ADD_SCATTER_CHART_DATA:
            return state.concat(action.payload);
        default:
            return state;
    }
}
