import { Action } from '@ngrx/store';

const UPDATE_URL_PATH = 'UPDATE_URL_PATH';

export class UpdateURLPath implements Action {
    readonly type = UPDATE_URL_PATH;
    constructor(public payload: string) {}
}

export function Reducer(state: string, action: UpdateURLPath): string {
    switch (action.type) {
        case UPDATE_URL_PATH:
            return action.payload;
        default:
            return state;
    }
}
