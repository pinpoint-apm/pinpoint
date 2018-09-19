import { Action } from '@ngrx/store';

const UPDATE_SERVER_MAP_DATA = 'UPDATE_SERVER_MAP_DATA';

export class UpdateServerMapData implements Action {
    readonly type = UPDATE_SERVER_MAP_DATA;
    constructor(public payload: any) {}
}

export function Reducer(state = {}, action: UpdateServerMapData): any {
    switch (action.type) {
        case UPDATE_SERVER_MAP_DATA:
            return action.payload;
        default:
            return state;
    }
}
