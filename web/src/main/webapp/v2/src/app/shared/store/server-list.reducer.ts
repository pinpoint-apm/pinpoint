import { Action } from '@ngrx/store';

export const UPDATE_SERVER_LIST = 'UPDATE_SERVER_LIST';

export class UpdateServerList implements Action {
    readonly type = UPDATE_SERVER_LIST;
    constructor(public payload: any) {}
}

export function Reducer(state = {}, action: UpdateServerList): any {
    switch (action.type) {
        case UPDATE_SERVER_LIST:
            return action.payload;
        default:
            return state;
    }
}
