import { Action } from '@ngrx/store';

const UPDATE_RANGE = 'UPDATE_RANGE';

export class UpdateRange implements Action {
    readonly type = UPDATE_RANGE;
    constructor(public payload: number[]) {}
}

export function Reducer(state: number[], action: UpdateRange): number[] {
    switch (action.type) {
        case UPDATE_RANGE:
            return action.payload;
        default:
            return state;
    }
}
