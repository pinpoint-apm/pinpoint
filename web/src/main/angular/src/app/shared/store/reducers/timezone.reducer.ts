import { Action } from '@ngrx/store';

const CHANGE_TIMEZONE = 'CHANGE_TIMEZONE';
export class ChangeTimezone implements Action {
    readonly type = CHANGE_TIMEZONE;
    constructor(public payload: string) {}
}

export function Reducer(state = 'Asia/Seoul', action: ChangeTimezone): string {
    switch (action.type) {
        case CHANGE_TIMEZONE:
            return (state === action.payload) ? state : action.payload;
        default:
            return state;
    }
}
