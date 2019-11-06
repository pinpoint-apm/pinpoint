import { Action } from '@ngrx/store';

import { ServerMapData } from 'app/core/components/server-map/class';

const UPDATE_SERVER_MAP_DATA = 'UPDATE_SERVER_MAP_DATA';

export class UpdateServerMapData implements Action {
    readonly type = UPDATE_SERVER_MAP_DATA;
    constructor(public payload: ServerMapData) {}
}

export function Reducer(state: ServerMapData, action: UpdateServerMapData): any {
    switch (action.type) {
        case UPDATE_SERVER_MAP_DATA:
            return action.payload;
        default:
            return state;
    }
}
