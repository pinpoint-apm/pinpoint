import { Action } from '@ngrx/store';

const CHANGE_DATE_FORMAT = 'CHANGE_DATE_FORMAT';
export class ChangeDateFormat implements Action {
    readonly type = CHANGE_DATE_FORMAT;
    constructor(public payload: number) {}
}

export function Reducer(state = 0, action: ChangeDateFormat) {
    switch (action.type) {
        case CHANGE_DATE_FORMAT:
            return (state === action.payload) ? state : action.payload;
        default:
            return state;
    }
}
