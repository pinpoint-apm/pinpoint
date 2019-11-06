import { Action } from '@ngrx/store';

const UPDATE_SERVER_MAP_TARGET_SELECTED = 'UPDATE_SERVER_MAP_TARGET_SELECTED';

export class UpdateServerMapTargetSelected implements Action {
    readonly type = UPDATE_SERVER_MAP_TARGET_SELECTED;
    constructor(public payload: ISelectedTarget) {}
}

export function Reducer(state: ISelectedTarget, action: UpdateServerMapTargetSelected): ISelectedTarget {
    switch (action.type) {
        case UPDATE_SERVER_MAP_TARGET_SELECTED:
            return action.payload;
        default:
            return state;
    }
}
