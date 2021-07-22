import { Action } from '@ngrx/store';

const CHANGE_AGENT = 'CHANGE_AGENT';

export class ChangeAgent implements Action {
    readonly type = CHANGE_AGENT;
    constructor(public payload: string) {}
}

export function Reducer(state = '', action: ChangeAgent): string {
    switch (action.type) {
        case CHANGE_AGENT:
            return action.payload;
        default:
            return state;
    }
}
