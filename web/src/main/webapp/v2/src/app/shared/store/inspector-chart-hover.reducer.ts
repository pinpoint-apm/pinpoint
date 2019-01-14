import { Action } from '@ngrx/store';

const SYNC_HOVER_ON_INSPECTOR_CHARTS = 'SYNC_HOVER_ON_INSPECTOR_CHARTS';
export class ChangeHoverOnInspectorCharts implements Action {
    readonly type = SYNC_HOVER_ON_INSPECTOR_CHARTS;
    constructor(public payload: IHoveredInfo) {}
}

export function Reducer(state: IHoveredInfo, action: ChangeHoverOnInspectorCharts): IHoveredInfo {
    switch ( action.type ) {
        case SYNC_HOVER_ON_INSPECTOR_CHARTS:
            return action.payload;
        default:
            return state;
    }
}
