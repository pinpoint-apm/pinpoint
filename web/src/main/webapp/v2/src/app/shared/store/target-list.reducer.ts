import { Action } from '@ngrx/store';

const UPDATE_SERVER_MAP_SELECTED_TARGET_BY_LIST = 'UPDATE_SERVER_MAP_SELECTED_TARGET_BY_LIST';

export class UpdateServerMapSelectedTargetByList implements Action {
    readonly type = UPDATE_SERVER_MAP_SELECTED_TARGET_BY_LIST;
    constructor(public payload: any) {}
}
export function Reducer(state: any, action: UpdateServerMapSelectedTargetByList): any {
    switch (action.type) {
        case UPDATE_SERVER_MAP_SELECTED_TARGET_BY_LIST:
            if (state && state.key === action.payload.key) {
                return state;
            } else {
                return action.payload;
            }
        default:
            return state;
    }
}
